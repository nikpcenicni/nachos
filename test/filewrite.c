#include "syscall.h"
#include "stdlib.h"
#include "stdio.h"

int main(){

	char testfile[] = {'t','e','s','t','f','i','l','e','.','t','x','t'};
	char *filepointer = &testfile[0];
	int filedescriptor = creat(filepointer);
	
	printf("This is the file: %d\n", filedescriptor);
	
	char array[] = {'W','r','i','t','e',' ','t','e','s','t','\n'};
	char *buffer = &array[0];
	int bytes = write(filedescriptor, buffer, 13);

	printf("We have written this many bytes: %d\n", bytes);

	return 0;
}
	
