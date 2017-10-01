#include "hmm.h"
#include "predict.h"
#include <algorithm>
#include <iostream>
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <vector>

using namespace std;

vector<float> pi;
vector<float>* A;
vector<float>* B;
int M = 10000; //grids num
int N = 10; //states num
int T = 50; //the length of the sequence

vector<float> scale(T, 0);
vector<vector<float> > alpha(N, vector<float>(T, 0));
vector<vector<float> > beta(N, vector<float>(T, 0));
vector<vector<float> > gama(N, vector<float>(T, 0));
vector<vector<vector<float> > > xi(N, vector<vector<float>>(N, vector<float>(T-1, 0)));
//int M = 10000; //grids num
//int N = 10; //states num
//int T = 500;; //the length of the sequence

//vector<float> scale;
/*vector<float>* alpha;
vector<float>* beta;
vector<float>* gama;*/
//vector<vector<float> > alpha ;
//vector<vector<float> > beta;
//vector<vector<float> > gama;
//vector<vector<vector<float> > > xi;
//vector<vector<float>* > xi;

void initLocalVectors(){
//	alpha = new vector<float>[N];
//	beta  = new vector<float>[N];
//	gama  = new vector<float>[N];
	//vector<float>* tmp = new vector<float>[N];

/*	for(int n=0; n<N; n++){
		vector<float> tmp1;
		vector<float> tmp2;
		vector<float> tmp3;
		alpha[n].push_back(tmp1);
		beta[n].push_back(tmp2);
		gama[n].push_back(tmp3);

		for(int t=0; t<T; t++){
			alpha[n].push_back(0);
			beta[n].push_back(0);
			gama[n].push_back(0);

		//	if(t < T-1)
			//	tmp[n].push_back(0);
		}
	}

	for(int n=0; n<N; n++){
		vector<vector<float> > tmp1;
		xi.push_back(tmp1);
		for(int p=0; p<N; p++){
			vector<float> tmp2;
			xi[n].push_back(tmp2);
			for(int t=0; t<T-1; t++)
				xi[n][p].push_back(0);
		}
	}

//	for(int n=0; n<N; n++){
//		xi.push_back(tmp);
//	}


//	printf("After initialize, xi[0][0][0]=%f\n", xi[0][0][0]);

	for(int t=0; t<T; t++)
		scale.push_back(0);
*/
	printf("---- Finish initLocalVectors! ----\n");
}

void resetLocalVectors(){
	for (auto& sub : alpha) {
	    std::fill(sub.begin(), sub.end(), 0);
	}
	for (auto& sub : beta) {
	    std::fill(sub.begin(), sub.end(), 0);
	}
	for (auto& sub : gama) {
	    std::fill(sub.begin(), sub.end(), 0);
	}
	for (auto& sub : xi) {
		for (auto& subsub : sub) {
		    std::fill(subsub.begin(), subsub.end(), 0);
		}
	}
}
/*void resetLocalVectors(){
	for(int n=0; n<N; n++){
		for(int t=0; t<T; t++){
			alpha[n][t] = 0;
			beta[n][t] = 0;
			gama[n][t] = 0;
		}
		for(int p=0; p<N; p++)
			for(int t=0; t<T-1; t++)
				xi[n][p][t] = 0;
	}

	for(int t=0; t<T; t++)
		scale[t]=0;	

//	printf("---- Finish resetLocalVectors! ----\n");
}*/


//forward algorithm
float forward(vector<int> cur_obs){
	int obs_len = cur_obs.size();
	float prob = 0;
	
	//step 1: assign the first values of alpha
	for(int n=0; n<N; n++){
		alpha[n][0] = pi[n] * B[n][cur_obs[0]-1];
	}

	//step 2: update alpha at each time and each state
	for(int t=1; t<obs_len; t++){
		for(int n=0; n<N; n++){
			float half_alpha = 0;
			for(int j=0; j<N; j++){
				half_alpha += alpha[j][t-1] * A[j][n];
			}
			alpha[n][t] = half_alpha * B[n][cur_obs[t]-1];			
		}
	}

	//step 3: compute the probability
	for(int n=0; n<N; n++){
		prob += alpha[n][obs_len-1];
	}

	return prob;
}

