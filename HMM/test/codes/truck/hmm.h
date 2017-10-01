#ifndef __HMM__
#define __HMM__

#include <vector>

using namespace std;

extern vector<float> pi;
extern vector<float>* A;
extern vector<float>* B;
extern int M; //grids num
extern int N; //states num
extern int T; //the length of the sequence*/
/*extern int M = 10000; //grids num
extern int N = 10; //states num
extern int T = 500; //the length of the sequence*/

/*extern vector<float> scale(T, 0);
extern vector<vector<float> > alpha(N, vector<float>(T, 0));
extern vector<vector<float> > beta(N, vector<float>(T, 0));
extern vector<vector<float> > gama(N, vector<float>(T, 0));
extern vector<vector<vector<float> > > xi(N, vector<vector<float>>(N, vector<float>(T-1, 0)));*/
extern vector<float> scale;
extern vector<vector<float> > alpha;
extern vector<vector<float> > beta;
extern vector<vector<float> > gama;
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
float baumWelch(float DELTA, vector<int> obs);
void trainAll(int train_id, float DELTA);

#endif
