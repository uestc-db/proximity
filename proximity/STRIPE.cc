#include <iostream>
#include <string.h>
#include <stdio.h>
#include "utility.h"
#include "compute.h"
#include "test.h"
#include "cost.h"
#include "STRIPE.h"
#include "dynamic.h"

#include <float.h> //static region
#include <math.h>
#include <iterator>

using namespace std;

/* Def: Funcion
 * Func: check if the 'fid' matched curobj
 * Author: Ashley_Xu
 * Date: 2016.04
* */
int idToMatchnum(MovingPoint& curobj, int fid){
    int find=INT_MAX;
    for(int i=0;i<curobj.matregs.size();i++){
        if(curobj.matregs[i].match_id==fid){
            find=i;
    	    break;
	    }
    }
    return find;
}

/* Def: Funcion
 * Func: draw a new match region for the matched pair
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void addMatchRegion(MovingPoint& curobj, MovingPoint& friobj){
//	cout<<"add match "<<curobj.id<<"\t"<<friobj.id<<endl;
	MatchRegion curmg,frimg;

	curmg.match_id=friobj.id;
	frimg.match_id=curobj.id;
	curmg.xcenter=frimg.xcenter=(curobj.xPos+friobj.xPos)/2;
	curmg.ycenter=frimg.ycenter=(curobj.yPos+friobj.yPos)/2;
	curmg.lambda=frimg.lambda  =eps_value/2;

	curobj.matregs.push_back(curmg);
	friobj.matregs.push_back(frimg);
}

/* Def: Funcion
 * Func: delete the abandoned region for the matched pair
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void delMatchRegion(MovingPoint& curobj, MovingPoint& friobj, int i,int j){
//	cout<<"delete match "<<curobj.id<<"\t"<<friobj.id<<endl;
	assert(curobj.matregs.size()>0);
	assert(friobj.matregs.size()>0);
	
	curobj.matregs.erase(curobj.matregs.begin()+i);
	friobj.matregs.erase(friobj.matregs.begin()+j);
}

/* Def: Funcion
 * Func: clear the array of predict-points 
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void clearPredPoints(){
	for(int p=0;p<num_objects;p++){
		allPredPoints[p].~vector();
	}
}

/* Def: Funcion
 * Func: check if cur user is in its stripe or not 
 * Author: Ashley_Xu
 * Date: 2016.04
* */
bool outStripe(MovingPoint& curobj){
	//if curobj is in stripe, its coordinate is between two predpoints 
	//and dist from curobj to pointsegment is smaller than su
	bool out=true;

	if(curobj.stripe.points.size()==1){
		double dist=computeDistance(curobj.xPos,curobj.yPos,curobj.stripe.points[0].xPos,curobj.stripe.points[0].yPos);
		if(dist<curobj.stripe.radius)
			out=false;
	}
	else{
		for(int i=1;i<curobj.stripe.points.size();i++){
			Point& point1=curobj.stripe.points[i-1];
			Point& point2=curobj.stripe.points[i];

			double dist=computeDistance(curobj.xPos,curobj.yPos,point1,point2);
			if(dist<curobj.stripe.radius)
				out=false;
		}
	}
	return out; 
}

/* Def: Funcion
 * Func: check if cur user is out of matched region
 * Author: Ashley_Xu
 * Date: 2016.04
* */
bool withinMatchRegion(MovingPoint& curobj, MatchRegion& curmg){
	double dist=computeDistance(curobj.xPos,curobj.yPos,curmg.xcenter,curmg.ycenter);
	return (computeDistance(curobj.xPos,curobj.yPos,
				curmg.xcenter,curmg.ycenter)<=curmg.lambda);
}

