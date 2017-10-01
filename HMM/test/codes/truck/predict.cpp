#include "predict.h"
#include "hmm.h"

#include <sys/time.h>
#include <algorithm>
#include <iostream>
#include <fstream>
#include <stdio.h>
#include <float.h>
#include <time.h>
#include <vector>
#include <math.h>

using namespace std;

#define PI         3.1415926
#define RANDOM_MAX 10000
#define RAND_SCALE 100000000

float xMin;
float yMin;
float xLen;
float yLen;

int same_num  = 0;
int nine_same = 0;
int all_num   = 0;

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
	if(last_pos-1-cols_num > 0){
		for(int j=0; j<path_len; j++)
			paths[path_index][j] = cur_path[j];
		paths[path_index++][path_len] = last_pos-1-cols_num;
	}

	if(last_pos-cols_num > 0){
		for(int j=0; j<path_len; j++)
			paths[path_index][j] = cur_path[j];
		paths[path_index++][path_len] = last_pos-cols_num;
	}

	if(last_pos+1-cols_num > 0){
		for(int j=0; j<path_len; j++)
			paths[path_index][j] = cur_path[j];
		paths[path_index++][path_len] = last_pos+1-cols_num;
	}

	if(last_pos-1 > 0){
		for(int j=0; j<path_len; j++)
			paths[path_index][j] = cur_path[j];
		paths[path_index++][path_len] = last_pos-1;
	}

	if(last_pos+1 <= M){
		for(int j=0; j<path_len; j++)
			paths[path_index][j] = cur_path[j];
		paths[path_index++][path_len] = last_pos+1;
	}

	if(last_pos-1+cols_num <= M){
		for(int j=0; j<path_len; j++)
			paths[path_index][j] = cur_path[j];
		paths[path_index++][path_len] = last_pos-1+cols_num;
	}

	if(last_pos+cols_num <= M){
		for(int j=0; j<path_len; j++)
			paths[path_index][j] = cur_path[j];
		paths[path_index++][path_len] = last_pos-1+cols_num;
	}

	if(last_pos+1+cols_num <= M){
		for(int j=0; j<path_len; j++)
			paths[path_index][j] = cur_path[j];
		paths[path_index++][path_len] = last_pos+1+cols_num;
	}

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

//	struct timeval start, end;

	vector<int>* paths = find_next_pos(cur_path, valid_pred);

	float last_prob = 0;
        bool all_the_same = true;
	for(int i=0; i<valid_pred; i++){
//		gettimeofday(&start, NULL);

//		vector<int> test_path;
//		for(int k=0; k<path_len+1; k++)
//			test_path.push_back(paths[i][k]);

		float cur_prob = forwardWithScale(paths[i]);
//		float cur_prob = forwardWithScale(test_path);

		resetLocalVectors();

		if(cur_prob >= probmax){
			probmax = cur_prob;
			max_index = i;
		}		
		if(i>0 && all_the_same && last_prob!=cur_prob)
                        all_the_same = false;
                last_prob = cur_prob;

//		vector<int>(test_path).swap(paths[i]);

//		gettimeofday(&end, NULL);
//                int timeuse = 1000000 * (end.tv_sec - start.tv_sec) + end.tv_usec - start.tv_usec;//time count
//		printf("one valid pred: %d us\n", timeuse);
	}
	if(all_the_same)
                nine_same++;

	wanted_grid = paths[max_index][path_len];
	if(wanted_grid == paths[max_index][path_len-1]){
                same_num++;
	}
//	else
//		printf("\nif not same, same_num: %d  the last two grid: %d %d\n", same_num, paths[max_index][path_len-1], wanted_grid);
        all_num++;

//	printf("\nthe same_num: %d  the nine_same: %d  the all_num: %d\n",same_num, nine_same, all_num);

	//clear the paths
	for(int i=0; i<9; i++)
		vector<int>(paths[i]).swap(paths[i]);	

	return wanted_grid;
}

//vector<int> pred_long_path(vector<int> cur_path, int his_num, int pred_num){
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

float getDistance(float lat1, float lon1, float lat2, float lon2){  // generally used geo measurement function
    float R = 6378.137; // Radius of earth in KM
    float dLat = lat2 * PI / 180 - lat1 * PI / 180;
    float dLon = lon2 * PI / 180 - lon1 * PI / 180;
    float a = sin(dLat/2) * sin(dLat/2) +
				cos(lat1 * PI / 180) * cos(lat2 * PI / 180) *
				sin(dLon/2) *sin(dLon/2);
    float c = 2 * atan2(sqrt(a), sqrt(1-a));
    float d = R * c;
    return d * 1000; // meters
}

