#ifndef __STRIPE
#define __STRIPE

#include <iostream>
#include <stdio.h>

#include "utility.h"
#include "cost.h"
#include "test.h"

using namespace std;

int idToMatchnum(MovingPoint& curobj, int fid);
int idToSafenum(MovingPoint& curobj, int fid);

void addMatchRegion(MovingPoint& curobj, MovingPoint& friobj);
void delMatchRegion(MovingPoint& curobj, MovingPoint& friobj, int i, int j);

bool outStripe(MovingPoint& curobj);
void clearPredPoints();
bool withinMatchRegion(MovingPoint& curobj, MatchRegion& curmg);
bool outMatchRegion(MovingPoint& curobj);
bool updateMatchRegion(MovingPoint& curobj,int iter);
int getMaxPred(MovingPoint& curobj, int iter, int pointnum);

double calS2SDistance(int iter, int fid, int prednum, MovingPoint& curobj, bool dynamic);
double initSu(MovingPoint& curobj,int iter, int prednum);
void estimateEm(MovingPoint& curobj, double su, double p, int prednum);
void estimateEp(MovingPoint& curobj, double su, double p, int prednum, int iter);
void readCurrentPred(MovingPoint& curobj,int maxnum,int iter);
void updateStripe(MovingPoint& curobj, int iter);
void reportStripeInfo(int iter);
void initSrtipe(int iter);
void STRIPEProcess(int iter);

#endif
