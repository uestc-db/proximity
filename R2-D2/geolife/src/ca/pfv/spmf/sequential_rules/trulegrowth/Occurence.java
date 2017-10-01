package ca.pfv.spmf.sequential_rules.trulegrowth;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an occurence
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
public class Occurence {
	public int transactionID =-1;
	public List<Short> occurences = new ArrayList<Short>();
	
	public Occurence(int transactionID){
		this.transactionID = transactionID;
	}
	
	public void add(short occurence){
		occurences.add(occurence);
	}
	
	public short getFirst(){
		return occurences.get(0);
	}
	
	public short getLast(){
		return occurences.get(occurences.size()-1);
	}
	
	public boolean equals(Object obj) {
		return ((Occurence)obj).transactionID == transactionID;
	}

	public int hashCode() {
		return transactionID;
	}
}
