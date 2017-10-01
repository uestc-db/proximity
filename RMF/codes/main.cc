#include "test.h"
#include <stdio.h>

int main(int argc, char* argv[]){

	int start_iter = atoi(argv[1]);
	int max_iter   = atoi(argv[2]);
	int pred_num   = atoi(argv[3]);

	num_objects = atoi(argv[4]);

	char* input_file  = argv[5];
	char* output_file = argv[6];

	printf("start_iter  = %d\n", start_iter);
	printf("max_iter    = %d\n", max_iter);
	printf("pred_num    = %d\n", pred_num);
	printf("num_objects = %d\n", num_objects);
	printf("input_file  = %s\n", input_file);
	printf("output_file = %s\n", output_file);

	initObjects();
	ReadMovementFile(input_file, max_iter);

	//generate predict files of RMF
	writePredFiles(start_iter, max_iter, output_file, pred_num);
	return 0;
}
