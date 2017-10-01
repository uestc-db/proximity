#include "rmf.h"
#include <iostream>
#include <fstream>
#include <string>
//#include <windows.h> 

#include "util.h"

using namespace prediction;
using namespace std;

#define DEBUG_INFO

int 
rmf::pushPosition(MovingPoint& mp) {
  current_positions.push_back(mp);

  return current_positions.size();
}


MovingPoint& 
rmf::popPosition() {
  //coordinate c = *current_positions.begin();
	MovingPoint mp = *current_positions.begin();
  current_positions.erase(current_positions.begin());
  return mp;
}


void 
rmf::clearPositions(MovingPoint& mp) {
//	vector<MovingPoint>().swap(current_positions);
	vector<coordinate>().swap(mp.trajectory);
}

void 
rmf::setRetrospective(int value) {
  cerr << "TODO: causing problems for other value than 3 -> see the algorithm to handle other values" << endl;
  //retrospective = value;
}

void
rmf::loadCoordinate(const char* datafile,MovingPoint& curobj){
/*	char buf[1024];
	ifstream fin(datafile); 
	bool isopen=fin.good();
	int i=0;
	int startlinenum = (curobj.timestamp < 15) ? (curobj.id*1200) : (curobj.id*1200 + curobj.timestamp - needed_positions);
//	int endlinenum = startlinenum + curobj.timestamp - 1;
	while(fin.good()&&!fin.eof()&&curobj.trajectory.size()<needed_positions){
		if(i<startlinenum){
			char* tmp=new char[1000];
			fin.getline(tmp, 1000);
			delete[] tmp;
		}
		else {
			fin.getline(buf, 1024);
			vector<string> tok = Utility::tokenize(buf," \t");
			coordinate c(atof(tok[2].c_str()),atof(tok[3].c_str()));
			curobj.trajectory.push_back(c);
		}
		i++;
	}
	fin.close();
//	cout << curobj.trajectory.size() <<" loaded"<<endl;
*/
}

void rmf::loadTrajectory(vector<LocationRecord>* records, MovingPoint& curobj){
	int startnum = (curobj.timestamp < 15) ? 0 : curobj.timestamp - needed_positions;
	while(curobj.trajectory.size()<needed_positions){
		float x = records[curobj.id][startnum].xPos;
		float y = records[curobj.id][startnum].yPos;
		coordinate c(x,y);
		curobj.trajectory.push_back(c);
		startnum++;
	}
	
}

