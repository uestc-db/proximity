#include "predict.h"
#include "hmm.h"

#include <algorithm>
#include <iostream>
#include <fstream>
#include <stdio.h>
#include <float.h>
#include <time.h>
#include <vector>
#include <math.h>

using namespace std;

#define RANDOM_MAX 10000
#define RAND_SCALE 100000000

float xMin = 21.301;
float yMin = 102;
float xLen = 0.136837;
float yLen = 0.169925;

vector<int>* train_obs;
vector<int>* test_obs;
vector<Point>* location_records;

void checkFile(FILE* fp,const char* filename) {
    if (fp==NULL) {
        printf("Invalid file '%s'\n",filename);
        exit(0);
    }
}

void initValues(const char* rand_name){

	A = new vector<float>[N];
	B = new vector<float>[N];

	//1. initialize matrix A
	float a_ele = 1.0 / N;
	for(int i=0; i<N; i++)
		for(int j=0; j<N; j++)
			A[i].push_back(a_ele);

	//2. initialize matrix B
	float b_ele;
	vector<float> first_list;
	FILE* randfile = fopen(rand_name, "r");
	checkFile(randfile, rand_name);

	float rand_value;
	while(!feof(randfile)){
		fscanf(randfile, "%f\n", &rand_value);
//		printf("%f\n", rand_value);
		first_list.push_back(rand_value);
	}
	fclose(randfile);	

	for(int i=0; i<N; i++){
		random_shuffle(first_list.begin(), first_list.end());
		for(int j=0; j<M; j++){
			B[i].push_back(first_list[j]);
		}
	}
//	printf("The two end of the first two rows: %f %f\n", B[0][M-1], B[1][M-1]);

	//3. initialize array pi
	for(int i=0; i<N; i++)
		pi.push_back(a_ele);
}

void printMatrices(){
	//1. print matrix A
	cout << "\nA is:\n[";
	for(int i=0; i<N; i++){
		cout << "[";
		for(int j=0; j<N; j++){
			cout << A[i][j] << ", ";
		}
		cout << "]," <<endl;
	}

	//2. print matrix B
	cout << "\nB is:\n[";
	for(int i=0; i<N; i++){
		cout << "[";
		for(int j=0; j<M; j++){
			if(j < 2)
				cout << B[i][j] << ", ";
			else if(j > M-3)
				cout << B[i][j] << ", ";
			else if(j == 2)
				cout << "..." << ", ";

		}
		cout << "]," <<endl;
	}

	//3. print array pi
	cout << "\npi is:\n[";
	for(int i=0; i<N; i++){
		cout << pi[i] << ", ";
	}
	cout << "]" <<endl;
}

vector<int>* find_next_pos(vector<int> cur_path, int& valid_pred){
	int path_len = cur_path.size();
	int last_pos = cur_path[path_len-1];
	int cell_num = 9;
	int cols_num = int(sqrt(M));

	vector<int>* paths = new vector<int>[cell_num];
	for(int i=0; i<cell_num; i++){
		for(int j=0; j<path_len+1; j++){
			paths[i].push_back(-1);
		}
	}

	int path_index = 0;
	if(last_pos-1-cols_num > 0)
		for(int j=0; j<path_len; j++)
			paths[path_index][j] = cur_path[j];
		paths[path_index++][path_len] = last_pos-1-cols_num;

	if(last_pos-cols_num > 0)
		for(int j=0; j<path_len; j++)
			paths[path_index][j] = cur_path[j];
		paths[path_index++][path_len] = last_pos-cols_num;

	if(last_pos+1-cols_num > 0)
		for(int j=0; j<path_len; j++)
			paths[path_index][j] = cur_path[j];
		paths[path_index++][path_len] = last_pos+1-cols_num;

	if(last_pos-1 > 0)
		for(int j=0; j<path_len; j++)
			paths[path_index][j] = cur_path[j];
		paths[path_index++][path_len] = last_pos-1;

	if(last_pos+1 <= M)
		for(int j=0; j<path_len; j++)
			paths[path_index][j] = cur_path[j];
		paths[path_index++][path_len] = last_pos+1;

	if(last_pos-1+cols_num <= M)
		for(int j=0; j<path_len; j++)
			paths[path_index][j] = cur_path[j];
		paths[path_index++][path_len] = last_pos-1+cols_num;

	if(last_pos+cols_num <= M)
		for(int j=0; j<path_len; j++)
			paths[path_index][j] = cur_path[j];
		paths[path_index++][path_len] = last_pos-1+cols_num;

	if(last_pos+1+cols_num <= M)
		for(int j=0; j<path_len; j++)
			paths[path_index][j] = cur_path[j];
		paths[path_index++][path_len] = last_pos+1+cols_num;

	for(int j=0; j<path_len; j++)
		paths[path_index][j] = cur_path[j];
	paths[path_index++][path_len] = last_pos;//the last one

	valid_pred = path_index;
	return paths;
}

