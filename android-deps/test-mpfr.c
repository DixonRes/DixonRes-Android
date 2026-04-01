#include <stdio.h>
#include <stdlib.h>
#include <mpfr.h>

int main(int argc, char *argv[]) {
    printf("=== MPFR Test Started ===\n");
    printf("MPFR version: %s\n", mpfr_get_version());
    
    mpfr_t a, b, c;
    mpfr_init2(a, 256);
    mpfr_init2(b, 256);
    mpfr_init2(c, 256);
    
    printf("Testing mpfr_init2... OK\n");
    
    // Set values
    mpfr_set_d(a, 3.1415926535, MPFR_RNDN);
    mpfr_set_d(b, 2.7182818284, MPFR_RNDN);
    printf("Testing mpfr_set_d... OK\n");
    
    // Multiply
    mpfr_mul(c, a, b, MPFR_RNDN);
    printf("Testing mpfr_mul... OK\n");
    
    // Print results
    printf("a = ");
    mpfr_out_str(stdout, 10, 20, a, MPFR_RNDN);
    printf("\n");
    
    printf("b = ");
    mpfr_out_str(stdout, 10, 20, b, MPFR_RNDN);
    printf("\n");
    
    printf("c = a * b = ");
    mpfr_out_str(stdout, 10, 20, c, MPFR_RNDN);
    printf("\n");
    
    // Clear
    mpfr_clear(a);
    mpfr_clear(b);
    mpfr_clear(c);
    printf("Testing mpfr_clear... OK\n");
    
    printf("=== MPFR Test Completed Successfully ===\n");
    return 0;
}
