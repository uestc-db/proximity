#include <cstdio>
#include <string>
#include <vector>
#include <time.h>
#include <cstring>
#include <assert.h>
#include <typeinfo>
#include <float.h>

#include "test.h"
#include "utility.h"
#include "dynamic.h"
#include "STRIPE.h"
#include "FMDCMD.h"
#include "compute.h"

using namespace std;

vector<IntPair> variedPairs;

/* given a friend pair, add it into FriendPairs */
void addToFriendPairs(IntPair a_pair){
	IntPair tmppair;
	tmppair.nodeA = a_pair.nodeA;
	tmppair.nodeB = a_pair.nodeB;

	FriendPairs.push_back(tmppair); //add

	int last_pair_index = FriendPairs.size() - 1;
//	printf("add FriendPairs[%d] = (%d, %d), and the size of FriendPairs: %d\n",
//		last_pair_index, FriendPairs[last_pair_index].nodeA, FriendPairs[last_pair_index].nodeB, FriendPairs.size());
}

/* given a friend pair, add each other into the corresponding two friend lists */
void addToObjectFriends(IntPair a_pair, int iter){
	IntVec* friendsA=objects[a_pair.nodeA].friends;
	IntVec* friendsB=objects[a_pair.nodeB].friends;

	friendsA->push_back(a_pair.nodeB); //add
	friendsB->push_back(a_pair.nodeA); //add
	
	FriendCase fcA, fcB;
	fcA.fri_id = a_pair.nodeB;
	fcB.fri_id = a_pair.nodeA;
	fcA.add_iter = iter;
	fcB.add_iter = iter;
	objects[a_pair.nodeA].friendcases.push_back(fcA);
	objects[a_pair.nodeB].friendcases.push_back(fcB);

	//updateAddedStripes(a_pair, iter);

//	printf("object %d has added friend: %d\n", a_pair.nodeA, friendsA->at(friendsA->size()-1));
//	printf("object %d has added friend: %d\n", a_pair.nodeB, friendsB->at(friendsB->size()-1));
}

double distStr2Str(MovingPoint& curobj, MovingPoint& friobj) {

	int cur_stp_pnt_num = curobj.stripe.points.size();
	int fri_stp_pnt_num = friobj.stripe.points.size();

	double cur_radius = curobj.stripe.radius;
	double fri_radius = friobj.stripe.radius;

	assert(cur_radius != 0 && fri_radius != 0);
	
	//case 1: both stripes are circle
	if (cur_stp_pnt_num == 1 && fri_stp_pnt_num == 1) {
		Point& cur_pnt = curobj.stripe.points[0];
		Point& fri_pnt = friobj.stripe.points[0];

		double p2pdist = computeDistance(cur_pnt.xPos, cur_pnt.yPos, fri_pnt.xPos, fri_pnt.yPos);
		return p2pdist - cur_radius - fri_radius;
	}

	//case 2: cur's stripe is a circle
	if (cur_stp_pnt_num == 1 && fri_stp_pnt_num > 1) {
		double min_p2segdist = FLT_MAX;
		Point& cur_pnt = curobj.stripe.points[0];

		for (int f = 0; f < fri_stp_pnt_num-1; f++) {
			Point& fri_pnt_1 = friobj.stripe.points[f];
			Point& fri_pnt_2 = friobj.stripe.points[f+1];

			double p2segdist = computeDistance(cur_pnt.xPos, cur_pnt.yPos, fri_pnt_1, fri_pnt_2);
			if(p2segdist < min_p2segdist)
				min_p2segdist = p2segdist;
		}
		return min_p2segdist - cur_radius - fri_radius;
	}

	//case 3: fri's stripe is a circle
	if (fri_stp_pnt_num == 1 && cur_stp_pnt_num > 1) {
		double min_p2segdist = FLT_MAX;
		Point& fri_pnt = friobj.stripe.points[0];

		for (int c = 0; c < cur_stp_pnt_num-1; c++) {
			Point& cur_pnt_1 = curobj.stripe.points[c];
			Point& cur_pnt_2 = curobj.stripe.points[c+1];

			double p2segdist = computeDistance(fri_pnt.xPos, fri_pnt.yPos, cur_pnt_1, cur_pnt_2);
			if (p2segdist < min_p2segdist)
				min_p2segdist = p2segdist;
		}
		return min_p2segdist - cur_radius - fri_radius;
	}

	//case 4: both strips have >= 2 points
	double min_seg2segdist = FLT_MAX;
	for(int c = 0; c < cur_stp_pnt_num-1; c++) {
		Point& cur_pnt_1 = curobj.stripe.points[c];
		Point& cur_pnt_2 = curobj.stripe.points[c+1];

		for (int f = 0; f < fri_stp_pnt_num-1; f++) {
			Point& fri_pnt_1 = friobj.stripe.points[f];
			Point& fri_pnt_2 = friobj.stripe.points[f+1];

			double seg2segdist = computeDistance(cur_pnt_1, cur_pnt_2, fri_pnt_1, fri_pnt_2);
			if (seg2segdist < min_seg2segdist)
				min_seg2segdist = seg2segdist;
		}
	}
	return min_seg2segdist - cur_radius - fri_radius;
}