/* Def: Funcion
 * Func: check if cur user is out of matched regions
 *       only called in FixRadiusStripe()
 * Author: Ashley_Xu
 * Date: 2016.04
* */
bool outMatchRegion(MovingPoint& curobj)
{
	for(int j=0;j<curobj.friends->size();j++){
		int fid=curobj.friends->at(j);
		if(idToMatchnum(curobj,fid)>=curobj.matregs.size())
			continue;//match_region has been delete?  chenxinyuan 

		int m1=idToMatchnum(curobj,fid);	
		MatchRegion& curmg=curobj.matregs[m1];

		if(!withinMatchRegion(curobj,curmg)){
			return true;
		}
	}
	return false;
}

/* Def: Funcion
 * Func: Scan all the match regions with respect to friends.
 *       If the user is within the match region, no action is required.
 *       Otherwise, it requires updating the location of matching friend
 * Author: Ashley_Xu
 * Date: 2016.04
* */
bool updateMatchRegion(MovingPoint& curobj, int iter)
{
    bool region_update=false;
    for(int j=0;j<curobj.friends->size();j++){
		int fid=curobj.friends->at(j);
        MovingPoint& friobj=objects[fid];
		if(idToMatchnum(curobj,fid)>=curobj.matregs.size())
			continue; 

		int m1=idToMatchnum(curobj, fid);	
        int m2=idToMatchnum(friobj,curobj.id);
		MatchRegion& curmg=curobj.matregs[m1];
        MatchRegion& frimg=friobj.matregs[m2];

		if(!withinMatchRegion(curobj,curmg) || 
            !withinMatchRegion(friobj,frimg)){
            
            serverProbeFriend(friobj.id,iter);//cost+ 
			counters.probe_num++;
			double p2pdist=computeDistance(curobj, friobj);
			if(p2pdist <= eps_value){
                delMatchRegion(curobj, friobj, m1, m2);//delete the old
				addMatchRegion(curobj, friobj);
//				serverUpdateRegion();//compute the new saferegion and transport to user .chenxinyuan
			}
            else {
                delMatchRegion(curobj,friobj,m1,m2);
            }
		}
	}
	return region_update;
}

