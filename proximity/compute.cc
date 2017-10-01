#include <iostream>
#include <stdio.h>
#include <float.h>
#include "utility.h"
#include "compute.h"
#include "test.h"
#include "cost.h"

using namespace std;

/* Def: Funcion
 * Func: look for the arrange of su
 *       then return the mapped 'p' of the upper of the arrange
 * Author: Ashley_Xu
 * Date: 2016.04
* */
double findInteValue(double su){
    float diff = 0.001;
    int multi = 10000;

    int index = (su*multi)/(diff*multi);
//    assert(integvalues[index].key < su && su < integvalues[index+1].key);

    float midkey = integvalues[index].key + (float)(diff/2);

    if(su < midkey)
        return integvalues[index].value;
    else
        return integvalues[index+1].value;
}

/******------- To judge if the 2 segs have intersections --------******/
//叉积
double mult(Point a, Point b, Point c)
{
	return (a.xPos-c.xPos)*(b.yPos-c.yPos)-(b.xPos-c.xPos)*(a.yPos-c.yPos);
}

//aa, bb为一条线段两端点 cc, dd为另一条线段的两端点 相交返回true, 不相交返回false
bool intersect(Point aa, Point bb, Point cc, Point dd)
{
	if ( max(aa.xPos, bb.xPos)<min(cc.xPos, dd.xPos) )
	{
		return false;
	}
	if ( max(aa.yPos, bb.yPos)<min(cc.yPos, dd.yPos) )
	{
		return false;
	}
	if ( max(cc.xPos, dd.xPos)<min(aa.xPos, bb.xPos) )
	{
		return false;
	}
	if ( max(cc.yPos, dd.yPos)<min(aa.yPos, bb.yPos) )
	{
		return false;
	}
	if ( mult(cc, bb, aa)*mult(bb, dd, aa)<0 )
	{
		return false;
	}
	if ( mult(aa, dd, cc)*mult(dd, bb, cc)<0 )
	{
		return false;
	}
	return true;
}
/********------------ Finishing Judging ------------*********/

/********------------ Start different ComputeDistance() ------------*********/

//Compute the distance between 2 points with their x,y parameters
double computeDistance(double x1,double y1,double x2,double y2) {
	double xdiff=fabs(x1-x2);
	double ydiff=fabs(y1-y2);
	return sqrt(xdiff*xdiff+ydiff*ydiff);
}

//compute the distance between 2 moving points
double computeDistance(MovingPoint& objA,MovingPoint& objB) {
	double xdiff=fabs(objA.xPos - objB.xPos);
	double ydiff=fabs(objA.yPos - objB.yPos);
	return sqrt(xdiff*xdiff+ydiff*ydiff);
}

//compute the distance from one point to one segment
double computeDistance(double xPos,double yPos, Point& point1, Point& point2){
    double dist;
	
    //line equation: Ax+By+C=0
    double A,B,C; //the line include the segment
	double Av,Bv,Cv1,Cv2; //two parallel lines through point1 and point2 seperately, both vertical to the line above

    //1108 add
    if(point1.xPos == point2.xPos && point1.yPos == point2.yPos){
        return computeDistance(xPos,yPos,point1.xPos,point1.yPos);
    }

    if(point1.xPos==point2.xPos){
		A=1;
		B=0;
		Av=0;
		Bv=1;
		C=(-1)*point1.xPos;
		Cv1=(-1)*point1.yPos;
		Cv2=(-1)*point2.yPos;
    }
	else if(point1.yPos==point2.yPos){
		A=0;
		Av=1;
		B=1;
		Bv=0;
		C=(-1)*point1.yPos;
		Cv1=(-1)*point1.xPos;
		Cv2=(-1)*point2.xPos;
    }
    else{
		A=(point1.yPos-point2.yPos)/(point1.xPos-point2.xPos);
		Av=(-1)/A;
		B=Bv=-1;
		C=point1.yPos-A*point1.xPos;
		Cv1=point1.yPos-Av*point1.xPos;
		Cv2=point2.yPos-Av*point2.xPos;
    }

	//check the point is in the same side or different side of the two borders
	double sign1=Av*xPos + Bv*yPos + Cv1;
	double sign2=Av*xPos + Bv*yPos + Cv2;
	
    //dist from point to line
    if(sign1*sign2<0){ //the point is between the two borders
		double d1=fabs(A*xPos + B*yPos + C);
		double d2=sqrt(pow(A,2)+pow(B,2));
		dist=d1/d2; 
	}
    else
    {
		double dist1 = computeDistance(xPos,yPos,point1.xPos,point1.yPos);
		double dist2 = computeDistance(xPos,yPos,point2.xPos,point2.yPos);
		dist=min(dist1,dist2);
    }
	return dist;
}

//compute distance between two segments
double computeDistance(Point a1, Point a2, Point b1, Point b2){
    double mindist = FLT_MAX;

	//Here, judge if there is intersections !!!
	if( intersect(a1, a2, b1, b2) == true){
		mindist = 0;
		//printf("the two segs have intersection.\n");
	}
    else {
        double p2segdist_1 = computeDistance(a1.xPos,a1.yPos,b1,b2);
        double p2segdist_2 = computeDistance(a2.xPos,a2.yPos,b1,b2);
        double p2segdist_3 = computeDistance(b1.xPos,b1.yPos,a1,a2);
        double p2segdist_4 = computeDistance(b2.xPos,b2.yPos,a1,a2);

        double min_a2b = min(p2segdist_1,p2segdist_2);
        double min_b2a = min(p2segdist_3,p2segdist_4);

        mindist = min(mindist,min(min_a2b,min_b2a));
    }

    return mindist;
}
/********------------ End different ComputeDistance() ------------*********/

/* Def: Funcion
 * Func: called by FMD.cc
 *       draw the prediction model for FMD
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void fillEstRect(MovingPoint& obj,float box_width,float* rect) {
	float half_width=box_width/2.0;
	rect[0]=obj.est_xPos-half_width;
	rect[1]=obj.est_xPos+half_width;
	rect[2]=obj.est_yPos-half_width;
	rect[3]=obj.est_yPos+half_width;
}

/* Def: Funcion
 * Func: called by FMD.cc
 *       calculate the MIN distance between 2 prediction models
 * Author: Ashley_Xu
 * Date: 2016.04
* */
float getMinDist(float *rectA, float *rectB) {
    float r,summe=0.0;
    for (int i = 0; i < DIMENSION; i++) {
		r=0;
		if (rectA[2*i+1]<rectB[2*i])
			r=rectB[2*i]-rectA[2*i+1];
		else if (rectB[2*i+1]<rectA[2*i])
			r=rectA[2*i]-rectB[2*i+1];
		summe += r*r;
    }
    return sqrt(summe);
}

/* Def: Funcion
 * Func: called by FMD.cc
 *       calculate the MAX distance between 2 prediction models
 * Author: Ashley_Xu
 * Date: 2016.04
* */
float getMaxDist(float *rectA, float *rectB) {
    float r,summe=0.0;
    for (int i = 0; i < DIMENSION; i++) {
    	r=max( rectB[2*i+1]-rectA[2*i] , rectA[2*i+1]-rectB[2*i] );
		summe += r*r;
    }
    return sqrt(summe);
}
