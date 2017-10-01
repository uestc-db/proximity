package ca.pfv.spmf.multidimensionalsequentialpatterns;

import java.util.ArrayList;
import java.util.List;

/**
 * Set of MD-Séquences. Returned by the AlgoSeqDim algorithm.
 * 
 * Sequences are ordered by their size.
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
public class MDSequences {
	
	private final List<List<MDSequence>> levels = new ArrayList<List<MDSequence>>();  // itemset sorted by size
	private int sequencesCount=0;
	
	private final String name;
	
	public MDSequences(String name){
		this.name = name;
		levels.add(new ArrayList<MDSequence>()); // the level 0  is empty by default.
	}
	
	public void printFrequentSequences(int sequenceCount){
		System.out.println(" ------- " + name + " -------");
		int levelCount=0;
		for(List<MDSequence> level : levels){
			System.out.println("  L" + levelCount + " ");
			for(MDSequence sequence : level){
				System.out.print("  pattern " + sequence.getId() + ":  ");
				sequence.print();
				System.out.print("support :  " + sequence.getFormattedRelativeSupport(sequenceCount));
				System.out.print(" (" + sequence.getAbsoluteSupport() + "/" + sequenceCount + ") ");
				System.out.println("");
			}
			levelCount++;
		}
		System.out.println(" --------------------------------");
	}
	
	public String toString(int nbObject){
		StringBuffer out = new StringBuffer(" ------- " + name + " -------");
		int levelCount=0;
		for(List<MDSequence> level : levels){
			out.append("  L" + levelCount + "\n");
			for(MDSequence sequence : level){
				out.append("  pattern " + sequence.getId() + ":  \n");
				out.append(sequence.toString());
				out.append("support :  " + sequence.getFormattedRelativeSupport(nbObject));
				out.append(" (" + sequence.getAbsoluteSupport() + "/" + nbObject + ") \n");
			}
			levelCount++;
		}
		out.append(" --------------------------------");
		return out.toString();
	}
	
	public void addSequence(MDSequence sequence, int k){
		while(levels.size() <= k){
			levels.add(new ArrayList<MDSequence>());
		}
		levels.get(k).add(sequence);
		sequencesCount++;
	}
	
	public List<MDSequence> getLevel(int k){
		return levels.get(k);
	}

	public int size() {
		return sequencesCount;
	}

	public List<List<MDSequence>> getLevels() {
		return levels;
	}

	public void recalculateSize() {
		sequencesCount =0;
		for(List<MDSequence> level : levels){
			sequencesCount += level.size();
		}
	}
}
