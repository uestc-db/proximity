#ifndef __TEST
#define __TEST

#include <vector>
#include <stdio.h>
#include <stdlib.h>

using namespace std;

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

struct MovingPoint {
    int id;
	int timestamp;
    float xPos,yPos;

    //fixed radius
    double cur_radius;//
    double Em;//elapsed time for user to move out stripe
    double Ep;//elapsed time for the next probing request
    int prednum;
    vector<coordinate> trajectory;
    vector<Point> predpoints; 
	bool ispredicted;
};

struct LocationRecord {
    bool isUsed;
	int year,month,day;
	int hour,min,sec;
    float xPos,yPos;
};

extern int num_objects;

void initObjects();
void CheckFile(FILE* fp,const char* filename);
void AdvanceAllObjects(int curstep);
void ReadMovementFile(const char* dataname,int steps);

void writeOnePredFile(int iter, FILE* time_file, const char* pred_name, int pred_num);
void writePredFiles(int start_iter, int num_steps, const char* pred_name, int pred_num);

#endif //__TEST
