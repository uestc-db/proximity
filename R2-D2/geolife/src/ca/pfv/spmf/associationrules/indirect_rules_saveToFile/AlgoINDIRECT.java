package ca.pfv.spmf.associationrules.indirect_rules_saveToFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.frequentpatterns.aprioriTID_saveToFile.Itemset;


/**
 * An implementation of the INDIRECT algorithm for generating indirect association rules 
 *  described in the book:
 *            Tan, Steinbach & Kumar (2006) "Introduction to data mining", chapter 7, p. 469, Algorithm 7.2.
 *            
 *  and also described in the KDD 2000 paper by Tan et al.
 *  
 *  However, note that the algorithm is not exactly the same as what the authors did, because there
 *  is not enough details in the original paper and in the book. To implement the algorithm, I therefore
 *  had to make some choices based on what I tought what the best or easiest way to do it.
 *  
 *  Also, note that instead of using the IS measure to compute the dependancy between itemsets, I chose
 *  to use the confidence.  The confidence is easier to calculate.
 *            
 * Also, note that there is some faster algorithm that exists for generating indirect association rules. 
 * 
 * Lastly, note that in my implementation I use the AprioriTID algorithm for generating frequent itemsets that
 * are needed to generate indirect rules. However I do not save the frequent itemsets to file because we don't
 * need to keep them (we just want to generate the indirect rules).
 * 
 * If you find some errors or have some ideas for optimization, please let me know by contacting me on my website.
 * 
 * One possible optimization that I could do in the future would be to use BitSet instead of HashSet 
 * to represent the tids sets.
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
public class AlgoINDIRECT {

	// variables for the tid (transaction ids) set of items
	Map<Integer, Set<Integer>> mapItemTIDS = new HashMap<Integer, Set<Integer>>();
	
	int minSuppRelative;
	double minconf = 0;
	double tsRelative = 0;
	
	long startTimestamp = 0;
	long endTimeStamp = 0;
	BufferedWriter writer = null;

	private int ruleCount;
	private int tidcount =0;
	
	public AlgoINDIRECT() {
	}

	public void runAlgorithm(String input, String output, double minsup, double ts, double minconf) throws NumberFormatException, IOException {
		startTimestamp = System.currentTimeMillis();
		writer = new BufferedWriter(new FileWriter(output)); 
		this.minconf = minconf;

		// (1) count the tid set of each item in the database in one database pass
		mapItemTIDS = new HashMap<Integer, Set<Integer>>(); // id item, count
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		tidcount=0;
		while( ((line = reader.readLine())!= null)){ // for each transaction
			String[] lineSplited = line.split(" ");
			for(String stringItem : lineSplited){
				int item = Integer.parseInt(stringItem);
				Set<Integer> tids = mapItemTIDS.get(item);
				if(tids == null){
					tids = new HashSet<Integer>();
					mapItemTIDS.put(item, tids);
				}
				tids.add(tidcount);
			}
			tidcount++;
		}
		reader.close();
		
		this.minSuppRelative = (int) Math.ceil(minsup * tidcount);
		this.tsRelative= (int) Math.ceil(ts * tidcount);
		
		// To build level 1, we keep only the frequent items.
		int k=1;
		List<Itemset> level = new ArrayList<Itemset>();
		// For each item
		Iterator<Entry<Integer, Set<Integer>>> iterator = mapItemTIDS.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Set<Integer>> entry = (Map.Entry<Integer, Set<Integer>>) iterator.next();
			if(entry.getValue().size() >= minSuppRelative){ // if the item is frequent
				Integer item = entry.getKey();
				Itemset itemset = new Itemset();
				itemset.addItem(item);
				itemset.setTransactioncount(mapItemTIDS.get(item));
				level.add(itemset);
			}else{
				iterator.remove();  // if the item is not frequent we don't 
				// need to keep it into memory.
			}
		}
		
		// sort itemsets of size 1 according to lexicographical order.
		Collections.sort(level, new Comparator<Itemset>(){
			public int compare(Itemset o1, Itemset o2) {
				return o1.get(0) - o2.get(0);
			}
		});
		
		// Generate candidates with size k = 1 (all itemsets of size 1)
		k = 2;
		// While the level is not empty
		while (!level.isEmpty() ) {
			// We build the level k+1 with all the candidates that have
			// a support higher than the minsup threshold.
			level = generateCandidateSizeK(level, k); // We keep only the last level... 
			k++;
		}
		
		// close the file
		writer.close();
		endTimeStamp = System.currentTimeMillis();
	}

	protected List<Itemset> generateCandidateSizeK(List<Itemset> levelK_1, int level) throws IOException {
		List<Itemset> nextLevel = new ArrayList<Itemset>();

// For each itemset I1 and I2 of level k-1
loop1:	for(int i=0; i< levelK_1.size(); i++){
			Itemset itemset1 = levelK_1.get(i);
loop2:		for(int j=i+1; j< levelK_1.size(); j++){
				Itemset itemset2 = levelK_1.get(j);
				
				// we compare items of itemset1  and itemset2.
				// If they have all the same k-1 items and the last item of itemset1 is smaller than
				// the last item of itemset2, we will combine them to generate a candidate
				for(int k=0; k< itemset1.size(); k++){
					// if they are the last items
					if(k == itemset1.size()-1){ 
						// the one from itemset1 should be smaller (lexical order) 
						// and different from the one of itemset2
						if(itemset1.getItems().get(k) >= itemset2.get(k)){  
							continue loop1;
						}
					}
					// if they are not the last items, and 
					else if(itemset1.getItems().get(k) < itemset2.get(k)){ 
						continue loop2; // we continue searching
					}
					else if(itemset1.getItems().get(k) > itemset2.get(k)){ 
						continue loop1;  // we stop searching:  because of lexical order
					}
				}

				Integer b = itemset2.get(itemset2.size()-1);

				// =======   GENERATE ITEMSETS OF NEXT LEVEL AS IN APRIORI ======================
				Set<Integer> list = new HashSet<Integer>();
				for(Integer val1 : itemset1.getTransactionsIds()){
					if(itemset2.getTransactionsIds().contains(val1)){
						list.add(val1);
					}
				}
		
				if(list.size() >= minSuppRelative){
					// Create a new candidate by combining itemset1 and itemset2
					Itemset candidate = new Itemset();
					for(int k=0; k < itemset1.size(); k++){
						candidate.addItem(itemset1.get(k));
					}
					candidate.addItem(b);
					candidate.setTransactioncount(list);
					nextLevel.add(candidate);
				}	

			}
		}
		
		// TRY ALL COMBINATION TO GENERATE INDIRECT RULES FROM ITEMSET OF SIZE K, IF K > 2  -- NOT VERY EFFICIENT
		if(level > 2){
			// WE NEED TO FIND TWO IEMSETS WITH ONLY TWO ITEMS a,b THAT ARE DIFFERENT
			// SO WE COMPARE EACH ITEMSET OF SIZE K WITH EACH OTHER ITEMSET OF SIZE K.
			for(int i=0; i< levelK_1.size(); i++){
				for(int j=i+1; j< levelK_1.size(); j++){
					Itemset candidate1 = levelK_1.get(i);
					Itemset candidate2 = levelK_1.get(j);
					
					// We check if the pair of itemset have only one item that is different.
	  loopX:		for(Integer a : candidate1.getItems()){
						if(candidate2.getItems().contains(a) == false){
							Integer b = null;
							for(Integer itemM : candidate2.getItems()){
								if(candidate1.getItems().contains(itemM) == false){
									if(b!= null){
										continue loopX;  // more than two items are different... we don't want that.
									}
									b = itemM;  // the item that is different
								}
							}
							// if there is only one item that is different, then we call this method
							// to check if we can create an indirect rule such that it would meet the
							// ts threshold and the minconf threshold.
							testIndirectRule(candidate1, a, b);  
						}
					}
				}
	
			}
		}
		return nextLevel;
	}

	// test if an indirect rule satisfies the criteria for an indirect association rules  ts  and minconf.
	private void testIndirectRule(Itemset itemset1, Integer a, Integer b)
			throws IOException {
		Set<Integer> tidsA = mapItemTIDS.get(a);
		Set<Integer> tidsB = mapItemTIDS.get(b);
		
		// calculate the support of {a,b}
		int supportAB = 0;
		for(Integer val1 : tidsA){
			if(tidsB.contains(val1)){
				supportAB++;
			}
		}
		
		// if 
		if(supportAB < tsRelative ){
			// compute support of Y U {a}
			int supAY =0;
	loop1:	for(Integer tidA: tidsA){
				for(Integer item: itemset1.getItems()){
					if(!item.equals(a) && !item.equals(b)){
						if(!mapItemTIDS.get(item).contains(tidA)){
							continue loop1;
						}
					}
				}
				supAY++;
			}
			
			double confAY = supAY / ((double)tidsA.size()) ;
					
			if(confAY >= minconf){
				// compute support of Y U {b}
				int supBY =0;
		loop2:	for(Integer tidB: tidsB){
					for(Integer item: itemset1.getItems()){
						if(!item.equals(a) && !item.equals(b)){
							if(!mapItemTIDS.get(item).contains(tidB)){
								continue loop2;
							}
						}
					}
					supBY++;
				}
				double confBY = supBY / ((double)tidsB.size()) ;
				
				if(confBY >= minconf){
					// save the rule
					saveItemsetToFile(a, b, itemset1, confAY, confBY, supAY, supBY);  
				}
			}
		}
	}
	
	// Method to save an indirect rule to file.
	public void saveItemsetToFile(Integer a, Integer b, Itemset itemset, double confAY, double confBY, int supAY, int supBY) throws IOException{
		StringBuffer buffer = new StringBuffer();
		buffer.append("(a=");
		buffer.append(a);
		buffer.append(" b=");
		buffer.append(b);
		buffer.append(", mediator={");
		for(int i=0; i < itemset.size(); i++){
			if(!itemset.get(i).equals(a) && !itemset.get(i).equals(b)){
				buffer.append(itemset.get(i));
				buffer.append(" ");
			}
		}
		buffer.append("})");
		buffer.append("  sup(a,mediator)= ");
		buffer.append(supAY);
		buffer.append("  sup(b,mediator)= ");
		buffer.append(supBY);
		buffer.append("  conf(a,mediator)= ");
		buffer.append(confAY);
		buffer.append("  conf(b,mediator)= ");
		buffer.append(confBY);
		
		System.out.println(buffer.toString());   // DEBUG
		
		writer.write(itemset.toString() + " Support: " + itemset.getTransactionsIds().size() + " / " + tidcount + " = " + itemset.getRelativeSupport(tidcount));
		writer.newLine();
		ruleCount++;
	}
	
	public void printStats() {
		System.out
				.println("=============  INDIRECT RULES GENERATION - STATS =============");
		System.out.println(" Transactions count from database : " + tidcount);
		System.out.println(" Indirect rule count : " + ruleCount);

		System.out.println(" Total time ~ " + (endTimeStamp - startTimestamp)+ " ms");
		System.out
				.println("===================================================");
	}
}
