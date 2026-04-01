#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

int main(int argc, char *argv[]) {
    // Disable output buffering
    setvbuf(stdout, NULL, _IONBF, 0);
    setvbuf(stderr, NULL, _IONBF, 0);

    printf("[TEST-SIMPLE] ========================================\n");
    printf("[TEST-SIMPLE] Program started\n");
    printf("[TEST-SIMPLE] PID: %d\n", getpid());
    printf("[TEST-SIMPLE] Number of arguments: %d\n", argc);
    for (int i = 0; i < argc; i++) {
        printf("[TEST-SIMPLE] argv[%d]: %s\n", i, argv[i]);
    }
    
    char *cwd = getcwd(NULL, 0);
    printf("[TEST-SIMPLE] Current directory: %s\n", cwd);
    free(cwd);
    
    printf("[TEST-SIMPLE] Environment variables:\n");
    printf("[TEST-SIMPLE] LD_LIBRARY_PATH: %s\n", getenv("LD_LIBRARY_PATH") ? getenv("LD_LIBRARY_PATH") : "not set");
    printf("[TEST-SIMPLE] ========================================\n");
    
    // Sleep a little bit to simulate work
    printf("[TEST-SIMPLE] Sleeping for 1 second...\n");
    sleep(1);
    
    printf("[TEST-SIMPLE] Done! Exiting with code 0\n");
    return 0;
}