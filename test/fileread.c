#include "syscall.h"
#include "stdlib.h"
#include "stdio.h"

int main(){

	char testfile[] = {'t','e','s','t','f','i','l','e','.','t','x','t'};
	char *filepointer = &testfile[0];
	int filedescriptor = open(filepointer);
	
	printf("This is the file: %d\n", filedescriptor);
	
	char array[24];
	char *buffer = &array[0];
	int bytes = read(filedescriptor, buffer, 24);

	printf("We have read this many bytes: %d\n", bytes);
	printf("Contents of the test file:\n%s\n", array);

	return 0;
}