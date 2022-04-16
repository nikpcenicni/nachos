#include "stdio.h"
#include "stdlib.h"


#define NUMTESTS       2        

void log(char *format, ...);
void route(int, char);


int  status;
int  fd;
int  i;   
char testfile[] = {'t','e','s','t'};                                      */      

int main(int argc, char *argv[]) { 

    int i;
    int test = 0;
    char dbg_flag = 'd';

    if (argc > 1) {
        route(test, dbg_flag);
    } else {
        printf("File Syscall Test run all tests: \n");
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
            status = creat(testfile);
            if (status == -1) {
                printf("File Syscall Test 1: Failed to create file %s \n", testfile);
                exit(-1);
            }
            close(status);
            printf("File Syscall Test 1: Finished Successfully \n");

            break;

        case 1:
            printf("File Syscall Test 2: Started \n");
            printf("File Syscall Test 2: Call syscall create/close/unlink and check functionality\n");

            printf("File Syscall Test 2: Call syscall create to creat file %s\n", testfile);
            fd = creat(testfile);
            if (fd == -1) {
                printf("File Syscall Test 2: Failed to create file %s \n", testfile);
                exit(-1);
            }

            printf("File Syscall Test 2: Call syscall close \n");
            close(fd);

            printf("File Syscall Test 2: Call syscall unlink to delete %s\n", testfile);
            status = unlink(testfile);
            if (status == -1) {
                printf("File Syscall Test 2: Failed to delete file %s \n", testfile);
                exit(-1);
            }

            printf("File Syscall Test 2: Call syscall create again to creat file %s\n", testfile);
            fd = creat(testfile);
            if (fd == -1) {
                printf("File Syscall Test 2: Failed to create file %s \n", testfile);
                exit(-1);
            }

            printf("File Syscall Test 2: Call syscall unlink to delete file %s without close \n", testfile);
            status = unlink(testfile);
            if (status == -1) {
                printf("File Syscall Test 2: Failed to delete file %s \n", testfile);
                exit(-1);
            }

            printf("File Syscall Test 2: Call syscall unlink ensure that %s is deleted\n", testfile);
            status = unlink(testfile);
            if (status == -1) {
                printf("File Syscall Test 2: Failed to delete file %s in previous call \n", testfile);
                exit(-1);
            }

            printf("File Syscall Test 2: Completed successfully \n");
            break;
        
    }

};
