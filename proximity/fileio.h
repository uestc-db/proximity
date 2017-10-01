/*************************************************************************
	> File Name: fileio.h
	> Author: 
	> Mail: 
	> Created Time: 2016年06月08日 星期三 07时33分27秒
 ************************************************************************/

#ifndef _FILEIO_H
#define _FILEIO_H

#include <stdio.h>
#include "test.h"

using namespace std;

/* Def: Struct
 * Func: defined all files that will be read/written in the progress 
 * Author: Ashley_Xu
 * Date: 2016.04 
* */
struct Files{
	FILE* result_file; // write 'cost' 
	FILE* predict_file; // read the predicted points 
	FILE* pred_time_file; // write the run time of RMF
    FILE* integral_file; // read the su-p map from this Files
    FILE* draw_data_file;
};

/** global vars **/
extern Files files;

/** function declarations **/
void readInputFile(const char* dataname,int steps);
void readIntegral();
void readPredFile(int iter);

void writeOutFile(int mov_speed, const char* out_file_name);
void writeDrawFile(int iter);
#endif
