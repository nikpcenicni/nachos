#include "stdio.h"
#include "stdlib.h"        

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
        for (i = 0; i < 4; i++) {
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

        case 2:
            printf("File Syscall Test 3: Started \n");
            char fileName0[] = {'f','.','c'};
            char *namePtr0 = &fileName0[0];

            char fileName1[] = {'f','.','c','o','f','f'};
            char *namePtr1 = &fileName1[0];

            printf("File Syscall Test 3: Performing an Exec on file name without a .coff extension \n");
            int *r0;
            int a0 = exec(namePtr0,0,null);
            join(a0, r0);

            printf("File Syscall Test 3: Performing an Exec on file name with a .coff extension \n");
            int *r1;
            int a1 = exec(namePtr1,0,null);
            join(a1, r1);
            printf("File Syscall Test 3: Complete \n");

            break;

        case 3:
            printf("File Syscall Test 4: Started \n");
            printf("File Syscall Test 4: Creating process 1 \n");
            char fileName1[] = {'j','1','.','c','o','f','f'};
            char *ptr1 = &fileName1[0];
            
            printf("File Syscall Test 4: Creating process 2 \n");
            char fileName2[] = {'j','2','.','c','o','f','f'};
            char *ptr2 = &fileName2[0];

            int status1;
            int status2;
            
            int *r1 = &status1;
            int PID1 = exec(ptr1,0,null);

            char a = (char)PID1;
            char *args[1];
            args[0] = &a;

            int PID2 = exec(ptr2,1,args);
            int *r2 = &status2;
            
            printf("File Syscall Test 4: Joining process 2 with process 1 (non-child process) \n");
            join(PID2, r2);
            join(PID1, r1);	
            printf("File Syscall Test 4: PID1: %d\n PID2: %d\n", PID1, PID2);
            
            printf("File Syscall Test 4: Complete \n");

            printf("File Syscall Test 5: Started \n");
            printf("File Syscall Test 5: Exiting \n");		
            exit(-1);
            printf("File Syscall Test 5: Checking Process IDs to ensure they are null \n");
            printf("File Syscall Test 5: PID1: %d\n PID2: %d\n", PID1, PID2);
            printf("File Syscall Test 5: Complete \n");

            break;
    }

};
