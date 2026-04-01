#include <stdlib.h>

void *aligned_alloc(size_t alignment, size_t size) {
    void *ptr;
    int ret = posix_memalign(&ptr, alignment, size);
    return ret == 0 ? ptr : NULL;
}