/* Def: Funcion
 * Func: Get an upper bound of the maximum number of steps for the following pointiction
 *       And here, we should consider if there may be some matching
 * Author: Ashley_Xu
 * Date: 2016.04
* */
int getMaxPred(MovingPoint& curobj, int iter)
{		
	int pointnum = min(max_prednum, curobj.predpoints.size());
    int lastpointnum=pointnum;//used in every friend
	assert(lastpointnum > 0);
	if(lastpointnum == 1) {
		for(int i=0;i<curobj.friends->size();i++){
			int fid=curobj.friends->at(i);
			MovingPoint& friobj=objects[fid];
        
			if(idToMatchnum(curobj,fid)<curobj.matregs.size())
				continue;

			bool maymatch = false;

			//case 1: friobj is only a naked point
			if((flags.mustUpdate[fid] || flags.needUpdate[fid])
				&& !flags.isUpdated[fid]){
					double p2pdist = computeDistance(curobj, friobj);
					if(p2pdist < eps_value){
						maymatch = true;
					}
			}

			//case 2: friobj's stripe is a circle
			else if(friobj.stripe.points.size() == 1){
				double p2strdist = computeDistance(curobj.predpoints[0].xPos,
												   curobj.predpoints[0].yPos, 
												   friobj.predpoints[0].xPos,
												   friobj.predpoints[0].yPos)
									- friobj.stripe.radius;
				if(p2strdist < eps_value){
					maymatch = true;
				}
			}

			//case 3: friobj's stripe contains many points
			else {
				assert(friobj.stripe.points.size() > 1);

				for(int s=1;s<friobj.stripe.points.size();s++){
					Point fp1 = friobj.stripe.points[s-1];
					Point fp2 = friobj.stripe.points[s];

					double p2segdist = computeDistance(curobj.predpoints[0].xPos,
														curobj.predpoints[0].yPos,
														fp1,fp2);
					double p2strdist = p2segdist - friobj.stripe.radius;
					if(p2strdist < eps_value){
						maymatch = true;
						break;
					}
				}
			}

			//to check if it is really match
			if(maymatch && flags.mustUpdate[curobj.id]){
				serverProbeFriend(fid,iter);
				counters.probe_num++;

				double p2pdist = computeDistance(curobj,friobj);

				if(p2pdist < eps_value){
					serverFindMatch();
					counters.notify_num++;
//					printf("match: %d %d\n", curobj.id, friobj.id);
					addMatchRegion(curobj, friobj);
				}
			}
		}

		return 0;
	}

    //Now, compute the distance from friobj's stripe to curobj's segments,
    //to find out the last legal predpoint of curobj
	for(int i=0;i<curobj.friends->size();i++){
		int fid=curobj.friends->at(i);
		MovingPoint& friobj=objects[fid];
		if(idToMatchnum(curobj,fid)<curobj.matregs.size())
    		continue; 
        
        int minnum_1f = INT_MAX; //record the min point num of curobj's predpoints in this iteration
        bool maymatch = false; //a flag, if true means the two friends may be matched

        //case 1: friobj is only a naked point
        if((flags.mustUpdate[fid] || flags.needUpdate[fid])
           && !flags.isUpdated[fid]){
            for(int p=1;p<pointnum;p++){
                Point p1 = curobj.predpoints[p-1];
                Point p2 = curobj.predpoints[p];
                double p2segdist = computeDistance(friobj.xPos, friobj.yPos, p1, p2);
                if(p2segdist < eps_value){
                    if(p==1)
                        maymatch = true;

                    minnum_1f = p-1;
                    break;
                }
                minnum_1f = p; //all pred points are legal
            }       
        }

        //case 2: friobj's stripe is a circle
        else if(friobj.stripe.points.size() == 1){
            for(int p=1;p<pointnum;p++){
                Point p1 = curobj.predpoints[p-1];
                Point p2 = curobj.predpoints[p];
    
                //compute the distance from the circle to one of curobj's segments
                double p2segdist = computeDistance(friobj.stripe.points[0].xPos,
                                                    friobj.stripe.points[0].yPos,
                                                  p1, p2);
                double cir2segdist = p2segdist - friobj.stripe.radius;
                if(cir2segdist < eps_value){
                    if(p==1)
                        maymatch = true;

//                    printf("iter %d, getMaxPred(), case 2, cur:%d, mustUpdate=%d, fri:%d, p=%d, cir2segdist=%f, maymatch=%d.\n",
//                           iter, curobj.id, flags.mustUpdate[curobj.id], fid, p, cir2segdist, maymatch);
                    minnum_1f = p-1;
                    break;
                }
                minnum_1f = p;//all pred points are legal
            }
        }

        //case 3: friobj's stripe contains many points
        else {
            assert(friobj.stripe.points.size() > 1);

            for(int s=1;s<friobj.stripe.points.size();s++){
                Point fp1 = friobj.stripe.points[s-1];
                Point fp2 = friobj.stripe.points[s];

                int minnum_1sp = INT_MAX;

                for(int p=1;p<pointnum;p++){
                    Point cp1 = curobj.predpoints[p-1];
                    Point cp2 = curobj.predpoints[p];

                    //compute distance between the two segments
                    double seg2segdist = computeDistance(cp1, cp2, fp1, fp2);
                    double str2segdist = seg2segdist - friobj.stripe.radius;

                    if(str2segdist < eps_value){
                        if(p==1)
                            maymatch = true;

                        minnum_1sp = p-1;
                        break;
                    }
                    minnum_1sp = p;//all pred points are legal in current friobj's segment
                }
                minnum_1f = min(minnum_1f, minnum_1sp);
                if (maymatch)
                    break;
            }
        }

        //situation: maybe matched
        if(maymatch && flags.mustUpdate[curobj.id]){
            serverProbeFriend(fid,iter);
			counters.probe_num++;

            double p2pdist = computeDistance(curobj,friobj);
            if(p2pdist < eps_value){
                serverFindMatch();
				counters.notify_num++;
				//printf("match: %d %d\n", curobj.id, friobj.id);
				addMatchRegion(curobj, friobj);
            }
        }
        lastpointnum = min(lastpointnum, minnum_1f);
    }

    return lastpointnum;
}