void updateAddedStripes(IntPair a_pair, int iter) {
	if (strcmp(alg_name,"STRIPE") == 0 && iter > START_ITER){
		int node_a = a_pair.nodeA;
		int node_b = a_pair.nodeB;
		MovingPoint& obj_a = objects[node_a];
		MovingPoint& obj_b = objects[node_b];	

		//printf("Added pair %d: %d, %d\n", i, node_a, node_b);

		int stripe_pnt_num = obj_a.stripe.points.size();

		double s2sdist = distStr2Str(obj_a, obj_b);
		
		printf("Added Pair: (%d, %d), distance: %f, epsilon: %f\n", node_a, node_b, s2sdist, eps_value);
		if (s2sdist <= eps_value) {
			serverProbeFriend(node_a, iter);
			serverProbeFriend(node_b, iter);

			flags.mustUpdate[node_a]=true; //set flag            
			flags.mustUpdate[node_b]=true; //set flag

			flags.needUpdate[node_a]=false; //set flag
            flags.needUpdate[node_b]=false; //set flag

			printf ("\tIn updateAddedStripes(): must_upd[%d] = %d, need_upd[%d] = %d, must_upd[%d] = %d, need_upd[%d] = %d\n",
						node_a, flags.mustUpdate[node_a], node_a, flags.needUpdate[node_a],
						node_b, flags.mustUpdate[node_b], node_b, flags.needUpdate[node_b]);

			//updateStripe(obj_a, iter); //rebuild the stripe
			//updateStripe(obj_b, iter); //rebuild the stripe
			//serverUpdateRegion();//server send new region to user
			//serverUpdateRegion();//server send new region to user
		}
	}
}


/* given a friend pair, delete it in FriendPairs */
void delInFriendPairs(IntPair a_pair){

	//find where the pair is
	int loc = 0;
	for(; loc < FriendPairs.size(); loc++){
		IntPair& tmppair = FriendPairs[loc];
		if(tmppair.nodeA == a_pair.nodeA && tmppair.nodeB == a_pair.nodeB || 
			tmppair.nodeA == a_pair.nodeB && tmppair.nodeB == a_pair.nodeA)
			break;
	}

	//to delete
	if(loc == FriendPairs.size()){
		printf("### Error ###: Pair (%d, %d) is not in FriendPairs!\n", a_pair.nodeA, a_pair.nodeB);
		exit(0);
	}
	else {
//		printf("(%d, %d) will be deleted\n", (*(FriendPairs.begin() + loc)).nodeA, (*(FriendPairs.begin() + loc)).nodeB);
		FriendPairs.erase(FriendPairs.begin() + loc);
//		printf("del FriendPairs[]: (%d, %d), and the size of FriendPairs: %d\n", a_pair.nodeA, a_pair.nodeB, FriendPairs.size());
	}
}

