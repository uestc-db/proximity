package ca.pfv.spmf.sequentialpatterns;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a set of sequences, grouped by their size (how many items they have).
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
public class Sequences {
	public final List<List<Sequence>> levels = new ArrayList<List<Sequence>>();  // itemset classé par taille
	public int sequenceCount=0;
	
	private final String name;
	
	public Sequences(String name){
		this.name = name;
		levels.add(new ArrayList<Sequence>()); // on créé le niveau zéro vide par défaut.
	}
	
	public void printSequencesFrequentes(int nbObject){
		System.out.println(toString(nbObject));
	}
	
	public String toString(int nbObject){
		StringBuffer r = new StringBuffer(200);
		r.append(" ----------");
		r.append(name);
		r.append(" -------\n");
		int levelCount=0;
		for(List<Sequence> level : levels){
			r.append("  L");
			r.append(levelCount);
			r.append(" \n");
			for(Sequence sequence : level){
				r.append("  pattern ");
				r.append(sequence.getId());
				r.append(":  ");
				r.append(sequence.toString());
				r.append("support :  ");
				r.append(sequence.getRelativeSupportFormated(nbObject));
				r.append(" (" );
				r.append(sequence.getAbsoluteSupport());
				r.append('/');
				r.append(nbObject);
				r.append(") \n");
			}
			levelCount++;
		}
		r.append(" -------------------------------- Patterns count : ");
		r.append(sequenceCount);
		return r.toString();
	}
	
	public void addSequence(Sequence sequence, int k){
		while(levels.size() <= k){
			levels.add(new ArrayList<Sequence>());
		}
		levels.get(k).add(sequence);
		sequenceCount++;
	}
	
	public List<Sequence> getLevel(int index){
		return levels.get(index);
	}
	
	public int getLevelCount(){
		return levels.size();
	}

	public List<List<Sequence>> getLevels() {
		return levels;
	}
}