/* Def: Funcion
 * Func: estimate the value of Em
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void estimateEm(MovingPoint& curobj, double su, float p, int prednum)
{
    double curvel = sqrt(pow(curobj.xVel, 2) + pow(curobj.yVel, 2));
    curobj.Em = su/curvel + (p*(1-pow(p,prednum)))/(1-p);
}

/* Def: Funcion
 * Func: estimate the value of Ep
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void estimateEp(MovingPoint& curobj, double su, float p, int prednum, int iter)
{
	curobj.Ep = FLT_MAX;

    for(int i=0; i<curobj.friends->size(); i++){
        int fid=curobj.friends->at(i);
        if(idToMatchnum(curobj,fid) < curobj.matregs.size())
            continue;

        MovingPoint& friobj=objects[fid];
        double frivel=sqrt(pow(friobj.xVel,2)+pow(friobj.yVel,2));

        double y = initSu(curobj, iter, prednum) + eps_value;
        double tmpEp=(y-su-eps_value)/frivel;
        curobj.Ep = min(curobj.Ep,tmpEp);           
    }
}

/* Def: Funcion
 * Func: calculate the distance between two stripes
 * Author: Ashley_Xu
 * Date: 2016.04
* */
double calS2SDistance(int iter, int fid, int prednum, MovingPoint& curobj, bool dynamic) {
	double tmpdist = FLT_MAX;  //record the mindist of current friends.

	MovingPoint& friobj=objects[fid];

	//case 1: friobj is only a naked point
    if((flags.mustUpdate[fid] || flags.needUpdate[fid])
        && !flags.isUpdated[fid]){

        if(prednum == 1){
            tmpdist = computeDistance(curobj, friobj);
            if(tmpdist<eps_value){
                printf("iter %d, cur:%d, fri:%d, case 1, p2pdist=%f.\n",iter,curobj.id,fid,tmpdist);
            }
            if(!dynamic) assert(tmpdist>=eps_value);
        }
        else {
            assert(prednum > 1);
            for(int p=1;p<prednum;p++){
                Point p1 = curobj.predpoints[p-1];
                Point p2 = curobj.predpoints[p];
                double p2segdist = computeDistance(friobj.xPos, friobj.yPos, p1, p2);
                    
//                    if(iter == START_ITER+1)
//                      fprintf(files.estimate_file, "\t case 1: p = %d, p2segdist = %f.\n", p, p2segdist);

                if(p2segdist<eps_value){
                    printf("iter %d, cur:%d, fri:%d, case 1, p2segdist=%f.\n",iter,curobj.id,fid,p2segdist);
                }
               if(!dynamic) assert(p2segdist>=eps_value);
                tmpdist = min(tmpdist, p2segdist);
            }
        }
    }

    //case 2: friobj's stripe is a circle
    else if(friobj.stripe.points.size() == 1){
        if(prednum == 1){
            tmpdist = computeDistance(curobj.predpoints[0].xPos,
                                        curobj.predpoints[0].yPos, 
                                        friobj.predpoints[0].xPos,
                                        friobj.predpoints[0].yPos)
                        - friobj.stripe.radius;
			if(!dynamic) {
				if(tmpdist<eps_value){
					printf("\niter %d, cur:%d, fri:%d, case 2, cc2ccdist=%f.\n",iter,curobj.id,fid,tmpdist);

					for (int x=0; x<curobj.friendcases.size(); x++) {
						if (fid == curobj.friendcases[x].fri_id)
							printf("\n%d became %d's friend at %d\n", fid, curobj.id, curobj.friendcases[x].add_iter);
					}
					for (int x=0; x<friobj.friendcases.size(); x++) {
						if (curobj.id == friobj.friendcases[x].fri_id)
							printf("\n%d became %d's friend at %d\n", curobj.id, fid, friobj.friendcases[x].add_iter);
					}
					
					printf("\ndist between %d and %d is %f, fri's radius=%f.\n",curobj.id,fid,
							computeDistance(curobj, friobj),friobj.stripe.radius);
					printf("\nIf they being matched this iter? %d\n", idToMatchnum(curobj,fid) < curobj.matregs.size());
					printf("\ndist between them at the iter they constructed stripes is %f.\n",
						computeDistance(globalRecords[curobj.id][curobj.stripe.updateiter].xPos, globalRecords[curobj.id][curobj.stripe.updateiter].yPos,
						globalRecords[fid][friobj.stripe.updateiter].xPos, globalRecords[fid][friobj.stripe.updateiter].yPos));
					printf("\ncur's mustUpdate=%d, cur's needupdate=%d, fri's mustUpdate=%d, fri's needupdate=%d.\n",
							flags.mustUpdate[curobj.id],flags.needUpdate[curobj.id],
							flags.mustUpdate[fid],flags.needUpdate[fid]);
					printf("\ncur's updateiter=%d, fri's updateiter=%d.\n",
							curobj.stripe.updateiter,friobj.stripe.updateiter);
					printf("\ncur's friends: \n");
					for (int p=0; p<curobj.friends->size(); p++){
						int cur_fid = curobj.friends->at(p);
						printf("%d: must_update=%d, need_update=%d, is_updated=%d\n", 
							cur_fid, flags.mustUpdate[cur_fid],flags.needUpdate[cur_fid], flags.isUpdated[cur_fid]);
						
					}
					printf("\n");
				}
				assert(tmpdist>=eps_value);
			}
        }
        else {
            assert(prednum>1);
            for(int p=1;p<prednum;p++){
                Point p1 = curobj.predpoints[p-1];
                Point p2 = curobj.predpoints[p];

                //compute the distance from the circle to one of curobj's segments
                double p2segdist = computeDistance(friobj.stripe.points[0].xPos,
                                                friobj.stripe.points[0].yPos,
                                                p1, p2);

//                if(iter == START_ITER+1)
    //                  fprintf(files.estimate_file, "\t case 2: p = %d, p2segdist = %f.\n", p, p2segdist);
                double cir2segdist = p2segdist - friobj.stripe.radius;

				if(!dynamic){
					if(cir2segdist<eps_value){
						printf("iter %d, cur:%d, fri:%d, case 2, cir2segdist=%f.\n",iter,curobj.id,fid,cir2segdist);
					}
					assert(cir2segdist>=eps_value);
				}
                tmpdist = min(tmpdist,cir2segdist);
            }
        }
    }

    //case 3: friobj's stripe contains many points
    else {
        assert(friobj.stripe.points.size() > 1);

        for(int s=1;s<friobj.stripe.points.size();s++){
            Point fp1 = friobj.stripe.points[s-1];
            Point fp2 = friobj.stripe.points[s];

            double tmpdist_tmp = FLT_MAX;

            if(prednum == 1){
                double p2segdist = computeDistance(curobj.predpoints[0].xPos,
                                                    curobj.predpoints[0].yPos,
                                                    fp1,fp2);
                double p2strdist = p2segdist - friobj.stripe.radius;

				if(!dynamic){
					if(p2strdist<eps_value){
						printf("iter %d, cur:%d, fri:%d, case 3, p2strdist=%f.\n",iter,curobj.id,fid,p2strdist); 
						printf("the accident points of friobj are %d, %d.\n",s-1,s); 
						printf("p2segdist=%f, friobj.stripe.radius=%f.\n",p2segdist,friobj.stripe.radius); 
					}
					assert(p2strdist>=eps_value);
				}

                tmpdist = min(tmpdist,p2strdist);
            }
            else {
                for(int p=1;p<prednum;p++){
                    Point cp1 = curobj.predpoints[p-1];
                    Point cp2 = curobj.predpoints[p];

                    //compute distance between the two segments
                    double seg2segdist = computeDistance(cp1, cp2, fp1, fp2);
                    double str2segdist = seg2segdist - friobj.stripe.radius;

					if(!dynamic){
						if(str2segdist<eps_value){
							printf("iter %d, cur:%d, fri:%d, case 3, seg2segdist=%f, fri.stripe.r=%f, cur: %d, cp1: %f, %f, \
									cp2: %f, %f; fri: %d, fp1: %f, %f, fp2: %f, %f\n",iter,curobj.id,fid,seg2segdist, 
									friobj.stripe.radius, p, cp1.xPos, cp1.yPos, cp2.xPos, cp2.yPos, s, fp1.xPos, fp1.yPos, 
									fp2.xPos, fp2.yPos);
						}
						assert(str2segdist>=eps_value);
					}

                    tmpdist_tmp = min(tmpdist_tmp, str2segdist);
                }
                tmpdist = min(tmpdist, tmpdist_tmp);
            }
        }
    }

	return tmpdist;
}


