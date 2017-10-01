#ifndef _COST
#define _COST

#include <iostream>
#include <stdio.h>

using namespace std;

/* Def: struct
 * Func: record the cost and num_proxim
 * Author: Ashley_Xu
 * Date: 2016.04
*/


struct Results{
    int num_proxim;
	int cost_upload;
	int cost_download;

	Results() : num_proxim(0), cost_upload(0), cost_download(0)
	{}
};

/** global var **/
extern Results results;

/** Function declarations **/
void assignResults(int proxim, int upload, int download);
void serverProbeFriend(int friobj,int iter);
void userUpdateLoc(int curobj);
void serverUpdateRegion();
void serverFindMatch();
void reportCost();

#endif
