#include <stdio.h>
#include <string.h>
#include "loader.h"

int main() {
  void* hw = hw_open("../generated/libgcd.so");
    
  char str_in[100];
  while (1) {
    char* in = fgets(str_in, sizeof(str_in), stdin);
    if (in == NULL) break;
    if (strncmp(str_in, "quit", 4) == 0)
      break;
    const char* resp = hw_eval(hw, str_in);
    printf("CMD %s RSP %s\n", str_in, resp);
  }

  hw_close(hw);
}
