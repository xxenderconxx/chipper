#include <stdio.h>

int main (int argc, char* argv[]) {
  int c;
  for (;;) {
    c = getchar();
    if (c == EOF) break;
    if (c == '#' || c == '?' || c == '@')
      putchar('_');
    else if (c == '$') {
      //   char nc = getchar();
      // if (nc >= '0' && nc <= '9') {
      //   putchar('_'); 
      // } else {
      //   putchar(':'); putchar(':');
      // }
      // putchar(nc);
      putchar(':'); putchar(':');
    } else
      putchar(c);
  } 
}
