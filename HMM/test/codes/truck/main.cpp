#include "hmm.h"
#include "predict.h"

#include <iostream>
#include <stdio.h>
#include <vector>

using namespace std;

#define B_MAX 0.000009
#define B_MIN 0.000003
#define DELTA 0.001

int main(int argc, char* argv[]){

//	int train_id      = 1000;
//	int train_ts      = 1600;
	int test_id       = atoi(argv[1]);

	int test_ts_start = atoi(argv[2]);
	int test_ts_end   = atoi(argv[3]);
	int pred_num      = atoi(argv[4]);

	int his_num       = atoi(argv[5]);
	int read_real_rec = atoi(argv[6]);
	int test_ts       = test_ts_end - test_ts_start;

	M=atoi(argv[7]);
	N=atoi(argv[8]);
	T=atoi(argv[9]);
	xMin=atof(argv[10]);
	yMin=atof(argv[11]);
	xLen=atof(argv[12]);
	yLen=atof(argv[13]);

//	char* train_name  = "/home/xuying/proxim/6_pred_models/hmm/cpp/input/sig_grids_1k_1k6_16w_fortrain.txt";
	char* test_name   = argv[14];
	char* loc_name    = argv[15];

	char* mat_a_name  = argv[16];
	char* mat_b_name  = argv[17];
	char* mat_pi_name = argv[18];
	char* rand_name   = argv[19];
	
//	char* loss_name   = "/mnt/disk_root_ori/home/xuying/proxim/6_pred_models/hmm/data/output/1w/aaa";
	char* loss_name   = argv[20];
	char* time_name   = argv[21];
	char* out_file    = argv[22];

	printf("In main: test_id       = %d\n", test_id);
	printf("In main: test_ts_start = %d\n", test_ts_start);
	printf("In main: test_ts_end   = %d\n", test_ts_end);
	printf("In main: pred_num      = %d\n", pred_num);
	printf("In main: his_num       = %d\n", his_num);
	printf("In main: read_real_rec = %d\n", read_real_rec);
	printf("In main: M             = %d\n", M);
	printf("In main: N             = %d\n", N);
	printf("In main: T             = %d\n", T);
	printf("In main: xMin          = %f\n", xMin);
	printf("In main: yMin          = %f\n", yMin);
	printf("In main: xLen          = %f\n", xLen);
	printf("In main: yLen          = %f\n", yLen);
	printf("In main: test_name     = %s\n", test_name);
	printf("In main: loc_name      = %s\n", loc_name);
	printf("In main: mat_a_name    = %s\n", mat_a_name);
	printf("In main: mat_b_name    = %s\n", mat_b_name);
	printf("In main: mat_pi_name   = %s\n", mat_pi_name);
	printf("In main: rand_name     = %s\n", rand_name);
	printf("In main: loss_name     = %s\n", loss_name);
	printf("In main: time_name     = %s\n", time_name);
	printf("In main: out_file      = %s\n", out_file);

	//training
//	train_obs = readInput(train_id, train_ts, train_name);
	initValues(rand_name);
//	printMatrices();
	
	initLocalVectors();
//	trainAll(train_id, DELTA);

	//write training parameters
//	writeMatrices(mat_a_name, mat_b_name, mat_pi_name);

	//testing
	readCoors(test_id, read_real_rec, loc_name);
	test_obs = readInput(test_id, test_ts_start, test_ts_end, test_name);
	readMatrices(mat_a_name, mat_b_name, mat_pi_name);
	printMatrices();
	predictAll(test_id, test_ts_start, test_ts_end, his_num, pred_num, loss_name, out_file, time_name);


//	system("pause");
}