/* Def: Funcion
 * Func: initialize the radius of stripe
 * Author: Ashley_Xu
 * Date: 2016.04
* */
double initSu(MovingPoint& curobj, int iter, int prednum){
//    printf("iter %d, user %d starts to update safe region.\n",iter, curobj.id);

    double radius = FLT_MAX;

    //compute the distance from friobj's stripe to curobj's segments
    for(int i=0;i<curobj.friends->size();i++){
        int fid=curobj.friends->at(i);        
        
        if(idToMatchnum(curobj,fid) < curobj.matregs.size())
            continue;

		double tmpradius; 
        double tmpdist = calS2SDistance(iter, fid, prednum, curobj, false);

		if((flags.mustUpdate[fid] || flags.needUpdate[fid]) && !flags.isUpdated[fid])
			tmpradius = (tmpdist - eps_value)/2;
		else
			tmpradius = tmpdist - eps_value;

        radius = min(radius, tmpradius);
    }
    return radius;
}

/* Def: Funcion
 * Func: read the predpoints from the global 'allPredPoints'
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void readCurrentPred(MovingPoint& curobj,int maxnum,int iter){
    int cur_pred_num = allPredPoints[curobj.id].size();
    for(int n=0; n<cur_pred_num; n++){
        Point& point=allPredPoints[curobj.id][n];
        assert(point.xPos <= x_right && point.xPos >= x_left);
        assert(point.yPos <= y_right && point.yPos >= y_left);

        curobj.predpoints.push_back(point);
    }
}

/* Def: Funcion
 * Func: Updating the stripe of users 
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void updateStripe(MovingPoint& curobj, int iter)
{
//    printf("iter %d, user %d starts to update safe region.\n",iter, curobj.id);
    //some values used for estimating the Em and Ep 
    //double p = 0.9;
    float p;
    double bias = 0.3;
    double theta = 0.1;
    int best_pred_num;
    double best_su;
    double max_e = 0;

    //clear curobj's stripe and fill in the first point
    curobj.stripe.radius=0;
    curobj.stripe.points.clear();

    //clear curobj's predpoints
    curobj.predpoints.clear();

    //read predpoints from predfile
    readCurrentPred(curobj, max_prednum, iter);

    //obtain the legal max number of points 
	int max_pred_num = getMaxPred(curobj, iter);
    best_pred_num = (max_pred_num==0) ? 1 : max_pred_num;
    best_su = initSu(curobj, iter, best_pred_num);

	//update the points in stripe
    for(int i = 0; i < best_pred_num; i++){
		Point point = curobj.predpoints[i];

		assert(point.xPos <= x_right && point.xPos >= x_left);
        assert(point.yPos <= y_right && point.yPos >= y_left);
		
		curobj.stripe.points.push_back(point);
    }

    //-------------- update ------------------
    flags.isUpdated[curobj.id]=true;
	curobj.stripe.radius=best_su;
    curobj.stripe.updateiter=iter;
}

/* Def: Funcion
 * Func: print out the stripe infomation for each user 
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void reportStripeInfo(int iter){
    int num_object=objects.size();

    for(int i=0;i<num_object;i++){
        if(iter==0 || (iter>0 && !flags.isUpdated[i]))
            continue;

        MovingPoint& curobj=objects[i];
        int num=curobj.stripe.points.size();
        double radius=curobj.stripe.radius;
    }
}

/* Def: Funcion
 * Func: initSrtipe, mainly calls the updateStripe()
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void initSrtipe(int iter)
{
    int bestM;
    // First, all the users report their locations to the server
    for (int i=0;i<num_objects;i++) {
        userUpdateLoc(objects[i].id);
		counters.out_stripe_num++;
    }
    
    //init mustUpdate flag
    for (int i=0;i<num_objects;i++) {	
        flags.mustUpdate[i]=true;
    }

    //all points update stripe
    for (int i=0;i<num_objects;i++) {
        MovingPoint& curobj = objects[i];
        updateStripe(curobj, iter);
        serverUpdateRegion();//server send new region to user
		counters.server_notify_stripe_num = 0;
    }
}


/*
 * The main entry for Fix-Radius algorithm.
 *
 */
