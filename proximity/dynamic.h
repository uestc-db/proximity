#ifndef __DYNAMIC
#define __DYNAMIC

#include <vector>
#include "utility.h"

using namespace std;

void addToFriendPairs(IntPair a_pair);
void addToObjectFriends(IntPair a_pair, int iter);
void delInFriendPairs(IntPair a_pair);
void delInObjectFriends(IntPair a_pair);

void updateAddedStripes(IntPair a_pair, int iter);

void chooseAndAddFriendPairs(int iter);
void chooseAndDelFriendPairs();

// main entry
void varySocialNetwork(int iter);

// for STRIPE algorithms
void updateAddedFMDCMDRegions(bool* isUpdated);
void updateDeledFMDCMDRegions(bool* isUpdated);
void updateFMDCMDRegions(bool* isUpdated);

void updateAddedSTRIPERegions();
void updateDeledSTRIPERegions();
void updateSTRIPERegions(int iter);

#endif //__DYNAMIC