/* given a friend pair, delete each other in the corresponding two friend lists */
void delInObjectFriends(IntPair a_pair){
	IntVec* friendsA = objects[a_pair.nodeA].friends;
	IntVec* friendsB = objects[a_pair.nodeB].friends;

	vector<int>::iterator ItA = find(friendsA->begin(), friendsA->end(), a_pair.nodeB);
	vector<int>::iterator ItB = find(friendsB->begin(), friendsB->end(), a_pair.nodeA);

	if(ItA == friendsA->end()){
		printf("### Error ###: object %d has no friend %d\n", a_pair.nodeA, a_pair.nodeB);
		exit(0);
	}
	else {
		friendsA->erase(ItA);
		assert(count(friendsA->begin(),friendsA->end(), a_pair.nodeB) == 0);
//		printf("object %d has delete friend %d\n", a_pair.nodeA, a_pair.nodeB);
	}

	if(ItB == friendsB->end()) 
		printf("### Error ###: object %d has no friend %d\n", a_pair.nodeB, a_pair.nodeA);
	else {
		friendsB->erase(ItB);
		assert(count(friendsB->begin(),friendsB->end(), a_pair.nodeA) == 0);
//		printf("object %d has delete friend %d\n", a_pair.nodeB, a_pair.nodeA);
	}
}

void chooseAndAddFriendPairs(int iter){

	//printf("\n~~~~~~~ Add Friends Pairs ~~~~~~~\n");
	for(int i = 0; i < dyn_edge_num; i++){
		IntPair tmppair;

		while(true){
			srand((unsigned)time(NULL));  
			tmppair.nodeA = rand() % num_objects;
			tmppair.nodeB = rand() % num_objects;

			IntVec* friendsA = objects[tmppair.nodeA].friends;

			// Another same time cost way: a for loop traverse A's friends to find nodeB			
			if(count(friendsA->begin(), friendsA->end(), tmppair.nodeB) == 0) break;				
		}

		//add to corresponding data structures		
		variedPairs.push_back(tmppair);
		addToFriendPairs(tmppair);
		addToObjectFriends(tmppair, iter);
		/*printf("Added Pair %d: (%d, %d)\n", i, tmppair.nodeA, tmppair.nodeB);*/
		//printf("\n");
	}
	//printf("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");	
}

/* randomly to find enough friend pairs to be deleted */
void chooseAndDelFriendPairs(){

	//printf("\n~~~~~~~ Del Friends Pairs ~~~~~~~~\n");
	srand((unsigned)time(NULL)); 
	for(int i = 0; i < dyn_edge_num; i++){
		int find = false;
		IntPair tmppair;

		int nodeA = rand() % num_objects;

		int friends_num = objects[nodeA].friends->size();
		if(friends_num == 0){
			i--;
			continue;
		}
		int B_index = rand()%friends_num;
		int nodeB = objects[nodeA].friends->at(B_index);

		//test nodeA is also nodeB's friend
		for(int i = 0; i < objects[nodeB].friends->size(); i++){
			if(nodeA == objects[nodeB].friends->at(i)){
				find = true;
				break;
			}
		}
		assert(find == true);

		tmppair.nodeA = nodeA;
		tmppair.nodeB = nodeB;

		//modify corresponding data structures
		variedPairs.push_back(tmppair);
		delInFriendPairs(tmppair);
		delInObjectFriends(tmppair);
		//printf("Pair %d: (%d, %d)\n", i, DelPairs[i].nodeA, DelPairs[i].nodeB);
		//printf("\n");
	}
	//printf("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");	
}

/**
 *	The main entry to change the social network
 */
void varySocialNetwork(int iter){

	if (dyn_edge_num <= 0) return;

	// clear the variedPairs vector first
	variedPairs.clear();

	// assertion
	if(strcmp(alg_name, "FMD") == 0) 
		assert(strstr(dyn_type, "fmd") != NULL);
	else if (strcmp(alg_name, "CMD") == 0)
		assert(strstr(dyn_type, "cmd") != NULL);
	else
		assert(strstr(dyn_type, "stripe") != NULL);

	if(strstr(dyn_type, "add") != NULL){
		printf("***** Add edges num: %d\n", dyn_edge_num);
		chooseAndAddFriendPairs(iter);
	}

	if(strstr(dyn_type, "del") != NULL){
		printf("***** Del edges num: %d\n", dyn_edge_num);
		chooseAndDelFriendPairs();
	}

	printf("\nAfter varying, the pairs number of friends: %d\n\n", (int)FriendPairs.size());

}