void 
rmf::predictPositions(MovingPoint& curobj, FILE* predfile, int steps_ahead) {
  //vector<coordinate> predicted_position;

  /*========= Start test the prediction time of one object =========*/
/*  LARGE_INTEGER BegainTime ;     
  LARGE_INTEGER EndTime ;     
  LARGE_INTEGER Frequency ;     
  QueryPerformanceFrequency(&Frequency);     
  QueryPerformanceCounter(&BegainTime) ;
  double predtime;*/

  int d = dimensions;
  int h = needed_positions;
  int f = retrospective;
//  int steps_ahead = 20;

  // check if number of recent positions is enough
  //if ((int) current_positions.size() < h) 
  if((int) curobj.trajectory.size()<h){
    //cerr << "The queue of last positions is too short. For retrospective (" << retrospective << ") the queue size have to be retrospective * 3, but the queue size is " <<  current_positions.size() << endl;
	  cerr << "The queue of last positions is too short. For retrospective (" << retrospective << ") the queue size have to be retrospective * 3, but the queue size is " <<  curobj.trajectory.size() << endl;
    //error_code = QUEUE_IS_TOO_SHORT;
	return;
  }

  //int curr_pos = current_positions.size() - 1;
  int curr_pos = curobj.trajectory.size() - 1;

  // compute S(t) for x axis
  Eigen::MatrixXd S;
  S.resize(h - f + d, d * f);
  for (int row = 1; row != h - f + 1; row++) {
    for (int col = 0; col != f; col++) {
		//S(row - 1, d * col)     = current_positions[curr_pos - row - col].xPos;
		S(row - 1, d * col)     = curobj.trajectory[curr_pos - row - col].xPos;
		//S(row - 1, d * col + 1) = current_positions[curr_pos - row - col].yPos;
		S(row - 1, d * col + 1) = curobj.trajectory[curr_pos - row - col].yPos;
    }
  }

  // add the c1c2c1c2c1c2(101010) and 010101 to the end of the S matrix
  int c = 0;
  for (int row = h - f + 1; row != h - f + 1 + d; row++) {
    for (int col = 0; col != f; col++) {
      S(row - 1, d * col)     = (c == 0) ? 1.0 : 0.0;
      S(row - 1, d * col + 1) = (c == 0) ? 0.0 : 1.0;
    }
    c++;
  }

  Eigen::JacobiSVD<Eigen::MatrixXd> svd(S, Eigen::ComputeThinU | Eigen::ComputeThinV);

  Eigen::MatrixXd U = svd.matrixU();
  Eigen::MatrixXd V = svd.matrixV();

  Eigen::MatrixXd W;
  W.resize(d * f, d * f);

  Eigen::VectorXd svalues;
  svalues = svd.singularValues();

//#if defined(DEBUG_INFO)
//  cout << "Here is the matrix S(t): " << endl << S << endl;
//#endif
  // make identity;
  W.setZero();
  for (int i = 0; i != d * f; i++) {
    W(i, i) = 1.0;
  }
  
  for (int i = 0; i != d * f; i++) {
    if (svalues(i) < 0.00000000001) W(i, i) = 0.0;
    else W(i, i) = 1.0 / svalues(i);
  }

  // Compute k1* (first row of matrix Ko)
  Eigen::MatrixXd L;
  L.resize(h - f + d, 1);
  for (int row = 0; row != h - f; row++) {
	  //L(row, 0) = current_positions[curr_pos - row].xPos;
	  L(row, 0) = curobj.trajectory[curr_pos - row].xPos;
  }

  L(h - f + 0, 0) = 1;
  L(h - f + 1, 0) = 0;
  
  Eigen::VectorXd k1 = V * W * (U.transpose() * L);
//#if defined(DEBUG_INFO)
//  cout << "Here is the vector L: " << endl << L << endl;
//  cout << "Here is the vector k1:" << endl << k1 << endl;
//  cout << "Here is the vector S * k1*: " << endl << S * k1 << endl;
//#endif
  // Compute k2 (second row of matrix Ko)
  for (int row = 0; row != h - f; row++) {
	  //L(row, 0) = current_positions[curr_pos - row].yPos;
	  L(row, 0) = curobj.trajectory[curr_pos - row].yPos;
  }
  L(h - f + 0, 0) = 0;
  L(h - f + 1, 0) = 1;

  Eigen::VectorXd k2 = V * W * (U.transpose() * L);
//#if defined(DEBUG_INFO)
//  cout << "Here is the matrix L:" << endl << L << endl;
//  cout << "Here is the vector k2:" << endl << k2 << endl;
//  cout << "Here is the vector S * k2*: " << endl << S * k2 << endl;
//#endif
  Eigen::MatrixXd Ko;
  Ko.resize(d * f, d * f);

  for (int i = 0; i != d * f; i++) {
    Ko(0, i) = k1(i);
    Ko(1, i) = k2(i);
  }

  for (int row = 2; row != h - f; row++) {
    for (int i = 0; i != d * f; i++) {
      Ko(row, i) = (row - 2) == i ? 1.0 : 0.0;
    }
  }

  // define the S(t) state
  for (int row = 0; row != h - f; row++) {
    for (int col = 0; col != f; col++) {
		//S(row, d * col)     = current_positions[curr_pos - row - col].xPos;
		S(row, d * col)     = curobj.trajectory[curr_pos - row - col].xPos;
		//S(row, d * col + 1) = current_positions[curr_pos - row - col].yPos;
		S(row, d * col + 1) = curobj.trajectory[curr_pos - row - col].yPos;
    }
  }

  c = 0;
  for (int row = h - f; row != h - f + d; row++) {
    for (int col = 0; col != f; col++) {
      S(row, d * col)     = (c == 0) ? 1 : 0;
      S(row, d * col + 1) = (c == 0) ? 0 : 1;
    }
    c++;
  }

//#if defined(DEBUG_INFO)
//  cout << "Here is the matrix Ko  :" << endl << Ko << endl;  
//  cout << "Here is the matrix S(t):" << endl << S << endl;
//  cout << "Here is the matrix S(t+1):" << endl << Ko * S.transpose() << endl;
//  cout << "Here is the matrix S(t+2):" << endl << (Ko * Ko) * S.transpose() << endl;
//  cout << "..." << endl;
//#endif


  Eigen::MatrixXd PM = Ko;
  Eigen::MatrixXd foo;

  for (int i = 0; i != steps_ahead; i++) {
    PM = PM * Ko;
    foo = PM * S.transpose();

//#if defined(DEBUG_INFO)
//    cout << "Here is the matrix S(t+" << i + 1 << "):" << endl << foo << endl;
//#endif

	//predicted_position.push_back(coordinate(foo(0, 0), foo(1, 0)));
//	curobj.predpoints.push_back(Point(foo(0, 0), foo(1, 0)));
	Point point = Point(foo(0, 0), foo(1, 0));
	
	//Pay Attention!!!!!!!!!!!!!!! ignore this sentence for predict time testing
	fprintf(predfile,"%d %d %f %f\n",curobj.id,i,point.xPos,point.yPos);
  }
/*  QueryPerformanceCounter(&EndTime);    
  predtime = (double)( EndTime.QuadPart - BegainTime.QuadPart )/ Frequency.QuadPart;
  predtime = predtime * 1000; //the unit is millisecond
*/  //printf("%d %f\n",curobj.id, predtime);
  //fprintf(predfile,"%d %f\n",curobj.id, predtime);
  /*======================== End testing =======================*/

  //error_code = OK;
  //return predicted_position;
  return;
}

rmf::rmf() : dimensions(2), retrospective(5), needed_positions(retrospective * 3) {
}

rmf::~rmf() {
}
