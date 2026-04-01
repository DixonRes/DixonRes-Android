#!/bin/bash

# One-click build script for DixonRes Android APK
# This script builds dixon binary, test programs, and packages the APK

set -e

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  DixonRes Android APK Build Script${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Set up Android NDK path
export ANDROID_NDK=/usr/lib/android-ndk
export NDK_TOOLCHAIN=$ANDROID_NDK/toolchains/llvm/prebuilt/linux-x86_64
export TARGET=aarch64-linux-android
export API=24

export CC="$NDK_TOOLCHAIN/bin/$TARGET$API-clang"
export CXX="$NDK_TOOLCHAIN/bin/$TARGET$API-clang++"
export AR="$NDK_TOOLCHAIN/bin/llvm-ar"
export RANLIB="$NDK_TOOLCHAIN/bin/llvm-ranlib"
export STRIP="$NDK_TOOLCHAIN/bin/llvm-strip"

# Paths to our compiled libraries
export GMP_ROOT="$SCRIPT_DIR"
export MPFR_ROOT="$SCRIPT_DIR"
export FLINT_ROOT="$SCRIPT_DIR"

# CFLAGS and LDFLAGS
export CFLAGS="-target $TARGET$API -fPIC -O3"
export LDFLAGS="-target $TARGET$API"

echo -e "${YELLOW}Step 1: Building dixon binary...${NC}"
echo "========================================"

# Collect all source files
SOURCES=(
    "$PROJECT_DIR/dixon.c"
    "$PROJECT_DIR/src/dixon_complexity.c"
    "$PROJECT_DIR/src/dixon_flint.c"
    "$PROJECT_DIR/src/dixon_interface_flint.c"
    "$PROJECT_DIR/src/dixon_test.c"
    "$PROJECT_DIR/src/dixon_with_ideal_reduction.c"
    "$PROJECT_DIR/src/fmpq_acb_roots.c"
    "$PROJECT_DIR/src/fq_mat_det.c"
    "$PROJECT_DIR/src/fq_mpoly_mat_det.c"
    "$PROJECT_DIR/src/fq_multivariate_interpolation.c"
    "$PROJECT_DIR/src/fq_mvpoly.c"
    "$PROJECT_DIR/src/fq_nmod_roots.c"
    "$PROJECT_DIR/src/fq_poly_mat_det.c"
    "$PROJECT_DIR/src/fq_sparse_interpolation.c"
    "$PROJECT_DIR/src/fq_unified_interface.c"
    "$PROJECT_DIR/src/gf2n_field.c"
    "$PROJECT_DIR/src/gf2n_mpoly.c"
    "$PROJECT_DIR/src/gf2n_poly.c"
    "$PROJECT_DIR/src/polynomial_system_solver.c"
    "$PROJECT_DIR/src/rational_system_solver.c"
    "$PROJECT_DIR/src/resultant_with_ideal_reduction.c"
    "$PROJECT_DIR/src/unified_mpoly_det.c"
    "$PROJECT_DIR/src/unified_mpoly_interface.c"
    "$PROJECT_DIR/src/unified_mpoly_resultant.c"
    "$SCRIPT_DIR/android-deps/aligned_alloc_wrapper.c"
)

# Compile dixon
$CC $CFLAGS -o dixon \
    -I"$PROJECT_DIR/include" \
    -I"$PROJECT_DIR/android-deps/flint-android/include" \
    -I"$PROJECT_DIR/android-deps/gmp-android/include" \
    -I"$PROJECT_DIR/android-deps/mpfr-android/include" \
    "${SOURCES[@]}" \
    -L"$PROJECT_DIR/libs/arm64-v8a" \
    -Wl,--start-group \
    -lflint -lmpfr -lgmp \
    -Wl,--end-group \
    -lm -ldl

echo -e "${GREEN}dixon built successfully!${NC}"
ls -lh dixon
echo ""

echo -e "${YELLOW}Step 2: Building test programs...${NC}"
echo "========================================"

# Build GMP test
echo "Building test-gmp..."
$CC $CFLAGS -o test-gmp "$SCRIPT_DIR/android-deps/test-gmp.c" \
    -I"$PROJECT_DIR/android-deps/gmp-android/include" \
    -L"$PROJECT_DIR/libs/arm64-v8a" \
    -lgmp \
    -static-libgcc

echo -e "${GREEN}test-gmp built!${NC}"

# Build MPFR test
echo "Building test-mpfr..."
$CC $CFLAGS -o test-mpfr "$SCRIPT_DIR/android-deps/test-mpfr.c" \
    -I"$PROJECT_DIR/android-deps/mpfr-android/include" \
    -I"$PROJECT_DIR/android-deps/gmp-android/include" \
    -L"$PROJECT_DIR/libs/arm64-v8a" \
    -lmpfr -lgmp \
    -static-libgcc

echo -e "${GREEN}test-mpfr built!${NC}"

# Build FLINT test - use rpath to link to correct library
echo "Building test-flint..."
$CC $CFLAGS -o test-flint "$SCRIPT_DIR/android-deps/test-flint.c" \
    -I"$PROJECT_DIR/android-deps/flint-android/include" \
    -I"$PROJECT_DIR/android-deps/mpfr-android/include" \
    -I"$PROJECT_DIR/android-deps/gmp-android/include" \
    -L"$PROJECT_DIR/libs/arm64-v8a" \
    -Wl,-rpath,"$PROJECT_DIR/libs/arm64-v8a" \
    -lflint -lmpfr -lgmp \
    -lm -static-libgcc

echo -e "${GREEN}test-flint built!${NC}"

echo ""

echo -e "${YELLOW}Step 3: Copying files to Android project...${NC}"
echo "========================================"

# Copy dixon to assets
mkdir -p "$PROJECT_DIR/android-project/assets/"
cp dixon "$PROJECT_DIR/android-project/assets/"
mkdir -p "$PROJECT_DIR/android-project/app/src/main/assets/"
cp dixon "$PROJECT_DIR/android-project/app/src/main/assets/"

# Copy test programs
cp test-gmp test-mpfr test-flint "$PROJECT_DIR/android-project/app/src/main/assets/"

# Copy libraries to jniLibs
mkdir -p "$PROJECT_DIR/android-project/app/src/main/jniLibs/arm64-v8a/"

# Copy all FLINT library files
cp "$PROJECT_DIR/libs/arm64-v8a/libflint.so" "$PROJECT_DIR/android-project/app/src/main/jniLibs/arm64-v8a/"
cp "$PROJECT_DIR/libs/arm64-v8a/libflint.so.22" "$PROJECT_DIR/android-project/app/src/main/jniLibs/arm64-v8a/"
cp "$PROJECT_DIR/libs/arm64-v8a/libflint.so.22.0.0" "$PROJECT_DIR/android-project/app/src/main/jniLibs/arm64-v8a/"

# Copy other libraries
cp "$PROJECT_DIR/libs/arm64-v8a/libgmp.so" "$PROJECT_DIR/android-project/app/src/main/jniLibs/arm64-v8a/"
cp "$PROJECT_DIR/libs/arm64-v8a/libmpfr.so" "$PROJECT_DIR/android-project/app/src/main/jniLibs/arm64-v8a/"

# Also copy libraries to assets/lib/arm64-v8a for direct copying
mkdir -p "$PROJECT_DIR/android-project/app/src/main/assets/lib/arm64-v8a/"
cp "$PROJECT_DIR/libs/arm64-v8a/libflint.so" "$PROJECT_DIR/android-project/app/src/main/assets/lib/arm64-v8a/"
cp "$PROJECT_DIR/libs/arm64-v8a/libflint.so.22" "$PROJECT_DIR/android-project/app/src/main/assets/lib/arm64-v8a/"
cp "$PROJECT_DIR/libs/arm64-v8a/libgmp.so" "$PROJECT_DIR/android-project/app/src/main/assets/lib/arm64-v8a/"
cp "$PROJECT_DIR/libs/arm64-v8a/libmpfr.so" "$PROJECT_DIR/android-project/app/src/main/assets/lib/arm64-v8a/"

echo -e "${GREEN}All files copied!${NC}"
echo "" 
echo "=== Library structure ==="
ls -la "$PROJECT_DIR/android-project/app/src/main/jniLibs/arm64-v8a/"
echo ""

echo -e "${YELLOW}Step 4: Building Android APK...${NC}"
echo "========================================"

cd "$PROJECT_DIR/android-project"
ant debug

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Build Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "APK Location:"
echo "  $PROJECT_DIR/android-project/build/bin/DixonRes.apk"
echo ""
echo "File sizes:"
ls -lh "$PROJECT_DIR/android-project/build/bin/DixonRes.apk"
echo ""
