#include <iostream>
#include <string.h>
#include <stdio.h>

#include "utility.h"
#include "fileio.h"
#include "test.h"
#include "FMDCMD.h"
#include "STRIPE.h"
#include "dynamic.h"

using namespace std;

/** initialization of global vars defined in test.h **/
char* alg_name   = ""; 
char* prd_name   = ""; 
char* test_name  = ""; 
char* dyn_type   = ""; 

int dyn_edge_num = 0;
double lambda    = 0;
double alpha     = 0;

int START_ITER  = 0;
int max_prednum = 0;
int num_objects = 0;
int base_speed   = 0;

double x_left  = 0;
double x_right = 0;
double y_left  = 0;
double y_right = 0;

int num_friends  = 0; 
int num_steps    = 0;
int mov_speed    = 0;
double eps_value = 0;

char* in_data_file   = "";   
char* in_prd_path	 = "";   
char* out_cost_file  = "";  
//char* out_count_file = ""; 

/* Def: Function 
 * Func: the core of testing, all kinds of variables-tesing could use it 
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void testCore(){

	/*printf("In testCore(), lambda = %f, alpha = %f, base_speed = %d\n", lambda, alpha, base_speed);*/

	// first, generate social network & read the input file & read the integral file
	generateSocialNetwork(num_objects,num_friends); 
	readInputFile(in_data_file, num_steps);
	readIntegral();	
	
	for (int iter=START_ITER; iter < num_steps; iter++) {

		cout<<"********************  " << alg_name << " " << prd_name << " Iteration  "<< iter <<" ********************"<<endl;

		initCounters();

		//at this epoch, get all corresponding data
        advanceAllObjects(iter);

		//maybe vary the social network constructed by generateSocialNetwork()
		if(strcmp(dyn_type, "none") != 0)
			varySocialNetwork(iter);

		//begin proximity cost experiments
		if (strcmp(alg_name,"FMD") == 0) {
			FMDProcess(lambda, iter, eps_value);
		}
		else if (strcmp(alg_name,"CMD") == 0) {
			CMDProcess(iter, eps_value);
		}
		else if (strcmp(alg_name,"STRIPE") == 0) {	
			if (strstr(in_prd_path, "perfect") != NULL) 
				fillPerfectPred(iter);
			else
				readPredFile(iter); // read the predicted point file of the current epoch	

 			STRIPEProcess(iter); /** the main entry of Fix-radiu-Stripe alg **/
			clearPredPoints(); // clear the global variable 'allofPredPoints'
		}
		//printf("\nValues following are about the *current* epoch:\n\n");
		//printf("Out match region (upcost+1):        %d\n", counters.out_match_num);
		//printf("Out safe region (upcost+1):         %d\n", counters.out_stripe_num);
		//printf("Probe (upcost+1, dncost+1):         %d\n", counters.probe_num);
		//printf("Matched (dncost+2):                 %d\n", counters.notify_num);
		//printf("Server construts stripe (dncost+1): %d\n\n", counters.server_notify_stripe_num);

		////printf("num_proxim = %d\n", results.num_proxim);
		//printf("\nValues following are about *start*~*now*: \n\n");
		printf("proxim:     %d\n", results.num_proxim);
		printf("up_cost:    %d\n", results.cost_upload);
		printf("down_cost:  %d\n", results.cost_download);
		printf("total_cost: %d\n", results.cost_upload+results.cost_download);

		if ((iter + 1) % 100 == 0) {
			const char *sep = ".";
			char arrc[200] = { 0 };           //初始化char[] 类型，并具体赋值
			strncpy(arrc, out_cost_file, 200);
			char* p = strtok(arrc, sep);			
			sprintf (out_cost_file, "%s_%d.txt", p, iter+1);
			printf("Before writing, file name is %s\n", out_cost_file);
			writeOutFile(mov_speed, out_cost_file);
		}


		// A trick: the cost of first iter is the most, clearing it could decrease the average cost
		//if (iter==START_ITER){ //first iteration data is not necessary
		//	assignResults(0,0,0);
		//}			
	}
	ClearRegions();
	writeOutFile(mov_speed, out_cost_file);	
}


void fillPerfectPred(int iter){

	cout<<"It's going to fill perfect predicted points."<<endl;

	//init structure
    initialPredVector(iter);

	for(int i = 0; i < num_objects; i++){
		for(int t = 1; t < max_prednum + 5; t++){
			Point predpoint;
			predpoint.xPos = globalRecords[i][iter+t].xPos;
			predpoint.yPos = globalRecords[i][iter+t].yPos;

			assert(predpoint.xPos >= x_left && predpoint.xPos <= x_right);
			assert(predpoint.yPos >= y_left && predpoint.yPos <= y_right);

			allPredPoints[i].push_back(predpoint);
		}
	}

	//fill 'max_prednum' points into 'allPredPoints' for each id
	//for(int i = 0; i < num_objects; i++){
	//	for(int t = 1; t < max_prednum + 5; t++){
	//		Point predpoint;
	//		predpoint.xPos = globalRecords[i][iter+t].xPos;
	//		predpoint.yPos = globalRecords[i][iter+t].yPos;

	//		if(predpoint.xPos < x_left || predpoint.xPos > x_right || predpoint.yPos < y_left || predpoint.yPos > y_right) { //out of bound, a disqualification
	//			int size = allPredPoints[i].size();
	//			predpoint.xPos = allPredPoints[i][size-1].xPos;
	//			predpoint.yPos = allPredPoints[i][size-1].yPos;
	//		}

	//		assert(predpoint.xPos >= x_left && predpoint.xPos <= x_right);
	//		assert(predpoint.yPos >= y_left && predpoint.yPos <= y_right);

	//		allPredPoints[i].push_back(predpoint);
	//	}
	//}

    cout<<"Finished filling."<<endl;
}

