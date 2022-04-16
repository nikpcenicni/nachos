#include "stdio.h"
#include "stdlib.h"


#define NUMTESTS       2        
#define TESTFILE       "testVar1.txt"
#define TESTFILE2      "testVar2.txt"

void log(char *format, ...);
void route(int, char);


int  status;                    /* return value of system call                           */
int  fd;                        /* file handle                                           */        
int  i;                         /* loop counter                                          */      

int main(int argc, char *argv[]) { 

    int i;
    int test = 0;
    char dbg_flag = 'd';

    if (argc > 1) {
        route(test, dbg_flag);
    } else {
        printf("File Syscall Test run all tests \n");
        for (i = 0; i < NUMTESTS; i++) {
            printf("File Syscall Test run the %d test\n", i);
            route(i, dbg_flag);
        }
    }   
    printf("File Syscall Test finished\n");

    return 0;
};

void route(int test, char dbg_flag) {
    
    switch (test)
    {
        case 0:
            printf("File Syscall Test 1: Started \n");
            printf("File Syscall Test 1: creates a file and checks syscall create works\n");
            status = creat(TESTFILE);
            if (status == -1) {
                printf("File Syscall Test 1: Failed to create file %s \n", TESTFILE);
                exit(-1);
            }
            close(status);
            printf("File Syscall Test 1: Finished Successfully \n");

            break;

        case 1:
            printf("File Syscall Test 2: Started \n");
            printf("File Syscall Test 2: Call syscall create/close/unlink and checks functionality\n");

            printf("File Syscall Test 2: Call syscall create to creat file %s\n", TESTFILE2);
            fd = creat(TESTFILE2);
            if (fd == -1) {
                printf("File Syscall Test 2: Failed to create file %s \n", TESTFILE2);
                exit(-1);
            }

            printf("File Syscall Test 2: Call syscall close \n");
            close(fd);

            printf("File Syscall Test 2: Call syscall unlink to delete %s\n", TESTFILE2);
            status = unlink(TESTFILE2);
            if (status == -1) {
                printf("File Syscall Test 2: Failed to delete file %s \n", TESTFILE2);
                exit(-1);
            }

            printf("File Syscall Test 2: Call syscall create again to creat file %s\n", TESTFILE2);
            fd = creat(TESTFILE2);
            if (fd == -1) {
                printf("File Syscall Test 2: Failed to create file %s \n", TESTFILE2);
                exit(-1);
            }

            printf("File Syscall Test 2: Call syscall unlink to delete file %s without close \n", TESTFILE2);
            status = unlink(TESTFILE2);
            if (status == -1) {
                printf("File Syscall Test 2: Failed to delete file %s \n", TESTFILE2);
                exit(-1);
            }

            printf("File Syscall Test 2: Call syscall unlink ensure that %s is deleted\n", TESTFILE2);
            status = unlink(TESTFILE2);
            if (status == -1) {
                printf("File Syscall Test 2: Failed to delete file %s in previous call \n", TESTFILE2);
                exit(-1);
            }

            printf("File Syscall Test 2: Call syscall completed successfully \n");
            break;

    }

};
