package ca.pfv.spmf.frequentpatterns.relim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This is an implementation of the RELIM algorithm as described by :
 * 
 * Borgelt, C. (2005) Keeping Things Simple: Finding Frequent Item Sets by Recursive Elimination
 * Workshop Open Source Data Mining Software (OSDM'05, Chicago, IL), 66-70.
 * ACM Press, New York, NY, USA 2005
 * 
 * It is not a very efficient frequent itemset mining algorithm, but I decided to implement it
 * because it is simple.
 * 
 * Note that it might not be implemented in a very optimized way. One reason is that in the original
 * article there is no pseudo-code for the algorithm.
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
public class AlgoRelim {

	private long startTimestamp; // for stats
	private long endTimestamp; // for stats
	private int relativeMinsupp;
	
	private int items[];

	BufferedWriter writer = null;
	private int frequentCount; // the number of frequent itemsets found (for
								// statistics)

	double maxMemory = 0;
	
	
	public AlgoRelim() {
	}

	public void runAlgorithm(double minsupp, String input, String output) throws IOException {

		startTimestamp = System.currentTimeMillis();
		
		writer = new BufferedWriter(new FileWriter(output));
		frequentCount = 0;
		maxMemory = 0;
		
		int transactionCount =0;
		
		// (1) Scan the database and count the support of each item (in a map)
		// for this map : key = item value = tidset
		final Map<Integer, Integer> mapSupport = new HashMap<Integer, Integer>();
		// scan the database
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		while (((line = reader.readLine()) != null)) { // for each transaction
			String[] lineSplited = line.split(" ");
			for (String itemString : lineSplited) { // for each item in the
													// transaction
				
				// increase the support count of the item
				Integer item = Integer.parseInt(itemString);
				Integer count = mapSupport.get(item);
				if (count == null) {
					mapSupport.put(item, 1);
				} else {
					mapSupport.put(item, ++count);
				}
			}
			transactionCount++;
		}
		reader.close();


		this.relativeMinsupp = (int) Math.ceil(minsupp * transactionCount);
		
		
		// (2) Create ordered list of items by frequency and lexical ordering
		List<Integer> listItems = new ArrayList<Integer>();
		for(Integer item : mapSupport.keySet()){
			if(mapSupport.get(item) >= relativeMinsupp){
				listItems.add(item);
			}
		}
		Collections.sort(listItems, new Comparator<Integer>(){
			public int compare(Integer item1, Integer item2){
				int compare = mapSupport.get(item1) - mapSupport.get(item2);
				if(compare ==0){
					return (item1- item2);
				}
				return compare;
			}
		});

		//(3) Create initial database structure
		int supports[] = new int[listItems.size()];
		items = new int[listItems.size()];
		for(int i=0; i< listItems.size(); i++){
			items[i] = listItems.get(i);
		}
		DatabaseStructure initialDatabase =  new DatabaseStructure(supports);
		initialDatabase.initializeTransactions();
		
		// insert transactions into initial database structure...
		reader = new BufferedReader(new FileReader(input));
		while (((line = reader.readLine()) != null)) { // for each transaction
			String[] lineSplited = line.split(" ");
			List<Integer> transaction = new ArrayList<Integer>();
			// add each transaction to the database structure
			for (String itemString : lineSplited) { // for each item in the
													// transaction
				Integer item = Integer.parseInt(itemString);
				if(mapSupport.get(item) >= relativeMinsupp){
					transaction.add(item);  // add only frequent items to the transaction
				}
			}
			
			if(transaction.size() ==0){
				continue;
			}
			
			// sort transaction according to the frequency of items
			Collections.sort(transaction, new Comparator<Integer>(){
				public int compare(Integer item1, Integer item2){
					int compare = mapSupport.get(item1) - mapSupport.get(item2);
					if(compare ==0){
						return (item1 - item2);
					}
					return compare;
				}
			});
			
			// insert transaction in the data structure 
			int firstItem = transaction.get(0);
			int indexArray = listItems.indexOf(firstItem);
			supports[indexArray]++;

			initialDatabase.transactions.get(indexArray).add(transaction.subList(1, transaction.size()));
		}
		reader.close();	
		
		// (7) START RECURSION
		recursion(initialDatabase, new int[0]);
		checkMemory();
		writer.close();
		endTimestamp = System.currentTimeMillis();
	}

	private void recursion(DatabaseStructure database, int[] prefix) throws IOException {
		
		for(int i=0; i< items.length; i++){
			if(database.supports[i] > 0 ){
				// Check if frequent
				if(database.supports[i]>= relativeMinsupp){
					// (1) add the frequent itemset to the set of frequent itemsets found!
					writeOut(items[i], prefix, database.supports[i]);
				}
				// for each transaction for this item
				database.supports[i] = 0; // empty list for i
				
				int[] newSupportPrefix = new int[database.supports.length];
				
				DatabaseStructure databasePrefix =  new DatabaseStructure(newSupportPrefix);
				databasePrefix.initializeTransactions();
	
				for(List<Integer> transaction : database.transactions.get(i)){
						Integer firstItem = transaction.get(0);
						int index = getIndexOf(firstItem);
						database.supports[index]++;
						newSupportPrefix[index]++;
						if(transaction.size() >= 2){
							List<Integer> subList = transaction.subList(1, transaction.size());
							databasePrefix.transactions.get(index).add(subList);
							database.transactions.get(index).add(subList);
						}
				}
				
				// recursion
				int []newPrefix = new int[prefix.length+1];
				System.arraycopy(prefix, 0, newPrefix, 0, prefix.length);
				newPrefix[prefix.length] = items[i];
	
				recursion(databasePrefix, newPrefix);
			}
		}
		checkMemory();
	}


	public int getIndexOf(int item){ // Could be optimized with a hashmap
		for(int i=0; i < items.length; i++){
			if(item == items[i]){
				return i;
			}
		}
		return -1;
	}

	private void checkMemory() {
		double currentMemory = ((double) ((double) (Runtime.getRuntime()
				.totalMemory() / 1024) / 1024))
				- ((double) ((double) (Runtime.getRuntime().freeMemory() / 1024) / 1024));
		if (currentMemory > maxMemory) {
			maxMemory = currentMemory;
		}
	}

	/**
	 * Write a frequent itemset to the output file.
	 * @throws IOException 
	 */
	
	private void writeOut(int item, int[]prefix, int support) throws IOException{
		frequentCount++; // for statistics
		StringBuffer buffer = new StringBuffer();
		buffer.append(item);
		buffer.append(" ");
		// WRITE ITEMS
		for (int i = 0; i < prefix.length; i++) {
			buffer.append(prefix[i]);
			if (i != prefix.length - 1) {
				buffer.append(' ');
			}
		}
		buffer.append(':');
		// WRITE SUPPORT
		buffer.append(support);
		writer.write(buffer.toString());
		writer.newLine();
	}

	public void printStatistics() {

		System.out.println("========== RELIM - STATS ============");
		System.out.println(" Number of frequent  itemsets: " + frequentCount);
		System.out.println(" Total time ~: " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Max memory:" + maxMemory);
		System.out.println("=====================================");
	}
	
}