void predictAll(int test_id, int ts_start, int ts_end, int his_num, 
		int pred_num, const char* loss_name, const char* out_name, const char* time_name){
	char* pred_name = new char[200];

	struct timeval start, end;
	long allid_time = 0, alliter_time = 0;
	FILE* time_file = fopen(time_name, "a");

	//all timestamp, one file for each timestamp
	for(int t=ts_start+his_num; t<ts_end; t++){
		vector<int>* pred_points = new vector<int>[test_id];		
		sprintf(pred_name, "%s%d.txt", out_name, t);
		printf("timestamp: %d\n", t);

		//begin prediction for each id
		for(int i=0; i<test_id; i++){
	
			gettimeofday(&start, NULL);
//			printf("\tid: %d\n", i);

			//get the history list used for prediction
			vector<int> history_pts;
			for(int h=t-ts_start-his_num+1; h<=t-ts_start; h++)
				history_pts.push_back(test_obs[i][h]);

			//prediction, and put the predicted points into 2-d vector
			vector<int> pred_cur_id = pred_long_path(history_pts, pred_num);
//			printf("current id %d has %d pred pts\n", i, pred_cur_id.size());
			for(int p=0; p<pred_num; p++)
				pred_points[i].push_back(pred_cur_id[p]);

			gettimeofday(&end, NULL);
        	        int timeuse = 1000000 * (end.tv_sec - start.tv_sec) + end.tv_usec - start.tv_usec;//time count
			allid_time += timeuse;
//			printf("pred one id: %d us\n", timeuse);
		}

		alliter_time += allid_time;

		float avg_time = allid_time / float(test_id);
		printf("the average time in one iter = %f\n", avg_time);
		fprintf(time_file, "iter %d, avg time: %f\n", t, avg_time);

		allid_time    = 0;

		printf("1111\n");
		//write the corresponding file
		writePrediction(pred_name, test_id, pred_num, pred_points);
		printf("2222\n");

		//clear the pred_points
		for(int i=0; i<test_id; i++)
			pred_points[i].~vector();
	}
	
	float last_avg_time = allid_time / float(test_id);
	fprintf(time_file, "\nIn the last, %d iters, avg time: %f\n", ts_end-ts_start-his_num, last_avg_time);
	fclose(time_file);	
	
}

vector<int>* readInput(int id_num, int ts_start, int ts_end, const char* test_name){
	FILE* infile = fopen(test_name, "r");
	checkFile(infile, test_name);

	vector<int>* obs = new vector<int>[id_num];
	
	int read_id, read_ts, read_grid;
	int id_index = -1;
	int last_read_id= -1;
	int times = 0;

	while(!feof(infile)){
		fscanf(infile, "%d %d %d\n", &read_id, &read_ts, &read_grid);
//		printf("Line %d: %d %d %d\n", ++times, read_id, read_ts, read_grid);
		times++;

		if(read_ts < ts_start || read_ts >= ts_end)
			continue;

		if(last_read_id != read_id){
			id_index++;
//			printf("obs[%d] has %d recs.\n", id_index-1, obs[id_index-1].size());
		}		

		if(id_index >= id_num)
			break;

		obs[id_index].push_back(read_grid);//so the time dim begin with 0
		last_read_id = read_id;
	}

	fclose(infile);
	printf("---- Finish reading grids ----\n");
//	printf("read obs, obs[0][2]=%d\n", obs[0][2]);
	//printf("Finish reading! read id: %d, push back times: %d\n obs[1][20]=%d\n",
	//		id_index+1, times, obs[1][20]);

	return obs;
}

void readCoors(int id_num, int recs_num, const char* loc_name){
	FILE* locfile = fopen(loc_name, "r");
	checkFile(locfile, loc_name);

	location_records = new vector<Point>[id_num];
	for(int i=0; i<id_num; i++){
		for(int t=0; t<recs_num; t++){
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

		if(ts >= recs_num)
			continue;

		if(id != last_id){
//			printf("readCoors: id %d has %d locs\n", id_index, timestamp);
			id_index++;
			timestamp = 0;
		}

		if(id_index >= id_num)
			break;

		//the time dim of location_records also bigin with 0
		Point& tmp = location_records[id_index][timestamp];
		tmp.xPos = xpos;
		tmp.yPos = ypos;

		last_id = id;
		timestamp++;
	}

	printf("---- Finish reading coors ----\n");
	//printf("Read over Coordinates. location_records[1][20]: %f %f.\n", 
	//	location_records[1][20].xPos, location_records[1][20].yPos);
}


void writePrediction(const char* out_name, int id_num, int pred_num, 
					 vector<int>* out_list){
	FILE* outfile = fopen(out_name, "a");
	checkFile(outfile, out_name);
	printf("\nBegin write prediction file %s\n", out_name);

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
	pi.clear();

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

