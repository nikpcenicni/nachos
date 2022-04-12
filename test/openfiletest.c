#include "stdio.h"
#include "stdlib.h"

#define MAXOPENFILES   13         
#define TESTFILE       "test1.in"
#define TESTFILE2      "test2.in"
#define TESTFILE3      "test3.in"

int fds[MAXOPENFILES];   

int main(int argc, char *argv[]) {

    printf("Openfile test: Started\n");

    printf("Openfile test: Open first file %s \n", TESTFILE);
    fds[0] = open(TESTFILE);
    if (fds[0] == -1) {
        printf("Openfile test: Failed to open file %s \n", TESTFILE);
        exit(-1);
    }

    printf("Openfile test: Open second file %s \n", TESTFILE2);
    fds[1] = open(TESTFILE2);
    if (fds[1] == -1) {
        printf("Openfile test: Failed to open file %s \n", TESTFILE2);
        exit(-1);
    }

    printf("Openfile test: Open third file %s \n", TESTFILE3);
    fds[2] = open(TESTFILE3);
    if (fds[2] == -1) {
        printf("Openfile test: Failed to open file %s \n", TESTFILE3);
        exit(-1);
    }

    printf("Openfile test: Finished Successfully \n");
    exit(0);

}