//forward algorithm
float forwardWithScale(vector<int> obs){
//	cout << "Begin forwardWithScale():" << endl;
	
	float log_prob = 0;
	
	//step 1: assign the first values of alpha
	for(int n=0; n<N; n++){
		alpha[n][0] = pi[n] * B[n][obs[0]-1];
		scale[0] += alpha[n][0];
	}
	for(int n=0; n<N; n++){
		alpha[n][0] /= scale[0];
	}

	//step 2: update alpha at each time and each state
	for(int t=1; t<obs.size(); t++){
		for(int n=0; n<N; n++){
			float half_alpha = 0;
			for(int j=0; j<N; j++){
				half_alpha += alpha[j][t-1] * A[j][n];
			}
			alpha[n][t] = half_alpha * B[n][obs[t]-1];
			scale[t] += alpha[n][t];
		}
		for(int n=0; n<N; n++){
			alpha[n][t] /= scale[t];
		}
	}

	//step 3: compute the probability
	for(int t=0; t<obs.size(); t++){
		log_prob += log(scale[t]);
	}

	//step 4: print out
/*	cout << "\nalpha is:\n[";
    for(int i=0; i<N; i++){
        cout << "[";
        for(int j=0; j<T; j++){
			if(j < 2)
                cout << alpha[i][j] << ", ";
            else if(j > T-3)
                cout << alpha[i][j] << ", ";
            else if(j == 2)
                cout << "......" << ", ";
        }
        cout << "]," <<endl;
    }*/

	return log_prob;
}

float backward(vector<int> obs){
	float prob = 0;

	//step 1: assign the first values of alpha
	for(int n=0; n<N; n++){
		beta[n][T-1] = 1;
	}

	//step 2: update alpha at each time and each state
	for(int t=T-2; t>0; t--){
		for(int n=0; n<N; n++){
			for(int j=0; j<N; j++){
				beta[n][t] += B[j][obs[t+1]-1] * A[n][j] * beta[j][t+1];
			}		
		}
	}

	//step 3: compute the probability
	for(int n=0; n<N; n++){
		prob += beta[n][0];
	}

	return prob;
}

void backwardWithScale(vector<int> obs){

	//step 1: assign the first values of alpha
	for(int n=0; n<N; n++){
		beta[n][T-1] = 1/scale[T-1];
	}

	//step 2: update alpha at each time and each state
	for(int t=T-2; t>=0; t--){
		for(int n=0; n<N; n++){
			for(int j=0; j<N; j++){
				beta[n][t] += B[j][obs[t+1]-1] * A[n][j] * beta[j][t+1];
			}
			beta[n][t] /= scale[t];
		}
	}

	//step 3: print out
/*	cout << "\nbeta is:\n[";
    for(int i=0; i<N; i++){
        cout << "[";
        for(int j=0; j<T; j++){
			if(j < 2)
                cout << beta[i][j] << ", ";
            else if(j > T-3)
                cout << beta[i][j] << ", ";
            else if(j == 2)
                cout << "......" << ", ";
        }
        cout << "]," <<endl;
    }*/
}

void computeGamma(){
	//step 1: calculation
	for(int t=0; t<T; t++){
		float sum = 0;
		for(int n=0; n<N; n++){
			gama[n][t] = alpha[n][t] * beta[n][t];
//			if(t==0)
//				printf("Before norm, gamma[%d][0]=%f\n", n, gama[n][0]);
			sum += gama[n][t];
		}
		for(int n=0; n<N; n++){
			gama[n][t] /= sum;
//			if(t==0)
//				printf("After norm, gamma[%d][0]=%f\n", n, gama[n][0]);
		}
	}

	//step 2: print out
/*	cout << "\ngamma is:\n[";
    for(int i=0; i<N; i++){
        cout << "[";
        for(int j=0; j<T; j++){
			if(j < 2)
                cout << gama[i][j] << ", ";
            else if(j > T-3)
                cout << gama[i][j] << ", ";
            else if(j == 2)
                cout << "......" << ", ";
        }
        cout << "]," <<endl;
    }*/
}

