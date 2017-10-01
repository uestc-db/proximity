#include <iostream>
#include <stdio.h>
#include "test.h"

using namespace std;

int main(int argc, char* argv[]){
	
	alg_name  			= argv[1];
	prd_name            = argv[2];
	test_name           = argv[3];
    dyn_type            = argv[4];
	
	dyn_edge_num		= atoi(argv[5]);
	lambda   		    = atof(argv[6]);
	alpha               = atof(argv[7]);
 	
	START_ITER       	= atoi(argv[8]);
	max_prednum       	= atoi(argv[9]);
	num_objects        	= atoi(argv[10]);
	base_speed        	= atof(argv[11]);
	
	x_left         	  	= atof(argv[12]);
	x_right        	  	= atof(argv[13]);
    y_left            	= atof(argv[14]);
	y_right           	= atof(argv[15]);    
						            
	num_friends  	    = atoi(argv[16]);
	num_steps           = atoi(argv[17]);
    mov_speed      		= atoi(argv[18]);
	eps_value      		= atof(argv[19]);
	
	in_data_file   		= argv[20];
	in_prd_path	        = argv[21];
	out_cost_file       = argv[22];
	//out_count_file      = argv[23];

	printf("In main: alg_name      	= %s\n", alg_name);
	printf("In main: prd_name      	= %s\n", prd_name);
	printf("In main: test_name     	= %s\n", test_name);
	printf("In main: dyn_type      	= %s\n", dyn_type);
	printf("\n");
	printf("In main: dyn_edge_num   = %d\n", dyn_edge_num);
	printf("In main: lambda        	= %f\n", lambda);
	printf("In main: alpha         	= %f\n", alpha);
	printf("\n");
	printf("In main: num_objects   	= %d\n", num_objects);
	printf("In main: max_prednum   	= %d\n", max_prednum);
	printf("In main: START_ITER    	= %d\n", START_ITER);
	printf("In main: base_speed    	= %d\n", base_speed);
	printf("\n");	
	printf("In main: x_left        	= %f\n", x_left);
	printf("In main: x_right       	= %f\n", x_right);
	printf("In main: y_left        	= %f\n", y_left);
	printf("In main: y_right       	= %f\n", y_right);
	printf("\n");
	printf("In main: num_friends   	= %d\n", num_friends);
	printf("In main: num_steps     	= %d\n", num_steps);
	printf("In main: mov_speed     	= %d\n", mov_speed);
	printf("In main: eps_value     	= %f\n", eps_value);
	printf("\n");	
	printf("In main: in_data_file   = %s\n", in_data_file);
	printf("In main: in_prd_path	= %s\n", in_prd_path);
	printf("In main: out_cost_file  = %s\n", out_cost_file);
	//printf("In main: out_count_file = %s\n", out_count_file);	

	// run!
	testCore();

	return 0;
}
