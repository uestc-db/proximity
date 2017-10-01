#ifndef __UTILITY
#define __UTILITY

#include <stdlib.h>
#include <stdio.h>
#include <math.h>

#include <vector>
#include <deque>
#include <map>
#include <set>
#include <limits>
#include <bitset>
#include <queue>
#include <algorithm>

#include <iostream>
#include <fstream>
#include <assert.h>
#include <time.h>
#include <limits.h>

using namespace std;

#define FastArray	vector
#define FastList	deque
#define BitStore	vector<bool>

#define DOM_SZ (1000.0)
#define DIMENSION (2)
#define FRIENDSIZE 30

#ifdef _WIN32
#define PLATFORM 	("WIN32")
#define srand48 	(srand)
#define drand48() 	(rand()/(float)RAND_MAX)
#else
#define PLATFORM 	("UNIX")
#endif

typedef vector<float> FloatVec;
typedef vector<int> IntVec;

struct Separator {
    float coeffX,coeffY,coeffC,sign;
};

typedef vector<Separator> SeparatorVec;

/* Def: struct 
 * Func: used in fileio.cc, store the su and p read from the file 
 * Author: Ashley_Xu
 * Date: 2016.04
* */
struct IntegralValue{
    float key;// the value of su
    float value;//the value of p
};


/* Def: Struct
 * Func: defined all files that will be read/written in the progress 
 * Author: Ashley_Xu
 * Date: 2016.04 
* */
struct Counters{
	int probe_num;
	int out_match_num;
	int out_stripe_num;
	int notify_num;
	int server_notify_stripe_num;
};

/* Def: Struct
 * Func: defined all flags that will be used by every user(object) 
 * Author: Ashley_Xu
 * Date: 2016.04
* */
struct Flags{
   bool* locUpload;
   bool* mustUpdate;
   bool* needUpdate;
   bool* isUpdated;  
};

/* Def: struct 
 * Func: an assistant struct of MovingPoint
 *       store the infos of matchregions
 * Author: Ashley_Xu
 * Date: 2016.04
* */

struct MatchRegion{
    int match_id; // the id of the matched friend
    double xcenter; // the xPos of the center of match region
    double ycenter; // the yPos of the center of match region
    double lambda; // the radius of match region
};

/* Def: struct 
 * Func: an assistant struct when calculate the distance between stripes
 *       the 3 parameters could represent one line equation
 * Author: Ashley_Xu
 * Date: 2016.04
* */
struct Line{ //its equation is Ax+By+C=0
	double A;//the cofficient before x in line equation
	double B;//the cofficient before y in line equation
	double C;//the cofficient of constant
};

/* Def: struct 
 * Func: an assistant struct of stripes
 * Author: Ashley_Xu
 * Date: 2016.04
* */
struct Point {
    double xPos;
    double yPos;

    Point() : xPos(0.0), yPos(0.0) {
    }
    Point(double _x, double _y) : xPos(_x), yPos(_y) {
    }
    Point(int _x, int _y) : xPos((double) _x), yPos((double) _y) {
    }
};

/* Def: struct 
 * Func: an assistant struct when run the RMF
 * Author: Ashley_Xu
 * Date: 2016.04
* */
struct coordinate {
    double xPos;
    double yPos;

    double operator[](const int index) {
      if (index == 0) return xPos;
      return yPos;
    }
    coordinate() : xPos(0.0), yPos(0.0) {
    }
    coordinate(double _x, double _y) : xPos(_x), yPos(_y) {
    }
    coordinate(int _x, int _y) : xPos((double) _x), yPos((double) _y) {
    }
};

/* Def: struct 
 * Func: an assistant struct of MovingPoint
 * Author: Ashley_Xu
 * Date: 2016.04
* */
struct Stripe{
    double radius; // the radius of stripe
    int updateiter; //the last updated timestamp
    vector<Point> points; // store all legal predicted points 
};

/* Def: struct 
 * Func: an assistant struct of MovingPoint
 *       used to record the friend pairs' ids
 * Author: Ashley_Xu
 * Date: 2016.04
* */
struct IntPair {
    int nodeA,nodeB;
};

/* Def: struct 
 * Func: used to record all the infos read from the dataset file
 * Author: Ashley_Xu
 * Date: 2016.04
* */
struct LocationRecord {
    bool isUsed;
	int year,month,day;
	int hour,min,sec;
    float xPos,yPos;
    float xVel,yVel;
};

// add at 0928
struct FriendCase {
	int fri_id;
	int add_iter;
};


/* Def: struct 
 * Func: the VIP struct in the whole project
 * Author: Ashley_Xu
 * Date: 2016.04
* */
struct MovingPoint {
    int id;
	int timestamp;
    float xPos,yPos; // the coordinate value of current timestamp
    float xVel,yVel; // the velocity value of current timestamp

    // an adjacency list of friends
	vector<FriendCase> friendcases; //add 0928
    IntVec* friends;
    BitStore isPrevResult;

    // for FMD, CMD, RMD
    float est_xPos,est_yPos;
    float est_xVel,est_yVel;
    float rect[2*DIMENSION];	// for temporary use

    // for DCC
    float cir_x,cir_y,cir_rad;

    // for CMD
	int update_cost,probe_cost;

    // for RMD
    float cur_lambda,acc_factor;
    int acc_count;

    // for STRIP
    SeparatorVec* strips;	// a list of separators w.r.t. each friend
    set<int> prox_cand;

    //for static region
    int matchregion_count;
    vector<MatchRegion> matregs;
    vector<MatchRegion> losematchs;

    //fixed-radius-stripe
    double cur_radius;//
    double Em;//elapsed time for user to move out stripe
    double Ep;//elapsed time for the next probing request
    int prednum;
    vector<coordinate> trajectory;
    vector<Point> predpoints; 
	bool ispredicted;
    Stripe stripe;
};

/** declaration of objects and FriendPairs **/
extern vector<IntPair> FriendPairs;
extern vector<MovingPoint> objects;
extern vector<LocationRecord>* globalRecords;
extern vector<Point>* allPredPoints;
extern vector<IntegralValue> integvalues;
extern Flags flags;
extern Counters counters;

/** "InitClock" also initialize the seeds for 2 random generators **/
void InitClock();
void PrintElapsed();
void CheckFile(FILE* fp,const char* filename);

#define min(a, b) (((a) < (b))? (a) : (b)  )
#define max(a, b) (((a) > (b))? (a) : (b)  )
#define ValAbs(x) (((x) >  0 )? (x) : -(x)  )

/** function declarations **/
void checkFile(FILE* fp,const char* filename);

void initObject(MovingPoint& obj);
void initialPredVector(int iter);
void initFlags();
void freeFlags();
void ClearRegions();
void initCounters();

void generateSocialNetwork(int nlength,int num_friends);
void advanceAllObjects(int curstep);
float uniform(float lb,float ub);

Point getTrueLoc(int obj, int iter);
Point getObjLoc(MovingPoint& friobj);
void plot(MovingPoint& obj);

#endif //__UTILITY