void computeXi(vector<int> obs){
	//step 1: calculation
	for(int t=0; t<T-1; t++){
		float sum = 0;
		for(int i=0; i<N; i++){
			for(int j=0; j<N; j++){
				/*printf("In Xi: alpha[%d][%d]=%f\n", i, t, alpha[i][t]);
				printf("In Xi: A[%d][%d]=%f\n", i, j, A[i][j]);
				printf("In Xi: B[%d][%d]=%f\n", j, obs[t+1]-1, B[j][obs[t+1]-1]);
				printf("In Xi: beta[%d][%d]=%f\n", j, t+1, beta[j][t+1]);*/
				xi[i][j][t] = alpha[i][t] * A[i][j] 
					* B[j][obs[t+1]-1] * beta[j][t+1];
				
				sum += xi[i][j][t];
			}
		}
		for(int i=0; i<N; i++){
			for(int j=0; j<N; j++){
				xi[i][j][t] /= sum;
			}
		}
	}

	//step 2: print out
/*	cout << "\nxi is:\n[";
    for(int i=0; i<N; i++){
        cout << "[";
        for(int j=0; j<N; j++){
			cout << "[";
			for(int k=0; k<T-1; k++){
				if(k < 2)
					cout << xi[i][j][k] << ", ";
				else if(k > T-4)
					cout << xi[i][j][k] << ", ";
				else if(k == 2)
					cout << "......" << ", ";
			}
			cout << "]" <<endl;
        }
        cout << "]," <<endl;
    }*/
}

float baumWelch(float DELTA, vector<int> obs){
	float log_prob_final;
	float log_prob_prev;
	float log_prob_init;
	float log_prob_cur;
	float delta;

	//1. calculate initial parameters
	log_prob_prev = forwardWithScale(obs);
	cout << "log prob from forward_scale: " << log_prob_prev << endl;
	backwardWithScale(obs);
	computeGamma();
	computeXi(obs);
	log_prob_init = log_prob_prev;

	//2. start interative
	while(true){
		/// 1) E-step ///

		//(1) normalize pi
		for(int n=0; n<N; n++){
			pi[n] = 0.001 + 0.999 * gama[n][0];
		}

		//(2) update A
		for(int i=0; i<N; i++){
			float denominator = 0;
			for(int t=0; t<T-1; t++){
				denominator += gama[i][t];
			}
			
			for(int j=0; j<N; j++){
				float numerator = 0;
				for(int t=0; t<T-1; t++){
					numerator += xi[i][j][t];
				}//end t
				A[i][j] = numerator / denominator;
			}//end j
		}//end i

		//(3) normalize A
		for(int i=0; i<N; i++){
			for(int j=0; j<N; j++){
				A[i][j] = 0.001 + 0.999 * A[i][j];
			}//end j
		}//end i

		//(4) update B
		for(int j=0; j<N; j++){
			float denominator = 0;
			for(int p=0; p<T; p++){
				denominator += gama[j][p];
			}//end p
			for(int k=0; k<M; k++){
				float numerator = 0;
				for(int t=0; t<T; t++){
					if(obs[t]-1 == k)
						numerator += gama[j][t];
				}
				B[j][k] = numerator / denominator;
			}//end k
		}//end j

		//(5) normalize B
		for(int i=0; i<N; i++){
			for(int j=0; j<M; j++){
				B[i][j] = 0.001 + 0.999 * B[i][j];
			}//end j
		}//end i

		/// 2) M-step ///
		resetLocalVectors();
		log_prob_cur = forwardWithScale(obs);
		backwardWithScale(obs);
		computeGamma();
		computeXi(obs);

		delta = log_prob_cur - log_prob_prev;
		log_prob_prev = log_prob_cur;
		if(delta <= DELTA)
			break;
	}
	log_prob_final = log_prob_cur;

	return log_prob_final;
}

void trainAll(int train_id, float DELTA){
	for(int i=0; i<train_id; i++){
		
		float log_prob_final = baumWelch(DELTA, train_obs[i]);
		printMatrices();
		resetLocalVectors();
		printf("\nAfter a round, the log_prob: %f, train_id: %d\n", 
				log_prob_final, i);
	}
}