int pred_one_step(vector<int> cur_path){
	int valid_pred = 0;
	int wanted_grid;
	//*valid_pred = 0;

	float probmax = (-1) * FLT_MAX;
	int max_index = -1;
	int path_len = cur_path.size();

	vector<int>* paths = find_next_pos(cur_path, valid_pred);

	for(int i=0; i<valid_pred; i++){
		vector<int> test_path;
		for(int k=0; k<path_len+1; k++)
			test_path.push_back(paths[i][k]);

		float cur_prob = forward(test_path);
		if(cur_prob >= probmax){
			probmax = cur_prob;
			max_index = i;
		}		
	}

	wanted_grid = paths[max_index][path_len];
	return wanted_grid;
}

vector<int> pred_long_path(vector<int> cur_path, int pred_num){

	vector<int> predposs;

	for(int p=0; p<pred_num; p++){
		int next_pos = pred_one_step(cur_path);
		predposs.push_back(next_pos);
		cur_path.push_back(next_pos);
	}

	return predposs;
}

void convertCoors(int grid_id, float& real_x, float& real_y){
	int cols_num = int(sqrt(M));

	int grid_x = (grid_id-1) / cols_num;
	int grid_y = (grid_id-1) % cols_num;
	real_x = xMin + xLen/2 + xLen*grid_x;
	real_y = yMin + yLen/2 + yLen*grid_y;
}

float computeLoss(vector<int> pred_path, int cur_time, int id, 
				  int test_ts, int& loss_count, FILE* lossfile){
	float sum_loss = 0;
	int loss_num = 0;
	int pred_num = pred_path.size();

	for(int p=0; p<pred_num; p++){
		if(cur_time+p+1 >= test_ts)
			break;

		float pred_x=0, pred_y=0;
		convertCoors(pred_path[p], pred_x, pred_y);
		float real_x = location_records[id][cur_time+p+1].xPos;
		float real_y = location_records[id][cur_time+p+1].yPos;

		//test
		int cols_num = int(sqrt(M));
		int grid_x = (real_x - xMin) / xLen;
		int grid_y = (real_y - yMin) / yLen;
		int id = grid_x * cols_num + grid_y + 1;

		float cur_loss = sqrt(pow((pred_x-real_x),2) + pow((pred_y-real_y),2));
		sum_loss += cur_loss;
		loss_num++;

		fprintf(lossfile, "\tpred_grid: %d\tloss:%f\n", pred_path[p], cur_loss);
	}//end for p

	fprintf(lossfile, "This round, sum_loss: %f\tloss_num: %d\n",
				sum_loss, loss_num);	

	loss_count = loss_num;
	return sum_loss;
}

void predictAll(int test_id, int test_ts, int his_num, int pred_num, const char* loss_name){
	float sum_loss = 0;
	int loss_num = 0;
	char* pred_name = new char[200];

	FILE* lossfile = fopen(loss_name, "a");
	checkFile(lossfile, loss_name);

	//all timestamp, one file for each timestamp
	for(int t=his_num; t<test_ts; t++){
		vector<int>* pred_points = new vector<int>[test_id];		
		sprintf(pred_name, "HMM_singapore_%d.txt", t);

		//begin prediction for each id
		for(int i=0; i<test_id; i++){

			//get the history list used for prediction
			vector<int> history_pts;
			for(int h=t-his_num+1; h<=t; h++)
				history_pts.push_back(test_obs[i][h]);

			//prediction, and put the predicted points into 2-d vector
			vector<int> pred_cur_id = pred_long_path(history_pts, pred_num);
			for(int p=0; p<pred_num; p++)
				pred_points[i].push_back(pred_cur_id[p]);

			//compute the loss
			int nloss = 0;
			float cur_loss = computeLoss(pred_cur_id, t, i, test_ts, nloss, lossfile);
			sum_loss += cur_loss;
			loss_num += nloss;
		}

		//write the corresponding file
		writePrediction(pred_name, test_id, pred_num, pred_points);

		//clear the pred_points
		for(int i=0; i<test_id; i++)
			pred_points[i].~vector();
	}

	//report the loss
	float avg_loss = sum_loss / loss_num;
	fprintf(lossfile, "\nAll predicted num: %d, sum loss: %f, average loss: %f\n",
			loss_num, sum_loss, avg_loss);

	fclose(lossfile);
}

