#include "syscall.h"
#include "stdlib.h"
#include "stdio.h"

int main(){

	char filename[] = {'t','e','s','t','.','t','x','t'};
	char *fPtr = &filename[0];

	int fd = creat(fPtr);
	printf("File descriptor: %d\n", fd);
	char buffer[] = {'H','E','L','L','O',' ','W','O','R','L','D','!','\n'};
	char *buf = &buffer[0];

	int bytesWritten = write(fd, buf, 13);

	printf("Bytes written: %d\n", bytesWritten);

	return 0;
}
	
