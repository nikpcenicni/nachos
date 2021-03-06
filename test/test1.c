#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

int main(){

	char testfile[] = {'d','o','e','s','n','t',' ','e','x','i','s','t'};
	char *filepointer = &testfile[0];
	int filedescriptor = open(filepointer);

	printf("Attempting to open a file that doesn't exist: %d\n\n", filedescriptor);
	
	if(filedescriptor != -1){
		printf("\033[1;31m");
		printf("Test failed!\n");
		printf("\033[0;30m");
		exit(0);
	}

	filedescriptor = creat(filepointer);
	printf("Attempting to create a file that doesn't exist: %d\n\n", filedescriptor);

	if(filedescriptor == -1){
		printf("\033[1;31m");
		printf("Test failed!\n");
		printf("\033[0;30m");
		exit(0);
	}
	
	// Testing to open the file
	int openfiledescriptor = open(filepointer);
	printf("Attempting to open the file: %d\n\n", openfiledescriptor);
	
	// Testing to create the file
	int createfiledescriptor = creat(filepointer);
	printf("Attempting to create the file: %d\n\n", createfiledescriptor);
	
	// Testing the maximum amount of files
	int i;
	printf("\nOpening the maximum amount of files!\n\n");	
	for(i = 2; i < 15; i++){
		printf("File Descriptor: %d\n", open(filepointer));
	}

	// Testing the closing of the files and unlinking
	printf("\nClose all the files that remain.\n");
	for(i = 2; i < 17; ++i){
		close(i);
		printf("Close File Descriptor: %d\n", i);	
	}
	unlink(filepointer);
	printf("unlinking: doesnt exist");

	//'doesnt exist' won't be in the filesystem anymore
	int finalcheck = open(filepointer);
	
	// Final check
	if(finalcheck == -1){
		printf("\033[1;32m");
		printf("\nTest succeeded!\n");
		printf("\033[0;30m");
		exit(0);
	}
	else{
		printf("\033[1;31m");
		printf("Test failed!\n");
		printf("\033[0;30m");
		exit(0);
	}
	return 0;
}
