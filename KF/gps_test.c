#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include "gps.h"
#include<math.h>
#include <memory.h>
#include<time.h>

int NUM = 2100;
int step = 39;

struct DataSet {
    int n1;
    int n2;
    double lat;
    double lon;
};

struct timeval {
 long sec;
 long usec;
}; // used to save difftime

void test_read_lat_long_track()
{
  FILE* file = fopen("ly_1wid_2k1ts_test.txt", "r");
  int n1,n2;
  double m=0;
  double lat,lon;
  double error=0;
  struct DataSet dataSet[NUM];

  struct timeval starttime;
  struct timeval endtime;

  long totaltime = 0.0;

  int h;
  FILE *P[2000];

  for(int index = 0; index < 10000; index++)
  {
      for(int i = 0;i < NUM; i++)
        {
        read_lat_long(file,&n1, &n2, &lat, &lon);
        struct DataSet dataItem = {n1, n2, lat, lon};
        dataSet[i] = dataItem;
        }
          //------------------------------------------------------
          KalmanFilter f = alloc_filter_velocity2d(1.0);
          assert(file);
          int i,j,k,l;

        for(int j=0;j<2000;j++)
        {
            double result_lat = 0;
            double result_lon = 0;

            char out[35];
            sprintf(out, "kalman_singapore_%d.txt", j+10);
            printf("%s\n",out);
            if(index==0) P[j] = fopen(out, "w");
            else P[j] = fopen(out, "a");

           if( j > 99 && j < 200) {
           gettimeofday(&starttime, NULL); // get start time to difftime


            for(k=j;k<=j+9;k++)
            {
              update_velocity2d(f, dataSet[k].lat, dataSet[k].lon, 0.001);
            }

            get_lat_long(f, &result_lat, &result_lon);

            error+=sqrt(pow((result_lat-dataSet[j+10].lat),2)+pow((result_lon-dataSet[j+10].lon),2)); //¼ÆËãÎó²î£¬Å·ÊÏ¾àÀë
            m+=1;

              for(l=j+10;l<=j+step;l++)
              {
                  update_velocity2d(f, result_lat, result_lon, 0.001);
                  get_lat_long(f, &result_lat, &result_lon);
                  if(l<j+step)
                  {
                  error+=sqrt(pow((result_lat-dataSet[l+1].lat),2)+pow((result_lon-dataSet[l+1].lon),2));//¼ÆËãÎó²î
                  m+=1;
                }
              }

            gettimeofday(&endtime, NULL);
            totaltime += 1000000 * (endtime.sec - starttime.sec) + (endtime.usec - starttime.usec);
            }
            printf("%d %d\n", index,j);
            fclose(P[j]);
            }
  }
  printf("totaltime complete!\n");
  printf("%ld\n", totaltime);
  fclose(file);
  printf("m= %f \n",m);
  printf("error= %f \n",error);
}

int main(void)
{

    test_read_lat_long_track();

return 0;
}
