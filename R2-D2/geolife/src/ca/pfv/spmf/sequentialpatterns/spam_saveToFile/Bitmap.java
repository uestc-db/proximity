package ca.pfv.spmf.sequentialpatterns.spam_saveToFile;

import java.util.BitSet;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of a bitmap for SPAM.
 *
 * Copyright (c) 2008-2012 Philippe Fournier-Viger
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SPMF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Bitmap {
	
	private BitSet bitmap = new BitSet();  // the bitmap
	
	// for calculating the support more efficiently
	private int lastSID = -1;  // the sid of the last sequence inserted that contains a bit set to true
	private int support = 0;  // the number of bits that are currently set to 1
	
	Bitmap(int lastBitIndex){
		this.bitmap = new BitSet(lastBitIndex+1); 
	}
	
	private Bitmap(BitSet bitmap){
		this.bitmap = bitmap; 
	}

	public void registerBit(int sid, int tid, List<Integer> sequencesSize) {
		int pos = sequencesSize.get(sid) + tid;
		
		bitmap.set(pos, true);
		
		// to update the bit count
		if(sid != lastSID){
			support++;
		}
		lastSID = sid;
	}
	
	private int bitToSID(int bit, List<Integer> sequencesSize) {
		int result = Collections.binarySearch(sequencesSize, bit);
		if(result >= 0){
			return result;
		}
		return 0 - result -2;
	}

	public int getSupport() {
		return support;
	}

	Bitmap createNewBitmapSStep(Bitmap bitmapItem, List<Integer> sequencesSize, int lastBitIndex) {
		BitSet newBitset = new BitSet(lastBitIndex); 
		Bitmap newBitmap = new Bitmap(newBitset);
		
		// We do an AND with the bitmap of the item
		for (int bitK = bitmap.nextSetBit(0); bitK >= 0; bitK = bitmap.nextSetBit(bitK+1)) {
			
			int sid = bitToSID(bitK, sequencesSize);
 			int lastBitOfSID = lastBitOfSID(sid, sequencesSize, lastBitIndex);
			
			boolean match = false;
			for (int bit = bitmapItem.bitmap.nextSetBit(bitK+1); bit >= 0 && bit <= lastBitOfSID; bit = bitmapItem.bitmap.nextSetBit(bit+1)) {
				newBitmap.bitmap.set(bit);
				match = true;
			}
			if(match){
				// update the support
				if(sid != newBitmap.lastSID){
					newBitmap.support++;
				}
				newBitmap.lastSID = sid;
			}
			bitK = lastBitOfSID; // to skip the bit from the same sequence
		}

		// We return the resulting bitmap
		return newBitmap;
	}

	private int lastBitOfSID(int sid, List<Integer> sequencesSize, int lastBitIndex) {
		if(sid+1 >= sequencesSize.size()){
			return lastBitIndex;
		}else{
			return sequencesSize.get(sid+1) -1;
		}
	}



	Bitmap createNewBitmapIStep(Bitmap bitmapItem, List<Integer> sequencesSize, int lastBitIndex) {
		// We create the new bitmap
		BitSet newBitset = new BitSet(lastBitIndex); // TODO: USE LAST SET BIT
		Bitmap newBitmap = new Bitmap(newBitset);
		
		// We do an AND with the bitmap of the item
		for (int bit = bitmap.nextSetBit(0); bit >= 0; bit = bitmap.nextSetBit(bit+1)) {
			if(bitmapItem.bitmap.get(bit)){ // if both bits are TRUE
				
				// set the bit
				newBitmap.bitmap.set(bit);
				// update the support
				int sid = bitToSID(bit, sequencesSize);
				if(sid != newBitmap.lastSID){
					newBitmap.support++;
				}
				newBitmap.lastSID = sid;
			}
		}
		// Then 
		newBitset.and(bitmapItem.bitmap);
		
		// We return the resulting bitmap
		return newBitmap;
	}
}
