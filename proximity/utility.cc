#include "utility.h"
#include "string.h"
#include <iostream>
#include <float.h> 
#include <math.h>
#include "test.h"

/** definition of objects and FriendPairs **/
vector<IntPair> FriendPairs;
vector<MovingPoint> objects;
vector<LocationRecord>* globalRecords;
vector<Point>* allPredPoints;
vector<IntegralValue> integvalues;
Flags flags;
Counters counters;

/* Def: Function 
 * Func: check the file be opened successfully
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void checkFile(FILE* fp,const char* filename) {
	if (fp==NULL) {
		printf("Invalid file '%s'\n",filename);
		exit(0);
	}
}

/* Def: Function 
 * Func: initialize all the users
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void initObject(MovingPoint& obj) {
	obj.xPos=uniform(0,DOM_SZ);
	obj.yPos=uniform(0,DOM_SZ);

	obj.xVel=0;
	obj.yVel=0;
}

/* Def: Funcion
 * Func: initialize the vector of predict points,
 *		 the first point of each id in the vector is the true loc of the id
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void initialPredVector(int iter) {
	allPredPoints = new vector<Point> [num_objects];

	for (int i = 0; i < num_objects; i++) {
		Point cur_true_point;
		cur_true_point.xPos = globalRecords[i][iter].xPos;
		cur_true_point.yPos = globalRecords[i][iter].yPos;

		allPredPoints[i].push_back(cur_true_point);
	}
}

/* Def: Function 
 * Func: apply space for all flags
 *       initialize all flags with false 
 *       and the size=num_objects
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void initFlags(){
    flags.locUpload=new bool[num_objects];
    flags.mustUpdate=new bool[num_objects];
    flags.needUpdate=new bool[num_objects];
    flags.isUpdated=new bool[num_objects];

    memset(flags.locUpload, 0, sizeof(bool)*num_objects);
    memset(flags.mustUpdate, 0, sizeof(bool)*num_objects);
    memset(flags.needUpdate, 0, sizeof(bool)*num_objects);
    memset(flags.isUpdated, 0, sizeof(bool)*num_objects);
			    
    for (int i=0;i<num_objects;i++) {
        flags.locUpload[i]=false;
        flags.mustUpdate[i]=false;
        flags.needUpdate[i]=false;
        flags.isUpdated[i]=false;
    }
}

/* Def: Function 
 * Func: Free the sapce of all flags 
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void freeFlags(){
	delete[] flags.locUpload;
	delete[] flags.mustUpdate;
	delete[] flags.needUpdate;
	delete[] flags.isUpdated;
}

void initCounters() {
	counters.probe_num = 0;
	counters.out_match_num = 0;
	counters.out_stripe_num = 0;
	counters.notify_num = 0;
	counters.server_notify_stripe_num = 0;
}


/* Def: Funcion
 * Func: called by test.cc
 *       after each iteration, clear all buffer
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void ClearRegions(){
    int user_num=objects.size();
    for (int i=0;i<user_num;i++) {
    	MovingPoint& curobj = objects[i];
	    curobj.matregs.clear();
        curobj.trajectory.clear();
        curobj.predpoints.clear();
        curobj.stripe.points.clear();
    }
}

/* Def: Function 
 * Func:  
 * Author: Ashley_Xu
 * Date: 2016.04
* */
float uniform(float lb,float ub) {
	float value=lb+drand48()*(ub-lb);
	return value;
}

