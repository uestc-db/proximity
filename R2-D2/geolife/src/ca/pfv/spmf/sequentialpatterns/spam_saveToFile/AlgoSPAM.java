package ca.pfv.spmf.sequentialpatterns.spam_saveToFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/*** 
 * This is an implementation of the SPAM algorithm. 
 * 
 * The SPAM algorithm was originally described in this paper:
 * 
 *     Jay Ayres, Johannes Gehrke, Tomi Yiu, and Jason Flannick. Sequential PAttern Mining Using Bitmaps. 
 *     In Proceedings of the Eighth ACM SIGKDD International Conference on Knowledge Discovery and Data Mining. 
 *     Edmonton, Alberta, Canada, July 2002.
 * 
 * I tried to do what is indicated in that paper but some optimizations are not described with enough details in the paper.
 * So my implementation does not include these optimizations for example:
 * - lookup tables for bitmaps
 * - compression of bitmaps.
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

public class AlgoSPAM{
		
	// for statistics
	private long startTime;
	private long endTime;
	private int patternCount;
	double maxMemory = 0;
	
	// minsup
	private int minsup = 0;

	BufferedWriter writer = null;
	
	// Vertical database
	Map<Integer, Bitmap> verticalDB = new HashMap<Integer, Bitmap>();
	
	// List indicating the number of bits per sequence
	List<Integer> sequencesSize = null;
	int lastBitIndex = 0;
		
	public AlgoSPAM(){
	}

	public void runAlgorithm(String input, String outputFilePath, double minsupRel) throws IOException {
		writer = new BufferedWriter(new FileWriter(outputFilePath)); 
		patternCount =0;
		maxMemory = 0;
		
		startTime = System.currentTimeMillis();
		spam(input, minsupRel);
		endTime = System.currentTimeMillis();
		writer.close();
	}
	
	/**
	 * @param contexte The initial context.
	 * @throws IOException 
	 */
	private void spam(String input, double minsupRel) throws IOException{
		verticalDB = new HashMap<Integer, Bitmap>();
		
		// STEP 0: SCAN THE DATABASE TO STORE THE FIRST BIT POSITION OF EACH SEQUENCE 
		// AND CALCULATE THE TOTAL NUMBER OF BIT FOR EACH BITMAP
		sequencesSize = new ArrayList<Integer>();
		lastBitIndex =0;
		try {
			FileInputStream fin = new FileInputStream(new File(input));
			BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
			String thisLine;
			int bitIndex =0;
			while ((thisLine = reader.readLine()) != null) {
				sequencesSize.add(bitIndex);
				// for each sequence
				for(String integer:  thisLine.split(" ")){
					if(integer.equals("-1")){ // indicate the end of an itemset
						bitIndex++;
					}
				}
			}
				
			lastBitIndex = bitIndex -1;
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Calculate absolute minimum support 
		minsup = (int)(minsupRel * sequencesSize.size());
		if(minsup ==0){
			minsup =1;
		}
		
		// STEP1: SCAN THE DATABASE TO CREATE THE BITMAP VERTICAL DATABASE REPRESENTATION
		try {
			FileInputStream fin = new FileInputStream(new File(input));
			BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
			String thisLine;
			int sid =0;
			int tid =0;
			while ((thisLine = reader.readLine()) != null) {
				// for each sequence
				for(String integer:  thisLine.split(" ")){
					if(integer.equals("-1")){ // indicate the end of an itemset
						tid++;
					}else if(integer.equals("-2")){ // indicate the end of a sequence
//						determineSection(bitindex - previousBitIndex);  // register the sequence length for the bitmap
						sid++;
						tid =0;
					}else{  // indicate an item
						// Get the bitmap for this item. If none, create one.
						Integer item = Integer.parseInt(integer);
						Bitmap bitmapItem = verticalDB.get(item);
						if(bitmapItem == null){
							bitmapItem = new Bitmap(lastBitIndex);
							verticalDB.put(item, bitmapItem);
						}
						// Register the bit in the bitmap for this item
						bitmapItem.registerBit(sid, tid, sequencesSize);
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// STEP2: REMOVE INFREQUENT ITEMS FROM THE DATABASE BECAUSE THEY WILL NOT APPEAR IN ANY FREQUENT SEQUENTIAL PATTERNS
		List<Integer> frequentItems = new ArrayList<Integer>();
		Iterator<Entry<Integer, Bitmap>> iter = verticalDB.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Integer, Bitmap> entry = (Map.Entry<Integer, Bitmap>) iter.next();
			if(entry.getValue().getSupport() < minsup){
//				System.out.println(entry.getKey() + " has not the support ");
				iter.remove();
			}else{
				savePattern(entry.getKey(), entry.getValue());
				frequentItems.add(entry.getKey());
			}
		}
		
		// STEP3: PERFORM THE DEPTH FIRST SEARCH!
		
		for(Entry<Integer, Bitmap> entry: verticalDB.entrySet()){
			Prefix prefix = new Prefix();
			prefix.addItemset(new Itemset(entry.getKey()));
			dfsPruning(prefix, entry.getValue(), frequentItems, frequentItems, entry.getKey());
		}
	}
	

	private void dfsPruning(Prefix prefix, Bitmap prefixBitmap, List<Integer> sn, List<Integer> in, int hasToBeGreaterThanForIStep) throws IOException {
//		System.out.println(prefix.toString());
		
		//  ======  S-STEPS ======
		List<Integer> sTemp = new ArrayList<Integer>();
		List<Bitmap> sTempBitmaps = new ArrayList<Bitmap>();
		
		for(Integer i : sn){
			Bitmap newBitmap = prefixBitmap.createNewBitmapSStep(verticalDB.get(i), sequencesSize,  lastBitIndex);
			if(newBitmap.getSupport() >= minsup){
				sTemp.add(i); 
				sTempBitmaps.add(newBitmap);
			}
		}
		for(int k=0; k < sTemp.size(); k++){
			int item = sTemp.get(k);
			// create the new prefix
			Prefix prefixSStep = prefix.cloneSequence();
			prefixSStep.addItemset(new Itemset(item));
			// create the new bitmap
			Bitmap newBitmap = sTempBitmaps.get(k);

			savePattern(prefixSStep, newBitmap);
			dfsPruning(prefixSStep, newBitmap, sTemp, sTemp, item);
		}
		
		// ========  I STEPS =======
		List<Integer> iTemp = new ArrayList<Integer>();
		List<Bitmap> iTempBitmaps = new ArrayList<Bitmap>();
		
		for(Integer i : in){
			if(i > hasToBeGreaterThanForIStep){
				Bitmap newBitmap = prefixBitmap.createNewBitmapIStep(verticalDB.get(i), sequencesSize,  lastBitIndex);
				if(newBitmap.getSupport() >= minsup){
					iTemp.add(i);
					iTempBitmaps.add(newBitmap);
				}
			}
		}
		for(int k=0; k < iTemp.size(); k++){
			int item = iTemp.get(k);
			// create the new prefix
			Prefix prefixIStep = prefix.cloneSequence();
			prefixIStep.getItemsets().get(prefixIStep.size()-1).addItem(item);
			// create the new bitmap
			Bitmap newBitmap = iTempBitmaps.get(k);
			
			savePattern(prefixIStep, newBitmap);
			dfsPruning(prefixIStep, newBitmap, sTemp, iTemp, item);
		}	
		
		checkMemory();
	}

	private void savePattern(Integer item, Bitmap bitmap) throws IOException {
		patternCount++;
		StringBuffer r = new StringBuffer("");
		r.append(item);
		r.append(" -1 ");
		r.append("SUP: ");
		r.append(bitmap.getSupport());
		writer.write(r.toString());
		writer.newLine();
	}
	
	private void savePattern(Prefix prefix, Bitmap bitmap) throws IOException {
		patternCount++;
		
		StringBuffer r = new StringBuffer("");
		for(Itemset itemset : prefix.getItemsets()){
//			r.append('(');
			for(Integer item : itemset.getItems()){
				String string = item.toString();
				r.append(string);
				r.append(' ');
			}
			r.append("-1 ");
		}

		r.append("SUP: ");
		r.append(bitmap.getSupport());
		
		writer.write(r.toString());
//		System.out.println(r.toString());
		writer.newLine();
	}

	

	private void checkMemory() {
		double currentMemory = ( (double)((double)(Runtime.getRuntime().totalMemory()/1024)/1024))- ((double)((double)(Runtime.getRuntime().freeMemory()/1024)/1024));
		if(currentMemory > maxMemory){
			maxMemory = currentMemory;
		}
	}

	public void printStatistics() {
		StringBuffer r = new StringBuffer(200);
		r.append("=============  Algorithm - STATISTICS =============\n Total time ~ ");
		r.append(endTime - startTime);
		r.append(" ms\n");
		r.append(" Frequent sequences count : " + patternCount);
		r.append('\n');
		r.append(" Max memory (mb) : " );
		r.append(maxMemory);
		r.append(patternCount);
		r.append('\n');
		r.append("===================================================\n");
		System.out.println(r.toString());
	}


}
