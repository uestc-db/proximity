#ifndef _FMDCMD
#define _FMDCMD

#include <iostream>
#include <stdio.h>
#include "utility.h"
#include "cost.h"

using namespace std;

enum ZM_EVENT_TYPE {EV_UPDATE, EV_PROBE};

/** function declarations **/
bool withinEstCircle(MovingPoint &, double);
void CheckResultStatus(bool isResultPair,int oid,int rel_fid,int iter);

void updateMobileRegion(MovingPoint& obj);
void advancePredModel();

void FMDProcess(double box_width,int iter, double epsilon);
void ZMSetWidth(MovingPoint& curobj,float alpha, ZM_EVENT_TYPE event);
//void CMDProcess(int iter, double box_width, double alpha, double epsilon);
void CMDProcess(int iter, double epsilon);

#endif
