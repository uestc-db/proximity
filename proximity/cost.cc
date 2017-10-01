#include <iostream>
#include <stdio.h>
#include "utility.h"
#include "compute.h"
#include "test.h"
#include "cost.h"

using namespace std;

/** global var **/
Results results;

/* Def: Funcion
 * Func: called in test.cc
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void assignResults(int proxim, int upload, int download){
	results.num_proxim = proxim;
	results.cost_upload = upload;
	results.cost_download = download;
}

/* Def: Funcion
 * Func: Augments occur when a *probe* appeared
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void serverProbeFriend(int friobj,int iter)
{
	if(!flags.locUpload[friobj]){
		results.cost_download+=1;
		results.cost_upload+=1;
		flags.locUpload[friobj]=true;
	}
    if(!flags.mustUpdate[friobj] && !flags.needUpdate[friobj])
        flags.needUpdate[friobj]=true;
}

/* Def: Funcion
 * Func: Augments occur when a *update loc* appeared
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void userUpdateLoc(int curobj)
{
	if(!flags.locUpload[curobj]){
		results.cost_upload+=1;
		flags.locUpload[curobj]=true;
	}
}

/* Def: Funcion
 * Func: Augments occur when a *server update safe region for a user* appeared
 *       yep, the safe region updated 'belong to' the work of server
 *       after calculating out, the safe region will be transfered to the user
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void serverUpdateRegion()
{
	results.cost_download+=1;
}

/* Def: Funcion
 * Func: Augments occur when a *find a match pair* appeared
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void serverFindMatch()
{
	results.cost_download+=2;
	results.num_proxim++;
}

/* Def: Funcion
 * Func: report the cost on the console screen
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void reportCost()
{
	cout<<"cost : "<<results.num_proxim<<"*2\t"<<(results.cost_download-results.num_proxim*2)<<"\t"<<results.cost_upload<<endl;
}
