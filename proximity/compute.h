#ifndef __COMPUTE
#define __COMPUTE

#include <iostream>
#include <stdio.h>
#include "utility.h"
#include "test.h"
#include "cost.h"
#include "FMDCMD.h"

using namespace std;

double findInteValue(double su);

double pointDist(Point& curpoint, MovingPoint& friobj);
double pointToStripeDist(Point& curpoint, MovingPoint& friobj);
double minDist(MovingPoint &curobj, MovingPoint& friobj);

double mult(Point a, Point b, Point c);
bool intersect(Point aa, Point bb, Point cc, Point dd);

double computeDistance(double x1,double y1,double x2,double y2);
double computeDistance(MovingPoint& objA,MovingPoint& objB);
double computeDistance(double xPos,double yPos, Point& point1, Point& point2);
double computeDistance(Point a1, Point a2, Point b1, Point b2);

double computeIntegral(double radius,double sigma);
Line solveParaLine(double x1,double y1,double x2,double y2,double d,double sign);
Point solveInsecPoint(Line& L1,Line& L2);

void fillEstRect(MovingPoint& obj,float box_width,float* rect);
void computeStripCoeff(float Ax,float Ay,float Bx,float By,Separator& sep);
float getMinDist(float *rectA, float *rectB);
float getMaxDist(float *rectA, float *rectB);

#endif
