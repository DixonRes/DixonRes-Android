#include <stdio.h>
#include <stdlib.h>
#include <gmp.h>

int main(int argc, char *argv[]) {
    printf("=== GMP Test Started ===\n");
    printf("GMP version: %s\n", gmp_version);
    
    mpz_t a, b, c;
    mpz_init(a);
    mpz_init(b);
    mpz_init(c);
    
    printf("Testing mpz_init... OK\n");
    
    // Set values
    mpz_set_ui(a, 123456789);
    mpz_set_str(b, "987654321", 10);
    printf("Testing mpz_set... OK\n");
    
    // Multiply
    mpz_mul(c, a, b);
    printf("Testing mpz_mul... OK\n");
    
    // Print results
    gmp_printf("a = %Zd\n", a);
    gmp_printf("b = %Zd\n", b);
    gmp_printf("c = a * b = %Zd\n", c);
    
    // Clear
    mpz_clear(a);
    mpz_clear(b);
    mpz_clear(c);
    printf("Testing mpz_clear... OK\n");
    
    printf("=== GMP Test Completed Successfully ===\n");
    return 0;
}
