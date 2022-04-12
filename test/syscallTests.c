#include "stdio.h"
#include "stdlib.h"



#define NULL           0
#define NUMTESTS       2
#define NAN            (0xEFFFFFFF)
#define MAXARGC        20
#define MAXPROCESS     10
#define MAXOPENFILES   13             /* MaxOpenFiles=16, 16-3(stdin/stdout/stderr)=13*/
#define LOG            printf
#define TRUE           1
#define STDIN          0
#define STDOUT         1
#define STDERR         2
#define FALSE          0
#define TESTFILE       "testVar1.txt"
#define TESTFILE2      "testVar2.txt"
#define INPUTFILE      "mv.c"
#define VAR7IN         "cp.in"
#define VAR7OUT        "cp.out"
#define OUTPUTFILE     "test.out"
#define MAXRUN         10
#define BUFSIZE        100

void log(char *format, ...);
void route(int, char);


int  status;                    /* return value of system call                           */
int  fd;                        /* file handle                                           */        
int  exitstatus;                /* exit status of child process                          */
int  flag;                      /* condition variable: TRUE or FALSE                     */
int  i;                         /* loop counter                                          */
int  cnt,tmp;                         
int  fds[MAXOPENFILES];         /* file hadle array                                      */
int  pid;                       /* child process id                                      */

char *executable;               /* executable file name for exec()                       */
char *_argv[MAXARGC];           /* argv for testing executable                           */
int  _argc;                     /* argc for testing executable                           */
char buf[BUFSIZE+1];            /* IO buf for read/write                                 */
char buf2[BUFSIZE+1];           /* The second buf that will be used to compare string    */
char *p;                        /* buf pointer                                           */
int  amount;                    /* amount(byte) per each read/write                      */


int main(int argc, char *argv[]) { 

    int i;
    int test = 0;
    char dbg_flag = 'd';

    if (argc > 1) {
        route(test, dbg_flag);
    } else {
        LOG("File Syscall Test run all tests \n");
        for (i = 0; i < NUMTESTS; i++) {
            LOG("File Syscall Test run the %d test\n", i);
            route(i, dbg_flag);
        }
    }   
    LOG("File Syscall Test finished\n");

    return 0;
};

void route(int test, char dbg_flag) {
    
    switch (test)
    {
        case 0:
            LOG("File Syscall Test 1: Started \n");
            LOG("File Syscall Test 1: creates a file and checks syscall create works\n");
            status = create(TESTFILE);
            if (status == -1) {
                LOG("File Syscall Test 1: Failed to create file %s \n", TESTFILE);
                exit(-1);
            }
            close(status);
            LOG("File Syscall Test 1: Finished Successfully \n");

            break;

        case 1:
            LOG("File Syscall Test 2: Started \n");
            LOG("File Syscall Test 2: Call syscall create/close/unlink and checks functionality\n");

            LOG("File Syscall Test 2: Call syscall create to creat file %s\n", TESTFILE2);
            fd = create(TESTFILE2);
            if (fd == -1) {
                LOG("File Syscall Test 2: Failed to create file %s \n", TESTFILE2);
                exit(-1);
            }

            LOG("File Syscall Test 2: Call syscall close \n");
            close(fd);

            LOG("File Syscall Test 2: Call syscall unlink to delete %s\n", TESTFILE2);
            status = unlink(TESTFILE2);
            if (status == -1) {
                LOG("File Syscall Test 2: Failed to delete file %s \n", TESTFILE2);
                exit(-1);
            }

            LOG("File Syscall Test 2: Call syscall create again to creat file %s\n", TESTFILE2);
            fd = create(TESTFILE2);
            if (fd == -1) {
                LOG("File Syscall Test 2: Failed to create file %s \n", TESTFILE2);
                exit(-1);
            }

            LOG("File Syscall Test 2: Call syscall unlink to delete file %s without close \n", TESTFILE2);
            status = unlink(TESTFILE2);
            if (status == -1) {
                LOG("File Syscall Test 2: Failed to delete file %s \n", TESTFILE2);
                exit(-1);
            }

            LOG("File Syscall Test 2: Call syscall unlink ensure that %s is deleted\n", TESTFILE2);
            status = unlink(TESTFILE2);
            if (status == -1) {
                LOG("File Syscall Test 2: Failed to delete file %s in previous call \n", TESTFILE2);
                exit(-1);
            }

            LOG("File Syscall Test 2: Call syscall completed successfully \n");
            break;

    }

};