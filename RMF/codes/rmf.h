#ifndef __RMF__H__
#define __RMF__H__

/*
 * This RMF class can predict user's motion based on it's previous positions
 *
 *  - the RMF stands for recursive motion function (see Tao, Y. at all: Prediction and Indexing of Moving Objects with Unknown Motion Patterns, 2004)
 *
 */

#include <Eigen/Dense>

#include <vector>

#include "test.h"
using namespace std;

namespace prediction {

  enum error {
    OK = 0,
    QUEUE_IS_TOO_SHORT
  };
/*
  struct coordinate {
    double x;
    double y;

    double operator[](const int index) {
      if (index == 0) return x;
      return y;
    }
    coordinate() : x(0.0), y(0.0) {
    }
    coordinate(double _x, double _y) : x(_x), y(_y) {
    }
    coordinate(int _x, int _y) : x((double) _x), y((double) _y) {
    }
  };
*/
  class rmfP;

  class rmf {

    int dimensions;       // dimension (x, y)
    int retrospective;    // retrospective (number of most recent position in one state)
    int needed_positions; // number of needed most recent timestamps (positions)

    //vector<coordinate> current_positions; /// stores the positions used for prediction
	vector<MovingPoint> current_positions;

  public:

    /**
     * push current positions from a queue of current positions
     */
    int pushPosition(coordinate c);

	int pushPosition(MovingPoint& mp);

    /** 
     * pop oldest positions from a queue of current positions
     */
    //coordinate popPosition();
	MovingPoint& popPosition();

    /** 
     * set number of most recent positions in one state
     * default: 3
     */
    void setRetrospective(int value = 3);

    /** 
     * clear the queue
     */
    void clearPositions(MovingPoint& mp);

    /*
     * predict all positions from current position to defined time ahead from current "time"
     */
    //vector<coordinate> predictPositions(error & error_code, int steps_ahead = 1);

	//vector<MovingPoint> predictPositions(error & eeror_code, int steps_ahead =1);
	//void predictPositions(MovingPoint* curobj, error & eeror_code, int steps_ahead =1);
	void predictPositions(MovingPoint& curobj, FILE* predfile, int steps_ahead);

	/*
	 * load position files and add to predictor
	 */
	void loadCoordinate(const char* datafile, MovingPoint& curobj);
	
	/*
	 * load trajectories from globalrecords
	 */
	void loadTrajectory(vector<LocationRecord>* records, MovingPoint& curobj);
//	void rmf::loadTrajectory(vector<LocationRecord>* records, MovingPoint& curobj);

    rmf();
    ~rmf();
  };
}

#endif
