#include <stdio.h>
#include "test.h"
#include "fileio.h"
#include "utility.h"

using namespace std;

Files files;

/* Def: Function 
 * Func: called in test.cc, 
 *       read the infos of all users at the current timestamp from dataset file
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void readInputFile(const char* dataname,int steps) {
	FILE* inpfile=fopen(dataname,"r");
	checkFile(inpfile,dataname);

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

		assert(id>=0);
		if(id>=nlength)
			break;
		if(timestamp>=steps + 100) //Attention! For perfect prediction. 17.09.25
		    continue;
		assert(timestamp>=0&&timestamp<steps + 100);

		// update position
		LocationRecord& tmprec=globalRecords[id][timestamp];
		tmprec.isUsed=true;
		tmprec.xPos=xPos;
		tmprec.yPos=yPos;
		tmprec.xVel=tmprec.yVel=0.0;

		if (timestamp>0) { // derive velocity
			LocationRecord& prevrec=globalRecords[id][timestamp-1];
                        
            //Ashley
			assert(prevrec.isUsed==true);
			tmprec.xVel=tmprec.xPos-prevrec.xPos;
			tmprec.yVel=tmprec.yPos-prevrec.yPos;
		}

		linecount++;

	}
	//printf("lines read: %d\n",linecount);
	//printf("*** location records read\n");
	fclose(inpfile);
}

/* Def: Funcion
 * Func: read the Integral table from the file
 * Author: Ashley_Xu
 * Date: 2016.06
* */
void readIntegral(){
    char* integfilename = new char[200];
//    sprintf(integfilename, "integral.txt");
    sprintf(integfilename, "0.009_inte.txt");

    files.integral_file = fopen(integfilename, "r");
    if (!files.integral_file)
        printf("The outfile is valid!\n");

    assert(files.integral_file);

    integvalues.clear();

    while(!feof(files.integral_file)){
        float key, value;

        fscanf(files.integral_file, "%f\t%f\n", &key, &value);

        assert(key>=0);

        IntegralValue tmpinte;
        tmpinte.key = key;
        tmpinte.value = value;

        integvalues.push_back(tmpinte);
    }

    //cout<<"Integral file  has been *READ* all."<<endl;
}