vector<int>* readInput(int id_num, int ts_num, const char* train_name){
	FILE* infile = fopen(train_name, "r");
	checkFile(infile, train_name);

	vector<int>* obs = new vector<int>[id_num];
	
	int read_id, read_ts, read_grid;
	int id_index = -1;
	int last_read_id= -1;
	int times = 0;

	while(!feof(infile)){
		fscanf(infile, "%d %d %d\n", &read_id, &read_ts, &read_grid);
//		printf("Line %d: %d %d %d\n", ++times, read_id, read_ts, read_grid);
		times++;

		if(last_read_id != read_id){
			id_index++;
//			timestamp = 0;
		}

		if(id_index >= id_num)
			break;

		if(last_read_id == read_id && obs[id_index].size() > ts_num)
			continue;

		obs[id_index].push_back(read_grid);
		last_read_id = read_id;
	}

	fclose(infile);
//	printf("read obs, obs[0][2]=%d\n", obs[0][2]);
	//printf("Finish reading! read id: %d, push back times: %d\n obs[1][20]=%d\n",
	//		id_index+1, times, obs[1][20]);

	return obs;
}

void readCoors(int id_num, int ts_num, const char* loc_name){
	FILE* locfile = fopen(loc_name, "r");
	checkFile(locfile, loc_name);

	location_records = new vector<Point>[id_num];
	for(int i=0; i<id_num; i++){
		for(int t=0; t<ts_num; t++){
			Point tmp;
			tmp.xPos = 0; 
			tmp.yPos = 0;
			location_records[i].push_back(tmp);
		}
	}

	int id, ts;
	float xpos, ypos;
	int id_index = -1;
	int last_id = -1;
	int timestamp = 0;

	while(!feof(locfile)){
		fscanf(locfile, "%d %d %f %f\n", &id, &ts, &xpos, &ypos);

		if(id_index >= id_num)
			break;

		if(id == last_id && timestamp >= ts_num)
			continue;

		if(id != last_id){
			id_index++;
			timestamp = 0;
		}

		Point& tmp = location_records[id_index][timestamp];
		tmp.xPos = xpos;
		tmp.yPos = ypos;

		last_id = id;
		timestamp++;
		
	}

	printf("---- Finish reading coors ----");
	//printf("Read over Coordinates. location_records[1][20]: %f %f.\n", 
	//	location_records[1][20].xPos, location_records[1][20].yPos);
}


void writePrediction(const char* out_name, int id_num, int pred_num, 
					 vector<int>* out_list){
	FILE* outfile = fopen(out_name, "a");
	checkFile(outfile, out_name);

	int cols_num = int(sqrt(M));

	for(int i=0; i<id_num; i++){
		for(int j=0; j<pred_num; j++){
			float real_x=0, real_y=0;
			convertCoors(out_list[i][j], real_x, real_y);
			fprintf(outfile, "%d %d %f %f\n", i, j, real_x, real_y);
		}
	}
	fclose(outfile);
}

void readMatrices(const char* matrix_a, const char* matrix_b,
				 const char* matrix_pi){
	FILE* afile = fopen(matrix_a, "r");
	FILE* bfile = fopen(matrix_b, "r");
	FILE* pifile = fopen(matrix_pi, "r");
	checkFile(afile, matrix_a);
	checkFile(bfile, matrix_b);
	checkFile(pifile, matrix_pi);

	//clear them
	for(int i=0; i<N; i++){
		A[i].~vector();
		B[i].~vector();
	}
	pi.~vector();

	//new matrices
	A = new vector<float>[N];
	B = new vector<float>[N];

	int first, second;
	float value;

	//read A
	while(!feof(afile)){
		fscanf(afile, "%d %d %f\n", &first, &second, &value);
		A[first].push_back(value);
	}

	//read B
	while(!feof(bfile)){
		fscanf(bfile, "%d %d %f\n", &first, &second, &value);
		B[first].push_back(value);
	}

	//read pi
	while(!feof(pifile)){
		fscanf(pifile, "%d %f\n", &first, &value);
		pi.push_back(value);
	}

	fclose(afile);
	fclose(bfile);
	fclose(pifile);
}

void writeMatrices(const char* matrix_a, const char* matrix_b,
				 const char* matrix_pi){
	FILE* afile = fopen(matrix_a, "w");
	FILE* bfile = fopen(matrix_b, "w");
	FILE* pifile = fopen(matrix_pi, "w");
	checkFile(afile, matrix_a);
	checkFile(bfile, matrix_b);
	checkFile(pifile, matrix_pi);

	//write A
	for(int i=0; i<N; i++)
		for(int j=0; j<N; j++)
			fprintf(afile, "%d %d %f\n", i, j, A[i][j]);

	//write B
	for(int i=0; i<N; i++)
		for(int j=0; j<M; j++)
			fprintf(bfile, "%d %d %f\n", i, j, B[i][j]);

	//write pi
	for(int i=0; i<N; i++)
		fprintf(pifile, "%d %f\n", i, pi[i]);

	fclose(afile);
	fclose(bfile);
	fclose(pifile);
}

