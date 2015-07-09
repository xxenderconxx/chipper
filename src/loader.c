typedef void open_t(void);
typedef void close_t(void);
typedef const char* eval_t(const char*);

#include <dlfcn.h>
#include <stdlib.h>
#include <stdio.h>

#include "loader.h"

typedef struct {
  void*    handle;
  open_t*  open;
  eval_t*  eval;
  close_t* close;
} hw_t;

void test_dl_error(char* msg) {
  const char* dlsym_error = dlerror();
  if (dlsym_error) {
    fprintf(stderr, msg, dlsym_error);
    exit(-1);
  }
}

void* load_sym (void* handle, char* name) {
  void* sym = dlsym(handle, name);
  // fprintf(stderr, "HW_OPEN: LOADING SYMBOL %s HANDLE %p\n", name, sym);
  return sym;
}

void* hw_open(const char* name) {
  void* handle = dlopen(name, RTLD_LAZY);
  // fprintf(stderr, "HW_OPEN: LOADING %s -> HANDLE %p\n", name, handle);
  if (!handle) {
    fprintf(stderr, "Cannot load library: %s\n", dlerror());
    exit(-1);
  }
  dlerror();
    
  open_t* open_hw = (open_t*)load_sym(handle, "open");
  eval_t* eval_hw = (eval_t*)load_sym(handle, "eval");
  close_t* close_hw = (close_t*)load_sym(handle, "close");

  open_hw();

  hw_t* hw = (hw_t*)malloc(sizeof(hw_t));
  hw->open  = open_hw;
  hw->eval  = eval_hw;
  hw->close = close_hw;
  // fprintf(stderr, "HW = %p\n", hw);
  return hw;
}

const char* hw_eval (void* hw, const char* cmd) {
  return ((hw_t*)hw)->eval(cmd);
}

void hw_close (void* hw) {
  // fprintf(stderr, "HW_CLOSE: HW %p HANDLE %p\n", hw, ((hw_t*)hw)->handle);
  ((hw_t*)hw)->close();
  if (((hw_t*)hw)->handle != NULL)
    dlclose(((hw_t*)hw)->handle);
  free(hw);
}

