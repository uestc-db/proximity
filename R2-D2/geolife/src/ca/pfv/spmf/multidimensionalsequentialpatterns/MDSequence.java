package ca.pfv.spmf.multidimensionalsequentialpatterns;

import java.text.DecimalFormat;

import ca.pfv.spmf.multidimensionalpatterns.MDPattern;
import ca.pfv.spmf.sequentialpatterns.Sequence;

/**
 * Implementation of a "MultiDimensional-Sequence" based on the article by Helen Pinto et al (2001).
 * 
 * An MD-Sequence is associated to a MD-Database.
 * 
 * An MD-Sequence has two parts: an mdpattern (the values for each dimension) and a sequence.
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
public class MDSequence {
	
	private final Sequence sequence;
	private final MDPattern mdpattern;
	
	private final int id; // id of this md-sequence.
	
	private int transactioncount = 0;
	
	public MDSequence(int id, MDPattern mdpattern, Sequence sequence){
		this.id = id;
		this.sequence = sequence;
		this.mdpattern = mdpattern;
		// we set the same id for the sequence and mdpattern
		sequence.setID(id);
		mdpattern.setID(id);
	}

	public Sequence getSequence() {
		return sequence;
	}

	public MDPattern getMdpattern() {
		return mdpattern;
	}

	public int getId() {
		return id;
	}

	public int size() {
		return transactioncount;
	}
	
	public double getRelativeSupport(int objectCount) {
//		System.out.println("((( " + transactioncount);
		return ((double)transactioncount) / ((double) objectCount);
	}
	
	public int getAbsoluteSupport(){
		return transactioncount;
	}
	
	
	public String getFormattedRelativeSupport(int objectCount) {
		double frequence = ((double)transactioncount) / ((double) objectCount);
		// pretty formating
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(2);
		return format.format(frequence);
	}
	
	public void print(){
		System.out.print("MDSequence " + id + ": ");
		mdpattern.print();
		sequence.print();
	}
	
	public String toString(){
		String out = "MDSequence " + id + ": ";
		out += mdpattern.toString();
		out += sequence.toString();
		return out;
	}

	public void setTransactioncount(int transactioncount) {
		this.transactioncount = transactioncount;
	}
	
	/**
	 * Return true if this  contains sequence2
	 * @param sequence2
	 * @return
	 */
	public boolean strictlyContains(MDSequence sequence2) {
		int patternContains = getMdpattern().strictlyContains(sequence2.getMdpattern());
		int sequenceContains = getSequence().strictlyContains(sequence2.getSequence());

		if(patternContains == 2 && sequenceContains ==2){
			return false; // EQUALS
		}
		if(patternContains != 0 && sequenceContains != 0){
			return true;
		}
		return false;
	}
	
	/**
	 * Return true if this  contains sequence2
	 * @param sequence2
	 * @return
	 */
	public boolean contains(MDSequence sequence2) {

		int patternContains = getMdpattern().strictlyContains(sequence2.getMdpattern());
		int sequenceContains = getSequence().strictlyContains(sequence2.getSequence());
		
		if(patternContains != 0 && sequenceContains != 0){
			return true;
		}
		return false;
	}
}
