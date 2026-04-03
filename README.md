# DixonRes-Android

Dixon Resultant Computation for Android (ARM64)

**Note: Early test version – major bugs and calculation errors exist.**

We found that basic Flint functionality can be used on Android, but there are numerous bugs.
Our attempt on Android should stop here. You can use our Linux version [DixonRes](https://github.com/DixonRes/DixonRes) via Termux.

    # install FLINT
    wget https://flintlib.org/download/flint-3.4.0.zip
    unzip flint-3.4.0.zip && cd flint-3.4.0
    ./configure --prefix=$PREFIX --enable-static
    make -j4
    make install
    cd ..
    # install PML (Optional)
    git clone https://github.com/vneiger/pml.git
    cd pml/flint-extras
    ./bootstrap.sh
    ./configure --prefix=$PREFIX --enable-static
    make -j4
    make install
    cd ../..
    # install DixonRes
    git clone https://github.com/DixonRes/DixonRes.git
    cd DixonRes
    ./configure --prefix=$PREFIX
    make -j4
    make install

## Overview

This project implements Dixon resultant computation algorithm for Android devices. It uses FLINT (Fast Library for Number Theory) for polynomial arithmetic and matrix operations.

## Features

- Dixon resultant computation over finite fields
- Support for multivariate polynomials
- Android ARM64 native support
- GMP, MPFR, FLINT library tests

## Requirements

- Android NDK (tested with r21e)
- Android SDK
- Apache Ant
- GMP 6.3.0
- MPFR 4.2.1
- FLINT 3.4.0

## Project Structure

```
DixonRes-Android/
├── build-apk.sh              # One-click build script
├── README.md                 # This file
├── dixon.c                   # Main entry point
├── src/                      # Source code
│   ├── dixon_flint.c
│   ├── dixon_interface_flint.c
│   ├── fq_mpoly_mat_det.c
│   ├── unified_mpoly_det.c
│   └── ... (other source files)
├── include/                  # Header files
│   ├── dixon.h
│   ├── unified_mpoly_interface.h
│   └── ... (other headers)
├── android-project/          # Android project
│   ├── build.xml
│   ├── AndroidManifest.xml
│   └── app/src/main/
│       ├── java/com/dixonres/app/MainActivity.java
│       └── res/layout/activity_main.xml
├── android-deps/             # Android dependencies
│   ├── test-gmp.c
│   ├── test-mpfr.c
│   ├── test-flint.c
│   └── flint-android/include/flint/
└── libs/arm64-v8a/           # Precompiled libraries
    ├── libgmp.so
    ├── libmpfr.so
    ├── libflint.so
    └── libflint.so.22
```

## Build Instructions

### 1. Set up environment

```bash
export ANDROID_NDK=/path/to/android-ndk
export ANDROID_SDK=/path/to/android-sdk
```

### 2. Build APK

```bash
cd DixonRes-Android
bash build-apk.sh
```

The APK will be generated at:
```
android-project/build/bin/DixonRes.apk
```

## Usage

### Input Format

- **Input Polynomials**: Comma-separated list of polynomials
- **Variables**: Comma-separated list of variables to eliminate
- **Modulus**: Prime number for finite field arithmetic

### Example

- Input: `x, y`
- Variables: `x,y`
- Modulus: `101`

This computes the resultant of the system {x, y} eliminating variable x over GF(101).

## Library Tests

The app includes test buttons for:
- **GMP**: Tests GNU Multiple Precision Arithmetic Library
- **MPFR**: Tests GNU Multiple Precision Floating-Point Reliable Library
- **FLINT**: Tests Fast Library for Number Theory

## Technical Details

### Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| GMP | 6.3.0 | Arbitrary precision arithmetic |
| MPFR | 4.2.1 | Floating-point arithmetic |
| FLINT | 3.4.0 | Number theory operations |

### Target Architecture

- ARM64 (aarch64-linux-android)
- Android API 24+

### Known Issues

1. FLINT 3.4.0 has a bug in `nmod_mpoly_sub` that can produce invalid polynomial states. This is handled by using `neg + add` instead of `sub` for 2x2 determinant computation.

2. The `flint_printf` function may not correctly print `fmpz_t` values on Android. Use `fmpz_print()` instead.

## License

This project is for educational and research purposes.

## Acknowledgments

- FLINT development team
- GMP and MPFR development teams
- Android NDK team
