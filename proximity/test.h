#ifndef __TEST
#define __TEST

#include <iostream>
#include <stdio.h>
#include "utility.h"
#include "cost.h"

using namespace std;

/** variables used in the whole project **/
extern char* alg_name;       // FMD/CMD/STRIPE
extern char* prd_name;       // rmf/r2d2/hmm/kalman/perfect
extern char* test_name;      // nf/ns/ms/ev
extern char* dyn_type;       // for FMD/CMD: fmdcmd_add/fmdcmd_del; for STRIPE: stripe_add/stripe_del_no_upd/stripe_del_has_upd

extern int dyn_edge_num;	 // the changed edge num when execute the social graph
extern double lambda;		 // the mobile region radius of FMD/CMD
extern double alpha ;		 // the parameter of CMD

extern int START_ITER;		 // the start epoch of the whole test
extern int max_prednum;		 // the max prediction points read for each object at each timestamp
extern int num_objects;		 // the number of objects in each dataset
extern int base_speed;		 // the base speed setting 

extern double x_left;		 // different to each dataset
extern double x_right;		 // different to each dataset
extern double y_left;		 // different to each dataset
extern double y_right;		 // different to each dataset

extern int num_friends;      // 10/20/30/40/50
extern int num_steps;		 // 300/600/900/1200/1500
extern int mov_speed;		 // 2/4/6/8/10/12/14/16
extern double eps_value;	 // 0.02/0.04/0.06/0.08/0.1

extern char* in_data_file;   // ***.txt
extern char* in_prd_path;    // **/**/
extern char* out_cost_file;  // ***.txt
//extern char* out_count_file; // count_***.txt

/** Function Declaration **/

//void testCore(int num_friends, int num_steps, int mov_speed, double eps_value, 
//			  const char* test_name, double lambda, double alpha, 
//			  const char* data_file, const char* out_file, const char* count_file);
void testCore();
void fillPerfectPred(int iter);

#endif