void STRIPEProcess(int iter){

    //init flags
    initFlags();

    //when the first iteration
    if (iter == START_ITER) {
        initSrtipe(iter);
        return;
    }
	
	// if dynamic, update the varied stripes
	if (strcmp(dyn_type, "none") != 0)
		updateSTRIPERegions(iter);

    //step 1: check if go out match regions
    for(int i=0;i < num_objects;i++){
        MovingPoint& curobj = objects[i];

        // monitor its distance to the matching friends in last iter
        int match_num = curobj.matregs.size();
        if(match_num > 0){
  
			bool is_out_match = outMatchRegion(curobj);
            if(is_out_match){
                userUpdateLoc(curobj.id); //cost++
				counters.out_match_num++;
                flags.mustUpdate[i]=true; //set flag
                flags.needUpdate[i]=false; //set flag

                updateMatchRegion(curobj, iter); //rebuild the outdated 
            }
        }
    }
	//printf("$$$ Safe to pass the match-region checking.\n");


    //step 2: check who is out of its stripe
    for(int i=0;i < num_objects;i++){
        MovingPoint& curobj = objects[i];

		bool is_out_stripe = outStripe(curobj);
        if(is_out_stripe){
            userUpdateLoc(curobj.id); //cost++
			counters.out_stripe_num++;
            flags.mustUpdate[i]=true; //set flag
            flags.needUpdate[i]=false; //set flag
        }
    }
	//printf("$$$ Safe to pass the safe-region checking.\n");

    //step 3: update users who are out of safe region or match region, and may set update flag to friends
    for(int i=0;i < num_objects;i++){
		if (!flags.mustUpdate[i] || flags.isUpdated[i]) 
            continue;

        MovingPoint& curobj = objects[i];
		updateStripe(curobj, iter); //rebuild the stripe
        serverUpdateRegion();//server send new region to user
		counters.server_notify_stripe_num++;
    }
	//printf("$$$ Safe to pass the MUST safe-region updating.\n");

    //step 4: update friends' safe region
    for(int i=0;i < num_objects;i++){
        if(!flags.needUpdate[i] || flags.isUpdated[i])
            continue;

        assert(!flags.mustUpdate[i]);

        MovingPoint& curobj = objects[i];
        updateStripe(curobj, iter); //rebuild the stripe
        serverUpdateRegion();//server send new region to user
		counters.server_notify_stripe_num++;
    }

//    reportCost();
    reportStripeInfo(iter);
    freeFlags();
    
    //fileio.cc, write some info about one abnormal user and his friends, then draw a series of variation diagram for them
    void writeDrawFile(int iter);
}

