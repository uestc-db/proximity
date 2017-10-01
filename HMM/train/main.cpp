#include "hmm.h"
#include "predict.h"

#include <cstdlib>
#include <iostream>
#include <stdio.h>
#include <vector>

using namespace std;

#define B_MAX 0.000009
#define B_MIN 0.000003
#define DELTA 0.001

int main(){

	int train_id = 2000;
	int train_ts = 1600;
//	int test_id = 2;
//	int test_ts = 200;
//	int his_num = 10;
//	int pred_num = 20;

	char* train_name  = "/home/xuying/proxim/6_pred_models/truck/hmm/data/truck_1w_grids_5s_14to19_1wid_1600ts_train.txt";
//	char* test_name   = "/home/xuying/proxim/6_pred_models/beijing/hmm/cpp/input/sig_grids_2_200_16w_fortest.txt";
	char* mat_a_name  = "/home/xuying/proxim/6_pred_models/truck/hmm/data/mat_a_1w_5s_2kid.txt";
	char* mat_b_name  = "/home/xuying/proxim/6_pred_models/truck/hmm/data/mat_b_1w_5s_2kid.txt";
	char* mat_pi_name = "/home/xuying/proxim/6_pred_models/truck/hmm/data/mat_pi_1w_5s_2kid.txt";
	char* rand_name   = "/home/xuying/proxim/6_pred_models/truck/hmm/data/rand_1w.txt";
//	char* loss_name   = "/home/xuying/proxim/6_pred_models/beijing/hmm/cpp/output/loss.txt";
//	char* loc_name    = "/home/xuying/proxim/6_pred_models/beijing/hmm/cpp/input/test_real.txt";
	

	//training
	train_obs = readInput(train_id, train_ts, train_name);
	printf("In main(), after reading input, train_obs_size = %d\n", train_obs->size());
//	exit(0);
	initValues(rand_name);
	printMatrices();
	
	initLocalVectors();
	printf("In main(), finished initLocalVector()\n");
	trainAll(train_id, DELTA);

	//write training parameters
	writeMatrices(mat_a_name, mat_b_name, mat_pi_name);

	//testing
/*	readCoors(test_id, test_ts, loc_name);
	test_obs = readInput(test_id, test_ts, test_name);
	readMatrices(mat_a_name, mat_b_name, mat_pi_name);
	printMatrices();
	predictAll(test_id, test_ts, his_num, pred_num, loss_name);
*/

//	system("pause");
}


