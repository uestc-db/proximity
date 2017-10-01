package ca.pfv.spmf.multidimensionalpatterns;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ca.pfv.spmf.frequentpatterns.apriori.AlgoAprioriClose;
import ca.pfv.spmf.frequentpatterns.apriori.ContextApriori;
import ca.pfv.spmf.frequentpatterns.apriori.ItemsetApriori;
import ca.pfv.spmf.frequentpatterns.apriori.Itemsets;
import ca.pfv.spmf.frequentpatterns.eclat_and_charm_saveToMemory.AlgoCharm;
import ca.pfv.spmf.frequentpatterns.eclat_and_charm_saveToMemory.Context;
import ca.pfv.spmf.frequentpatterns.eclat_and_charm_saveToMemory.Itemset;

/**
 * Class to extract frequent MD-Patterns (multi-dimensional patterns) from a MD-Database.
 * To perform this, we use the AprioriClose or CHARM .
 * This allow to find all frequent MD-Patterns and those that are closed.
 * We can then keep only the closed one, if necessary.
 * 
 * To proceed,  we (1) convert MD-Patterns in itemsets, (2) mine frequent (closed) itemsets
 * and (3) convert itemsets back in MD-Patterns.
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
public class AlgoDimApriori{
	private MDPatterns patterns = new MDPatterns("Frequent MD Patterns");
	private int dimensionsCount;
	
	private boolean findClosedPatterns;
	private boolean findClosedPatternsWithCharm;
	
	/**
	 * Constructor.
	 * @param findClosedPatterns Indicates if this class uses Apriori or Apriori-Close to
	 * find respectively frequent itemsets or frequent closed itemsets.
	 */
	public AlgoDimApriori(boolean findClosedPatterns){
		this.findClosedPatterns = findClosedPatterns;
	}
	
	/**
	 * @param findClosedPatterns Indicates if this class has to find respectively frequent itemsets
	 *  or frequent closed itemsets.
	 * @param Indicates to use CHARM or AprioriClose to find freq. closed patterns.
	 */
	public AlgoDimApriori(boolean findClosedPatterns, 
			boolean findClosedPatternsWithCharm){
		this.findClosedPatterns = findClosedPatterns;
		this.findClosedPatternsWithCharm = findClosedPatternsWithCharm;
	}
	
	public MDPatterns runAlgorithm(MDPatternsDatabase contexte, double minsupp) {
		patterns = new MDPatterns("FREQUENT MD Patterns");
		
		this.dimensionsCount = contexte.getMDPatterns().get(0).size();

		if(findClosedPatternsWithCharm){ // CHARM
			Context contextCharm = new Context();
			for(MDPattern pattern : contexte.getMDPatterns()){
				contextCharm.addItemset( convertPatternToItemsetCharm(pattern));
			}
			AlgoCharm charm = new AlgoCharm(contextCharm, 100000);
			
//			contexte.printContext();
//			contextCharm.printContext();
			ca.pfv.spmf.frequentpatterns.eclat_and_charm_saveToMemory.Itemsets frequentPatterns = charm.runAlgorithm(minsupp, true);
			
			int maxSupport = 0;
			// Convert patterns found by Charm into MDPatterns
			for(List<Itemset> itemsets : frequentPatterns.getLevels()){
				for(Itemset itemset : itemsets){
					MDPattern pattern = convertItemsetCharmToPattern(itemset);
					patterns.addPattern(pattern, pattern.size());
					
					if(itemset.getAbsoluteSupport() > maxSupport){
						maxSupport = itemset.getAbsoluteSupport();
					}
				}
			}
			
			// add the empty set to the list of patterns if necessary
			if(maxSupport < contextCharm.size()){
				patterns.addPattern(convertItemsetCharmToPattern(new Itemset()), 0);
			}
			
		}else{  // APRIORI / APRIORI-CLOSE
		
				// Appel de AprioriClose
			// (1) Create the context for AprioriClose
			ContextApriori context = new ContextApriori();
			for(MDPattern pattern : contexte.getMDPatterns()){
				context.addItemset(convertPatternToItemset(pattern));
			}
			AlgoAprioriClose apriori = new AlgoAprioriClose(context);
	//		context.printContext();
			Itemsets frequentPatterns = apriori.runAlgorithm(minsupp);
			
			if(findClosedPatterns){
				frequentPatterns = apriori.getFrequentClosed();
			}
			
			// Convert patterns found by AprioriClose into MDPatterns
			for(List<ItemsetApriori> itemsets : frequentPatterns.getLevels()){
				for(ItemsetApriori itemset : itemsets){
					MDPattern pattern = convertItemsetToPattern(itemset);
					patterns.addPattern(pattern, pattern.size());
				}
			}
		}
		return patterns;
	}
	


	public Map<Integer, String> mapItemIdIdentifier = new HashMap<Integer,String>();
	public Map<String, Integer> mapIdentifierItemId = new HashMap<String, Integer>();
	int lastUniqueItemIdGiven=0;

	
	public Integer getValueForItemId(int value){
		String identifier = mapItemIdIdentifier.get(value);
		int index = identifier.indexOf("-");
		return Integer.valueOf(identifier.substring(0, index));
	}
	
	public Integer getDimensionForItemId(int value){
		String identifier = mapItemIdIdentifier.get(value);
		int index = identifier.indexOf('-');
		return Integer.valueOf(identifier.substring(index+1, identifier.length()));
	}
	
	public int convertDimensionValueToItemId(int indexDimension, Integer value){
		Integer itemId = mapIdentifierItemId.get("" + value + "-" + indexDimension);
		if(itemId == null){
			itemId = lastUniqueItemIdGiven++;
			StringBuffer identifier = new StringBuffer();
			identifier.append(value);
			identifier.append('-');
			identifier.append(indexDimension);
			mapIdentifierItemId.put(identifier.toString(), itemId);
			mapItemIdIdentifier.put(itemId, identifier.toString());
		}
		return itemId;
	}
	
	private ItemsetApriori convertPatternToItemset(MDPattern pattern) {
		ItemsetApriori itemset = new ItemsetApriori();
		for(int i=0; i < pattern.values.size(); i++){
			itemset.addItem(convertDimensionValueToItemId(i, pattern.values.get(i)));
		}
		
		return itemset;
	}
	
	
	private Itemset convertPatternToItemsetCharm(MDPattern pattern) {
		Itemset itemsetCharm = new Itemset();
		for(int i=0; i < pattern.values.size(); i++){
			itemsetCharm.addItem(convertDimensionValueToItemId(i, pattern.values.get(i)));
		}
//		itemsetCharm.transactionId = pattern.; // TODO :  IS THIS CORRECT ??? 2010-10-09  THERE WAS A BUG HERE.
		return itemsetCharm;
	}
	
	private MDPattern convertItemsetToPattern(ItemsetApriori itemset) {
		MDPattern mdpattern = new MDPattern(0);
		for(int i=0; i< dimensionsCount; i++){
			for(int j=0; j<itemset.size(); j++){
				int dimension = getDimensionForItemId(itemset.get(j));
				int value = getValueForItemId(itemset.get(j));
				if(dimension == i){
					mdpattern.addInteger(value);
				}
			}
			if(mdpattern.size() == i){
				mdpattern.addWildCard();
			}
		}
 
		// PATCH FOR SUPPORT...
		mdpattern.setPatternsIDList(new HashSet<Integer>());
		for(int i=0; i< itemset.getAbsoluteSupport(); i++){
			mdpattern.getPatternsID().add(9999+i); // because we don't have the id..
		}
		
		return mdpattern;
	}
	
	private MDPattern convertItemsetCharmToPattern(Itemset itemset) {
		MDPattern mdpattern = new MDPattern(0);
		for(int i=0; i< dimensionsCount; i++){
			for(int j=0; j<itemset.size(); j++){
				Object[] objects = itemset.getItems().toArray();
				int dimension = getDimensionForItemId((Integer)objects[j]);
				int value = getValueForItemId((Integer)objects[j]);
				if(dimension == i){
					mdpattern.addInteger(value);
				}
			}
			if(mdpattern.size() == i){
				mdpattern.addWildCard();
			}
		}
 
		mdpattern.setPatternsIDList(itemset.getTransactionsIds());
		
		return mdpattern;
	}

	public void printStats(int objectsCount) {
		System.out.println("=============  DIM - STATS =============");
		System.out.println(" Frequent patterns count : " + patterns.size()); 
		patterns.printFrequentPatterns(objectsCount);
		System.out.println("===================================================");
	}

}