/* Def: Funcion
 * Func: read the predicion files generated by different prediction models
 *       then write all the infos into the global array 'allPredPoints'
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void readPredFile(int iter){

	//init structure
    initialPredVector(iter);

    //open the file
    char* pred_file_name=new char[200];
    sprintf(pred_file_name,"%s%d.txt", in_prd_path, iter);

    files.predict_file=fopen(pred_file_name, "r");
    checkFile(files.predict_file, pred_file_name);
    cout<<"It's going to *READ* predfile_"<<iter<<" .."<<endl;

    while (!feof(files.predict_file)){
        int id, number;
        float xpos, ypos;

        fscanf(files.predict_file,"%d %d %f %f\n", &id, &number, &xpos, &ypos);
        assert(id >= 0 && id < num_objects);

		//printf("In readPredFile(), x_left=%f, x_right=%f, y_left=%f, y_right=%f\n", x_left, x_right, y_left, y_right);

		if(xpos < x_left || xpos > x_right || ypos < y_left || ypos > y_right)
			continue;

		Point predpoint;
		predpoint.xPos = xpos;
		predpoint.yPos = ypos;
	
		allPredPoints[id].push_back(predpoint);	

		// a new predict point
		//Point predpoint;
		//if(xpos < x_left || xpos > x_right || ypos < y_left || ypos > y_right) { //out of bound, a disqualification
		//	int size = allPredPoints[id].size();
		//	predpoint.xPos = allPredPoints[id][size-1].xPos;
		//	predpoint.yPos = allPredPoints[id][size-1].yPos;
		//}
		//else {
		//	predpoint.xPos = xpos;
		//	predpoint.yPos = ypos;
		//}
	
		//allPredPoints[id].push_back(predpoint);	
		//printf("user %d's predpoints have been read already.\n", id);
    }
    cout<<"current predfile has been *READ* all."<<endl;
    fclose(files.predict_file);

    delete[] pred_file_name;
}


/* Def: Function 
 * Func: calculate the last cost 
 *       and write it into the result_file
 * Author: Ashley_Xu
 * Date: 2016.04
* */
void writeOutFile(int mov_speed, const char* out_file_name){

	files.result_file = fopen(out_file_name,"a");
    checkFile(files.result_file, out_file_name);

    //calculate the average value of per iteration
	double factor = (double)mov_speed / base_speed;

	//printf("Before computing, proxim:     %d\n", results.num_proxim);
	//printf("Before computing, up_cost:    %d\n", results.cost_upload);
	//printf("Before computing, down_cost:  %d\n", results.cost_download);
 //   printf("Before computing, total_cost: %d\n", results.cost_upload+results.cost_download);
	//printf("Before computing, mov_speed:  %d, base_speed: %d\n", mov_speed, base_speed);
	//printf("Before computing, factor:	  %f\n\n", factor);

	long total_proxim     = results.num_proxim;
	long total_up_cost    = (long)results.cost_upload * factor;
	long total_down_cost  = (long)results.cost_download * factor;
	long total_sum_cost   = (long)((results.cost_upload + results.cost_download) * factor);

    //printf("total_proxim:     %ld\n", total_proxim);
    //printf("total_up_cost:    %ld\n", total_up_cost);
    //printf("total_down_cost:  %ld\n", total_down_cost);
    //printf("total_total_cost: %ld\n\n", total_sum_cost);

    fprintf(files.result_file, "%ld\n", total_proxim);
    fprintf(files.result_file, "%ld\n", total_up_cost);
    fprintf(files.result_file, "%ld\n", total_down_cost);
    fprintf(files.result_file, "%ld\n\n", total_sum_cost);

	fclose(files.result_file);
}

/* Def: Funcion
 * Func: write the file used to draw variation diagram for some abnormal user
 * Author: Ashley_Xu
 * Date: 2016.06
* */
void writeDrawFile(int iter){
    int main_uid = 4818; //the id of the abnormal user

    //open the file which will be written
    char* datafilename = new char[200];
    sprintf(datafilename, "./results/%d/iter_%d", main_uid, iter);
    files.draw_data_file = fopen(datafilename,"a");
    if(files.draw_data_file == NULL)
        printf("Draw_Data_File is invalid.\n");

    //write some info of the main_uid
    fprintf(files.draw_data_file, "%d\t%0.6f\t%0.6f\t", main_uid, objects[main_uid].xPos,objects[main_uid].yPos);

    //write the stripe info of the main_uid 
    for (int j=0; j<objects[main_uid].stripe.points.size(); j++){
        fprintf(files.draw_data_file, "%0.6f\t%0.6f\t", objects[main_uid].stripe.points[j].xPos, objects[main_uid].stripe.points[j].yPos);
            
    }

    //write the stripe radius of the main_uid
    fprintf(files.draw_data_file, "%0.6f\n", objects[main_uid].stripe.radius);

    //It's turn to the friends of the main_uid
    for(int i=0; i< objects[main_uid].friends->size(); i++){
        int fid = objects[main_uid].friends->at(i);

        fprintf(files.draw_data_file, "%d\t%0.6f\t%0.6f\t", fid, objects[fid].xPos,objects[fid].yPos);

        for (int j=0; j<objects[fid].stripe.points.size(); j++)
        {
            fprintf(files.draw_data_file, "%0.6f\t%0.6f\t", objects[fid].stripe.points[j].xPos,objects[fid].stripe.points[j].yPos);
        }

        fprintf(files.draw_data_file, "%0.6f\n", objects[fid].stripe.radius);
    }
    fclose(files.draw_data_file);
}
