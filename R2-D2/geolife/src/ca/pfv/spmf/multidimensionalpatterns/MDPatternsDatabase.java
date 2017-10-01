package ca.pfv.spmf.multidimensionalpatterns;

import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation of an MDPatterns Database (based on Pinto et al, 2001)
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
public class MDPatternsDatabase {
	// Context
	private final List<MDPattern> patterns = new ArrayList<MDPattern>();

	//  Array of values indicating the number of different values for each dimension [i].
	private int[] valuesCountForDimension = null; 
	
	/**
	 * Add an MD-Pattern to the database
	 * @param pattern
	 */
	public void addMDPattern(MDPattern pattern){
		// We add the MD-Pattern.
		patterns.add(pattern);
		// If this is the first time, we keep the number of dimensions.
		if(valuesCountForDimension == null){
			valuesCountForDimension = new int[pattern.size()];
		}else{
			// Otherwise, we update the number of values for each dimension.
			for(int i=0; i< pattern.size(); i++){
				int value = pattern.get(i);
				if(value > valuesCountForDimension[i] && value != MDPattern.WILDCARD){
					valuesCountForDimension[i] = value;
				}
			}
		}
	}
	
	public void printContext(){
		System.out
		.println("============  MDPatterns Context ==========");
		System.out.println("Dimensions count : " + getDimensionCount());
		// Imprime le nombre de valeurs pour chaque dimension
		System.out.print("Number of value for each dimension  : ");
		for(int j : valuesCountForDimension){
			System.out.print(" " + j + " ");
		}
		System.out.println();
		
		// Print each pattern
		for(MDPattern pattern : patterns){ 
			System.out.print(pattern.getId() + ":  ");
			pattern.print();
			System.out.println("");
		}
	}
	
	/**
	 * Return the pattern count.
	 * @return
	 */
	public int size(){
		return patterns.size();
	}

	public List<MDPattern> getMDPatterns() {
		return patterns;
	}
	
	public MDPattern get(int index) {
		return patterns.get(index);
	}

	/**
	 * Return the number of dimensions in this database.
	 */
	public int getDimensionCount() {
		return valuesCountForDimension.length;
	}
	
	/**
	 * Return the number of different values for a dimension
	 * @param index of the dimension.
	 * @return the number of different values for this dimension.
	 */
	public int getValueCountForDimension(int index) {
		return valuesCountForDimension[index];
	}
}
