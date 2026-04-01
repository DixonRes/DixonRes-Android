#include <stdio.h>
#include <stdlib.h>
#include <flint/flint.h>
#include <flint/fmpz.h>
#include <flint/fmpz_mod.h>
#include <flint/fmpz_mod_poly.h>

int main(int argc, char *argv[]) {
    printf("=== FLINT Test Started ===\n");
    printf("FLINT version: %s\n", flint_version);
    
    // Test 1: Basic fmpz operations
    printf("\n--- Test 1: Basic fmpz operations ---\n");
    fmpz_t a, b, c;
    fmpz_init(a);
    fmpz_init(b);
    fmpz_init(c);
    printf("fmpz_init... OK\n");
    
    fmpz_set_ui(a, 123456789);
    fmpz_set_str(b, "987654321", 10);
    printf("fmpz_set... OK\n");
    
    fmpz_mul(c, a, b);
    printf("fmpz_mul... OK\n");
    
    printf("a = "); fmpz_print(a); printf("\n");
    printf("b = "); fmpz_print(b); printf("\n");
    printf("c = a * b = "); fmpz_print(c); printf("\n");
    
    fmpz_clear(a);
    fmpz_clear(b);
    fmpz_clear(c);
    printf("fmpz_clear... OK\n");
    
    // Test 2: Modular arithmetic
    printf("\n--- Test 2: Modular arithmetic ---\n");
    fmpz_mod_ctx_t ctx;
    fmpz_t mod;
    fmpz_init_set_ui(mod, 101);
    fmpz_mod_ctx_init(ctx, mod);
    printf("fmpz_mod_ctx_init... OK\n");
    
    fmpz_t x, y, z;
    fmpz_init(x);
    fmpz_init(y);
    fmpz_init(z);
    
    fmpz_set_ui(x, 42);
    fmpz_set_ui(y, 58);
    printf("Set x=42, y=58, mod=101... OK\n");
    
    fmpz_mod_add(z, x, y, ctx);
    printf("fmpz_mod_add... OK\n");
    printf("z = (x + y) mod mod = "); fmpz_print(z); printf("\n");
    
    fmpz_mod_mul(z, x, y, ctx);
    printf("fmpz_mod_mul... OK\n");
    printf("z = (x * y) mod mod = "); fmpz_print(z); printf("\n");
    
    fmpz_clear(x);
    fmpz_clear(y);
    fmpz_clear(z);
    fmpz_clear(mod);
    fmpz_mod_ctx_clear(ctx);
    printf("fmpz_mod_ctx_clear... OK\n");
    
    // Test 3: Polynomial operations
    printf("\n--- Test 3: Polynomial operations ---\n");
    fmpz_mod_poly_t poly1, poly2, poly3;
    fmpz_t p;
    fmpz_init_set_ui(p, 101);
    fmpz_mod_ctx_t ctx_poly;
    fmpz_mod_ctx_init(ctx_poly, p);
    
    fmpz_mod_poly_init(poly1, ctx_poly);
    fmpz_mod_poly_init(poly2, ctx_poly);
    fmpz_mod_poly_init(poly3, ctx_poly);
    printf("fmpz_mod_poly_init... OK\n");
    
    fmpz_mod_poly_set_coeff_ui(poly1, 0, 1, ctx_poly);
    fmpz_mod_poly_set_coeff_ui(poly1, 1, 1, ctx_poly);
    printf("Set poly1 = 1 + x... OK\n");
    
    fmpz_mod_poly_set_coeff_ui(poly2, 0, 1, ctx_poly);
    fmpz_mod_poly_set_coeff_ui(poly2, 1, 2, ctx_poly);
    printf("Set poly2 = 1 + 2x... OK\n");
    
    fmpz_mod_poly_mul(poly3, poly1, poly2, ctx_poly);
    printf("fmpz_mod_poly_mul... OK\n");
    
    printf("poly3 = poly1 * poly2 = ");
    fmpz_mod_poly_print_pretty(poly3, "x", ctx_poly);
    printf("\n");
    
    fmpz_mod_poly_clear(poly1, ctx_poly);
    fmpz_mod_poly_clear(poly2, ctx_poly);
    fmpz_mod_poly_clear(poly3, ctx_poly);
    fmpz_clear(p);
    fmpz_mod_ctx_clear(ctx_poly);
    printf("fmpz_mod_poly_clear... OK\n");
    
    printf("\n=== FLINT Test Completed Successfully ===\n");
    return 0;
}
