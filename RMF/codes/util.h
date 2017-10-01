#ifndef __UTILITY__
#define __UTILITY__

//#include "geom.h"
#include <vector>
#include <iostream>
#include <stdio.h>
#include <string>
#include <string.h>

using namespace std;


#ifndef ENCODE_TIME
// time encoding 1 : change to seconds
#define ENCODE_TIME(y_,mo_,d_,h_,m_,s_)	((y_)*31104000+(mo_)*2592000+(d_)*86400+(h_)*3600+(m_)*60+(s_))

// time decoding
#define YEAR(t_)	((int)((t_)/31104000))			//3600*24*30*12
#define MONTH(t_)	((int)(((t_)%31104000)/2592000))	//3600*24*30
#define DAY(t_)		((int)(((t_)%2592000)/86400))		//3600*24*30/3600*24
#define HOUR(t_)	((int)(((t_)%86400)/3600))	
#define MINUTE(t_)	((int)(((t_)%3600)/60))
#define SECOND(t_)	((int)((t_)%60))

// time encoding 2 : 1116075935 -> mon(11) + day(16) + hour(07) + min(59) + qlen(35)
#define MONTH1(t_)	((int)((t_)/100000000))
#define DAY1(t_)	((int)((t_)%100000000/1000000))
#define HOUR1(t_)	((int)((t_)%1000000/10000))
#define MINUTE1(t_)	((int)((t_)%10000/100))
#define SECOND1(t_)	((int)((t_)%100))
#define ENCODE_TIME1(mo_,d_,h_,m_,s_)	((mo_)100000000+(d_)*1000000+(h_)*10000+(m_)*100+(s_))

#endif


class LineSegment;
class Point;


class Utility  
{
public:
	Utility();
	virtual ~Utility();

	/* string control */
	/*static void trim(string& str){
		string::size_type pos = str.find_last_not_of(' ');
		if(pos != string::npos) {
			str.erase(pos + 1);
			pos = str.find_first_not_of(' ');
			if(pos != string::npos) str.erase(0, pos);
		}
		else str.erase(str.begin(), str.end());};

	static bool isSame(string& s1, string& s2)
		{trim(s1);trim(s2);return (s1.compare(s2)==0);};

	static bool isSame(char* s1, char* s2)
		{return isSame(string(s1),string(s2));};*/


/*	static vector<string> tokenize(string& str, string& deli){
		vector<string> tokens;		
		// Skip delimiters at beginning.
		string::size_type lastPos = str.find_first_not_of(deli, 0);
		// Find first "non-delimiter".
		string::size_type pos     = str.find_first_of(deli, lastPos);
		while (string::npos != pos || string::npos != lastPos){
			// Found a token, add it to the vector.
			tokens.push_back(str.substr(lastPos, pos - lastPos));
			// Skip delimiters.  Note the "not_of"
			lastPos = str.find_first_not_of(deli, pos);
			// Find next "non-delimiter"
			pos = str.find_first_of(deli, lastPos);
		}
		return tokens;};
	static vector<string> tokenize(char* s, string& deli)
		{return tokenize(string(s),deli);}
	static vector<string> tokenize(string& str, char* delim)
		{return tokenize(str,string(delim));}
	static vector<string> tokenize(char* s, char* delim)	
		{return tokenize(string(s),delim);};
*/	static const char* combine(char* s1, char* s2){
		return (string(s1) + string(s2)).c_str();}
	static string int2string(int v) {
		char buf[256];
		string rst;
		memset(buf,'\0',256);
//		itoa(v,buf,10);
		sprintf(buf, "%d", v); 
		rst = buf;
		return(rst);
	}
};

#endif
