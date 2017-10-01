#ifndef __PREDICT__
#define __PREDICT__

#include <vector>
#include <iostream>

using namespace std;

struct Point{
	float xPos;
	float yPos;
};

extern float xMin;
extern float yMin;
extern float xLen;
extern float yLen;

extern vector<int>* train_obs;
extern vector<int>* test_obs;
extern vector<Point>* location_records;

void checkFile(FILE* fp,const char* filename);
void initValues(const char* rand_name);
void printMatrices();

vector<int>* find_next_pos(vector<int> cur_path, int& valid_pred);
int pred_one_step(vector<int> cur_path);
//vector<int> pred_long_path(vector<int> cur_path, int his_num, int pred_num);
vector<int> pred_long_path(vector<int> cur_path, int pred_num);

void convertCoors(int grid_id, float& real_x, float& real_y);
float getDistance(float lat1, float lon1, float lat2, float lon2);
float computeLoss(vector<int> pred_path, int cur_time, int id, int test_ts, int& loss_count, float& dist, FILE* lossfile);

void predictAll(int test_id, int ts_start, int ts_end, int his_num, int pred_num, const char* loss_name, const char* out_name, const char* time_name);

void readCoors(int id_num, int recs_num, const char* loc_name);
vector<int>* readInput(int id_num, int ts_start, int ts_end, const char* train_name);
void writePrediction(const char* out_name, int id_num, int pred_num, vector<int>* out_list);
void readMatrices(const char* matrix_a, const char* matrix_b, const char* matrix_pi);
void writeMatrices(const char* matrix_a, const char* matrix_b, const char* matrix_pi);

#endif
