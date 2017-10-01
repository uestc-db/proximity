#include "rmf.h"
#include "test.h"
#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <assert.h>
#include <sys/time.h>

using namespace prediction;
using namespace std;

vector<MovingPoint> objects;
vector<LocationRecord>* globalRecords;
vector<Point>* allPredPoints;

int num_objects;
long whole_time  = 0;

void CheckFile(FILE* fp,const char* filename) {
	if (fp==NULL) {
		printf("Invalid file '%s'\n",filename);
		exit(0);
	}
}

void initObjects(){
	MovingPoint dummy_obj;

	objects.clear();
	for (int i=0;i<num_objects;i++)
		objects.push_back(dummy_obj);
}


void ReadMovementFile(const char* dataname,int steps) {
	FILE* inpfile=fopen(dataname,"r");
	CheckFile(inpfile,dataname);

	// init. structures
	int nlength=objects.size();
	globalRecords=new vector<LocationRecord> [nlength];
	for (int i=0;i<nlength;i++) {
		for (int t=0;t<steps;t++) {
			LocationRecord tmprec;
			tmprec.isUsed=false;
			globalRecords[i].push_back(tmprec);	// insert the record			
		}
	}

	int linecount=0;
	while (!feof(inpfile)) {
		int id,timestamp;
		float xPos,yPos;

		fscanf(inpfile,"%d %d %f %f\n", &id, &timestamp, &xPos, &yPos);
		//printf("%d | %d | %f | %f \n", id, timestamp, xPos, yPos);

		assert(id>=0);
		if(id>=nlength)
			break;
		if(timestamp>=steps)
		    continue;
		assert(timestamp>=0&&timestamp<steps);

		// update position
		LocationRecord& tmprec=globalRecords[id][timestamp];
		tmprec.isUsed=true;
		tmprec.xPos=xPos;
		tmprec.yPos=yPos;

		linecount++;
//		printf("lines read: %d\n",linecount);

	}
	printf("lines read: %d\n",linecount);
	fclose(inpfile);

	//Ashley
	LocationRecord& rec=globalRecords[1][1];
	cout<<"####rec.isUsed="<<rec.isUsed;
	cout<<"rec.xPos="<<rec.xPos<<endl;

	printf("*** location records read\n");
}

void AdvanceAllObjects(int curstep) {
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
		obj.ispredicted=false;
	}
}

/*this function will get some future locations with some probabilities
*/
void writeOnePredFile(int iter, FILE* time_file, const char* pred_name, int pred_num){
	char* predfilename=new char[200];
    sprintf(predfilename,"%s%d.txt", pred_name, iter);
    FILE* predict_file=fopen(predfilename,"a");

	cout<<"It's ready to write predfile RMF_predfile_"<<iter<<".."<<endl;

	rmf predictor;

	struct timeval start, end;	
	int total_time = 0;

    for(int i=0;i<num_objects;i++){
        MovingPoint& curobj=objects[i];

		gettimeofday(&start, NULL);
	    predictor.loadTrajectory(globalRecords,curobj);
		predictor.predictPositions(curobj, predict_file, pred_num);
		gettimeofday(&end, NULL);
		int timeuse = 1000000 * (end.tv_sec - start.tv_sec) + end.tv_usec - start.tv_usec;//time count
		total_time += timeuse;
    }

	float avg_time = (float)total_time / (num_objects*20);

	fprintf(time_file, "%d %d %f\n", iter, total_time, avg_time);
	printf("%d %d %f\n", iter, total_time, avg_time);

	whole_time += total_time;

	fclose(predict_file); //close predfile
	delete[] predfilename;
}

/**write the predict files**/
void writePredFiles(int start_iter, int num_steps, const char* pred_name, int pred_num){
	FILE* time_file = fopen("cpu-time.txt", "a");

	int iters = 0;
	for (int iter=start_iter;iter<num_steps;iter++) {
		AdvanceAllObjects(iter);
		writeOnePredFile(iter, time_file, pred_name, pred_num);
		iters++;
	}
	
	float avg_time_last = (float)whole_time / (iters*num_objects*20);
	fprintf(time_file, "\nAt last: %d predictions, avg time: %f\n", iters*num_objects*20, avg_time_last);

	fclose(time_file);
}