/* Def: Function 
 * Func:  generate the social network for all objects
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void generateSocialNetwork (int nlength,int num_friends) {
	MovingPoint dummy_obj;

	objects.clear();
	for (int i=0;i<nlength;i++)
		objects.push_back(dummy_obj);


	for (int i=0;i<nlength;i++) {
		MovingPoint& curobj=objects[i];
		curobj.id=i;
		curobj.friends=new IntVec();
	}

	// number of pairs =  nlength* num_friends/2
	assert(num_friends<=nlength-1);
	int num_pairs=nlength*num_friends/2;


	FriendPairs.clear();
	for (int z=0;z<num_pairs;z++) {
		bool isUsed=false;
		do {
			isUsed=false;
			int nodeA=rand()%nlength;	// generate two different numbers
			int nodeB=rand()%nlength;

			if (nodeA==nodeB)
				isUsed=true;

			IntVec* friendsA=objects[nodeA].friends;
			IntVec* friendsB=objects[nodeB].friends;
			if (isUsed==false) {
				if (count(friendsA->begin(),friendsA->end(),nodeB)>0)
					isUsed=true;

				if (count(friendsB->begin(),friendsB->end(),nodeA)>0)
					isUsed=true;
			}

			// nodeA!=nodeB, and this friend pair has not been found before
			if (isUsed==false) {
				friendsA->push_back(nodeB);
				friendsB->push_back(nodeA);

				FriendCase fcA, fcB;
				fcA.fri_id = nodeB;
				fcB.fri_id = nodeA;
				fcA.add_iter = START_ITER;
				fcB.add_iter = START_ITER;
				objects[nodeA].friendcases.push_back(fcA);
				objects[nodeB].friendcases.push_back(fcB);

				IntPair tmppair;
				tmppair.nodeA=nodeA;
				tmppair.nodeB=nodeB;
				FriendPairs.push_back(tmppair);
			}
		} while (isUsed==true);
	}

	printf("pairs: %d, %d\n",(int)FriendPairs.size(), num_pairs);
#ifdef DEBUG
	CheckNetwork();
#endif

	// additional init. of structures for strip
	Separator dummy_sep;
	for (int i=0;i<nlength;i++) {
		MovingPoint& curobj=objects[i];

		// update
		curobj.isPrevResult.assign(curobj.friends->size(),false);	// for incremental result report
//		curobj.canComWidth.assign(nlength,true);       // at the beginning, all friends could be used to compute curobj's new width

		curobj.strips=new SeparatorVec();
		for (int z=0;z<curobj.friends->size();z++)
			curobj.strips->push_back(dummy_sep);
	}

	printf("*** social network generated\n");
}

/* Def: Function 
 * Func: called in test.cc, after reading the dataset file,
 *       put the new infos into vector objects 
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void advanceAllObjects(int curstep) {
	int nlength=objects.size();
	for (int i=0;i<nlength;i++) {
		MovingPoint& obj=objects[i];

		// linear bouncing movement function
		// can be replaced by reading data from a file

		LocationRecord& tmprec=globalRecords[i][curstep];
		obj.id=i;
		obj.timestamp=curstep;
		obj.xPos=tmprec.xPos;
		obj.yPos=tmprec.yPos;
		obj.xVel=tmprec.xVel;
		obj.yVel=tmprec.yVel;
		obj.ispredicted=false;
	}
}

/* Def: Function
 * Func: obtain the current loc of the specified user
 *       used to fill up the vacant or abnormal data
 * Author: Ashley_Xu
 * Date: 2016.04
* */
Point getTrueLoc(int obj, int iter)
{
	LocationRecord& tmprec=globalRecords[obj][iter];
	Point point;
	point.xPos=tmprec.xPos;
	point.yPos=tmprec.yPos;
	return point;
}

/* Def: Function
 * Func: obtain the current loc of the specified user
 *       used in another situation 
 * Author: Ashley_Xu
 * Date: 2016.04
* */
Point getObjLoc(MovingPoint& friobj){
    Point point;
    point.xPos=friobj.xPos;
    point.yPos=friobj.yPos;
    return point;
}

/* Def: Function
 * Func: print out some infos of curobj 
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void plot(MovingPoint& obj)
{
	cout<<"----------------------------------"<<endl;
	cout<<obj.id<<":\t";
	for(int i=0;i < obj.stripe.points.size();i++){
		cout<<"("<<obj.stripe.points[i].xPos<<", "<<obj.stripe.points[i].yPos<<") ";
	}
	cout<<" with radius "<<obj.stripe.radius<<"  =>  ";
	for(int i=0;i < obj.predpoints.size();i++){
		cout<<"("<<obj.predpoints[i].xPos<<", "<<obj.predpoints[i].yPos<<") ";
	}
	cout<<endl;
	cout<<"----------------------------------"<<endl;
}
