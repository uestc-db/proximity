#ifndef __HMM__
#define __HMM__

#include <vector>

using namespace std;

extern vector<float> pi;
extern vector<float>* A;
extern vector<float>* B;
extern int M; //grids num
extern int N; //states num
extern int T; //the length of the sequence

extern vector<float> scale;
extern vector<float>* alpha;
extern vector<float>* beta;
extern vector<float>* gama;
extern vector<vector<vector<float> > > xi;
//extern vector<vector<float>* > xi;

void initLocalVectors();
void resetLocalVectors();
float forward(vector<int> cur_obs);
float forwardWithScale(vector<int> obs);
float backward(vector<int> obs);
void backwardWithScale(vector<int> obs);
void computeGamma();
void computeXi(vector<int> obs);
float baumWelch(float DELTA, vector<int> obs, int id);
void trainAll(int train_id, float DELTA);

#endif