// ===================================== Next are some updatings for stripes ========================================= //

void updateAddedFMDCMDRegions(bool* isUpdated) {
	//process: 1)check; 2)probe; 3)update safe regions

	for(int i = 0; i < dyn_edge_num; i++) {
		int node_a = variedPairs[i].nodeA;
		int node_b = variedPairs[i].nodeB;
		MovingPoint& obj_a = objects[node_a];
		MovingPoint& obj_b = objects[node_b];

		isUpdated[node_a]=true; //just for test

		// check if the distance between their safe regions is obscure
		// if yes, update their safe regions
		if (getMinDist(obj_a.rect,obj_b.rect) <= eps_value) { // need not check the maxdist between them
			results.cost_download += 2; //two nodes
			results.cost_upload   += 2; //

			isUpdated[node_a]=true;
			isUpdated[node_b]=true;
			updateMobileRegion(obj_a);
			updateMobileRegion(obj_b);
		}
	}
}

void updateDeledFMDCMDRegions(bool* isUpdated) {

}

/* Main function to update stripes in FMD algorithm */
void updateFMDCMDRegions(bool* isUpdated) {
	//1. for the added friend pairs
	if(strstr(dyn_type, "add") != NULL){
		updateAddedFMDCMDRegions(isUpdated);
	}

	//2. for the deleted friend pairs
	if(strstr(dyn_type, "del") != NULL){
		updateDeledFMDCMDRegions(isUpdated);
	}
}


void updateAddedSTRIPERegions(int iter) {
	//process: 1)check; 2)probe; 3)update safe regions

	for(int i = 0; i < variedPairs.size(); i++) {
		int node_a = variedPairs[i].nodeA;
		int node_b = variedPairs[i].nodeB;
		MovingPoint& obj_a = objects[node_a];
		MovingPoint& obj_b = objects[node_b];	

		//printf("Added pair %d: %d, %d\n", i, node_a, node_b);

		int stripe_pnt_num = obj_a.stripe.points.size();

		double s2sdist = distStr2Str(obj_a, obj_b);

		printf("Added Pair %d: (%d, %d), distance: %f, epsilon: %f\n", i, node_a, node_b, s2sdist, eps_value);

		if (s2sdist < eps_value) {
			serverProbeFriend(node_a, iter);
			serverProbeFriend(node_b, iter);

			flags.mustUpdate[node_a]=true; //set flag            
			flags.mustUpdate[node_b]=true; //set flag

			flags.needUpdate[node_a]=false; //set flag
            flags.needUpdate[node_b]=false; //set flag

			printf ("\tIn updateAddedStripes(): must_upd[%d] = %d, need_upd[%d] = %d, must_upd[%d] = %d, need_upd[%d] = %d\n",
						node_a, flags.mustUpdate[node_a], node_a, flags.needUpdate[node_a],
						node_b, flags.mustUpdate[node_b], node_b, flags.needUpdate[node_b]);

			//updateStripe(obj_a, iter); //rebuild the stripe
			//updateStripe(obj_b, iter); //rebuild the stripe
			//serverUpdateRegion();//server send new region to user
			//serverUpdateRegion();//server send new region to user
		}
	}
}

/* Main function to update stripes in STRIPE algorithms */
void updateSTRIPERegions(int iter){
	//1. for the added friend pairs
	if(strstr(dyn_type, "add") != NULL){
		updateAddedSTRIPERegions(iter);
	}

	//2. for the deleted friend pairs
	//if(strstr(dyn_type, "del") != NULL){
	//	updateDeledSTRIPERegions();
	//}
}

