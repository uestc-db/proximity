package ca.pfv.spmf.multidimensionalpatterns;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a set of MD-Patterns, sorted by size.
 * Level 0 = MDPatterns of size 0 
 * and so on...
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
public class MDPatterns {

	private final List<List<MDPattern>> levels = new ArrayList<List<MDPattern>>();  // sorted by size
	private int sequencesCount=0;
	
	private final String name;
	
	public MDPatterns(String name){
		this.name = name;
		levels.add(new ArrayList<MDPattern>()); // level 0 is empty.
	}
	
	public void printFrequentPatterns(int objectCount){
		StringBuffer r = new StringBuffer(150);
		r.append(" ------- ");
		r.append(name);
		r.append(" -------\n");
		int levelCount=0;
		for(List<MDPattern> level : levels){
			r.append("  L");
			r.append(levelCount);
			r.append(" \n");
			for(MDPattern pattern : level){
				StringBuffer s = new StringBuffer(100);
				s.append("  pattern ");
				s.append(pattern.getId());
				s.append(":  ");
				s.append(pattern.toString());
				s.append("support :  ");
				s.append(pattern.getRelativeSupportFormatted(objectCount));
				s.append(" (");
				s.append(pattern.getAbsoluteSupport());
				s.append("/");
				s.append(objectCount);
				s.append(") ");
				s.append("\n");
				r.append(s);
			}
			levelCount++;
		}
		r.append(" --------------------------------\n");
		System.out.print(r);
	}
	
	public void addPattern(MDPattern pattern, int k){
		while(levels.size() <= k){
			levels.add(new ArrayList<MDPattern>());
		}
		levels.get(k).add(pattern);
		sequencesCount++;
	}
	
	public void removePattern(MDPattern pattern, int k){
		levels.get(k).remove(pattern);
		sequencesCount--;
	}
	
	public List<MDPattern> getLevel(int k){
		return levels.get(k);
	}

	public int size() {
		return sequencesCount;
	}

	public int getLevelCount() {
		return levels.size();
	}

}
