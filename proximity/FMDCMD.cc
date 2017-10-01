#include <iostream>
#include <cstring>
#include <stdio.h>
#include <math.h>
#include "utility.h"
#include "compute.h"
#include "cost.h"
#include "FMDCMD.h"
#include "dynamic.h"

using namespace std;

int out_circle_index = 0;
int upd_circle_index = 0;

bool VelTrack=true;

/* Def: Funcion
 * Func: check curobj if still in the safe region
 * Author: Ashley_Xu
 * Date: 2016.04
* */
bool withinEstCircle(MovingPoint& curobj,double cur_lambda) {
	// the divide 2 is necessary!!! Or we will pay great cost!!!
    return (computeDistance(curobj.xPos,curobj.yPos,
                            curobj.est_xPos,curobj.est_yPos) <= cur_lambda/2); 
}

/* Def: Funcion
 * Func: check if matched last timestamp, if so, cost will not increase
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void CheckResultStatus(bool isResultPair,int oid,int rel_fid,int iter) {
	MovingPoint& curobj=objects[oid];
	int fid=curobj.friends->at(rel_fid);
	MovingPoint& pairobj=objects[fid];

		// match this timestamp but no the last timestamp, or reverse, update curobj.isPrevResult[rel_fid]
	if (curobj.isPrevResult[rel_fid]!=isResultPair) {
		curobj.isPrevResult[rel_fid]=isResultPair;
			 
		if(isResultPair){				
			results.num_proxim++;  
			results.cost_download+=2; //server send notifications to the two matched objs
			counters.notify_num++;
		}
	}
}

/* Def: Funcion
 * Func: update the mobile region
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void updateMobileRegion(MovingPoint& curobj){
	curobj.est_xPos=curobj.xPos;
    curobj.est_yPos=curobj.yPos;
    curobj.est_xVel=curobj.xVel;
    curobj.est_yVel=curobj.yVel;
}

/* Def: Funcion
 * Func: advance the prediction model
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void advancePredModel() {
	int nlength=objects.size();

	for (int i=0;i<nlength;i++) {
       MovingPoint& curobj = objects[i];
       if (VelTrack) {
			curobj.est_xPos+=curobj.est_xVel;
			curobj.est_yPos+=curobj.est_yVel;
		}
    }
}


/* Def: Funcion
 * Func: the main function of FMD
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void FMDProcess(double box_width,int iter, double epsilon) {
	
	int nlength=objects.size();

    if (iter==START_ITER) {
		// receive location update (and prediction model) from each object
		for (int i=0;i<nlength;i++) {
			MovingPoint& curobj = objects[i];

			results.cost_upload+=1;
            updateMobileRegion(curobj);			
        }
    } else {
        // advance the prediction model
        advancePredModel();
    }

	// initialize the flag array
	bool *isUpdated = new bool[nlength];
	for (int i=0;i<nlength;i++) 
        isUpdated[i]=false;

	if (strcmp(dyn_type, "none") != 0)
		updateFMDCMDRegions(isUpdated);

    // client side: check if location update needed
    // shared prediction model at server side
    for (int i=0;i<nlength;i++) {
        MovingPoint& curobj = objects[i];
		//if (isUpdated[i]) printf ("%d is updated.\n", i);

		//box_width: the given mobile region radius£¬here to check if still in mobile region
		if (!withinEstCircle(curobj, box_width) && !isUpdated[i]) {
			results.cost_upload+=1; //ÉÏ±¨loc
			isUpdated[i]=true;
			updateMobileRegion(curobj);
        }

        // both server and client side, construct prediction model
        fillEstRect(curobj, box_width, curobj.rect);
    }

    // server side processing
    for (int i=0;i<nlength;i++) {
        MovingPoint& curobj=objects[i];

        for (int z=0;z<curobj.friends->size();z++) {
            int j=curobj.friends->at(z);
            if (j<=i) // avoid duplicate check for the same pair!
                continue;

            MovingPoint& neighbor=objects[j];

			bool isMatch=false;

            // first check using the prediction model
            if (getMinDist(curobj.rect,neighbor.rect)>epsilon) // false hit detection
                isMatch=false;
			else {
                if (getMaxDist(curobj.rect,neighbor.rect)<=epsilon) { // true hit detection
					 isMatch=true;
				} else {
					
					// server side: request exact location from client, if not sent before
					for (int w=0;w<2;w++) {
						int wpos=(w==0)? (i):(j);
						if (isUpdated[wpos]==false) {
							results.cost_download+=1;	// one request message to user
							results.cost_upload+=1;		// client update cost
							isUpdated[wpos]=true;

							MovingPoint& tempobj=objects[wpos];

							//update the center of mobile region
							updateMobileRegion(tempobj);
						}
					}

					//compute the exact distance between curobj and neighbor
					double tdist=computeDistance(curobj,neighbor);
					isMatch=(tdist<=epsilon);
				}
			}

			//check if matched last epoch, if yes, server will not send replicate notify
            CheckResultStatus(isMatch,i,z,iter);
        }
    }   

    delete[] isUpdated;
}

void ZMSetWidth(MovingPoint& curobj,float alpha,ZM_EVENT_TYPE event) {
	if (event==EV_UPDATE) {
		curobj.update_cost+=1;
	} else if (event==EV_PROBE) {
		curobj.probe_cost+=2;
	}

	float factor=alpha;
	if (curobj.probe_cost>curobj.update_cost) {

		//if (curobj.update_cost>0)
		//	factor=min(1,curobj.probe_cost/curobj.update_cost);

		curobj.cur_lambda/=factor;

	} else if (curobj.update_cost>curobj.probe_cost) {

		//if (curobj.probe_cost>0)
		//	factor=min(1,curobj.update_cost/curobj.probe_cost);

		curobj.cur_lambda*=factor;
	}
}

/* Def: Funcion
 * Func: the main function of CMD
 * Author: Ashley_Xu
 * Date: 2016.04
* */
// implement VMRECT: dynamic growing/shrinking idea
// how about prob. based model ???
void CMDProcess(int iter, double epsilon) {

	assert(alpha>1.0);

    int nlength=objects.size();
	if (iter == START_ITER) {
        // receive location update (and prediction model) from each object
        for (int i=0;i<nlength;i++) {
            results.cost_upload+=1;
			counters.out_stripe_num++;

            MovingPoint& curobj = objects[i];

//            curobj.cur_lambda=box_width;
			curobj.cur_lambda=lambda;
            updateMobileRegion(curobj);

			// initialization for adjustment parameters
            curobj.acc_factor=1.0;	// reset
			curobj.acc_count=0;
			curobj.update_cost=0;
			curobj.probe_cost=0;
        }
    } else {
        // advance the prediction model
        advancePredModel();
    }

	// initialize the flag array
	bool *isUpdated = new bool[nlength];
	for (int i=0;i<nlength;i++) 
        isUpdated[i]=false;

	if (strcmp(dyn_type, "none") != 0)
		updateFMDCMDRegions(isUpdated);

    // client side: check if location update needed
    // shared prediction model at server side    
    for (int i=0;i<nlength;i++) {

        MovingPoint& curobj = objects[i];

		if (!withinEstCircle(curobj, curobj.cur_lambda) && !isUpdated[i]) {
                results.cost_upload+=1;
				counters.out_stripe_num++;
                isUpdated[i]=true;

               	ZMSetWidth(curobj,alpha,EV_UPDATE);

				updateMobileRegion(curobj);
        }

        // both server and client side
        fillEstRect(curobj, curobj.cur_lambda, curobj.rect);
    }

    // server side processing
    for (int i=0;i<nlength;i++) {
        MovingPoint& curobj=objects[i];

        for (int z=0;z<curobj.friends->size();z++) {
            int slot=curobj.friends->at(z);
            if (slot<=i) // avoid duplicate check for the same pair!
                continue;

            MovingPoint& neighbor=objects[slot];

			bool isResultPair=false;

            // first check using the prediction model
            if (getMinDist(curobj.rect,neighbor.rect) > epsilon)  // false hit detection
                isResultPair=false;
			else {

				if (getMaxDist(curobj.rect,neighbor.rect)<= epsilon) { // true hit detection
					isResultPair=true;
				} else {
					// server side: request exact location from client, if not sent before
					for (int w=0;w<2;w++) {
						int wpos=(w==0)? (i):(slot);
						if (isUpdated[wpos]==false) {
							results.cost_download+=1;	// one request message to user
							results.cost_upload+=1;		// client update cost, client updates the box itself!
							counters.probe_num++;
							isUpdated[wpos]=true;

							MovingPoint& tempobj=objects[wpos];

						/*	if(iter <= 44 && (wpos == 1538 || wpos == 3219))
								printf ("iter = %d, %d is probed, the other one is %d.\n", iter, wpos, wpos==i ? slot : i);*/

							ZMSetWidth(tempobj,alpha,EV_PROBE);

							updateMobileRegion(tempobj);
						}
					}

					double tdist=computeDistance(curobj,neighbor);
					isResultPair=(tdist<=epsilon);
				}
			}

			// server checks the result status, and then deicdes the action
            CheckResultStatus(isResultPair,i,z,iter);
        }
    }

    delete[] isUpdated;
}
