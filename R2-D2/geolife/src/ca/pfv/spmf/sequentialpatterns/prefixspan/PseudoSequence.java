package ca.pfv.spmf.sequentialpatterns.prefixspan;



/**
 * This class represents a sequence from a projected database (as based in PrefixSpan).
 * Since it is a projected sequence, it makes reference to the original sequence.
 * 
 * This class also include several methods for calculating the maximum periods, 
 * semi-maximum perdiods, etc. as required by the BIDE+ algorithm.
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

public class PseudoSequence {

	private Sequence sequence;
	private int firstItemset;
	private int firstItem;
	
	public PseudoSequence(PseudoSequence sequence, int indexItemset, int indexItem){

		this.sequence = sequence.sequence;
		this.firstItemset = indexItemset + sequence.firstItemset;
		if(this.firstItemset == sequence.firstItemset){
			this.firstItem = indexItem + sequence.firstItem;
		}else{
			this.firstItem = indexItem; // ?????????? NÉCESSAIRE??
		}
	}
	
	public PseudoSequence(PseudoSequence sequence, int indexItemset, int indexItem, int lastItemset, int lastItem){
		this.sequence = sequence.sequence;
		this.firstItemset = indexItemset + sequence.firstItemset;
		if(this.firstItemset == sequence.firstItemset){
			this.firstItem = indexItem + sequence.firstItem;
		}else{
			this.firstItem = indexItem; // ?????????? necessary??
		}
	}
	
	public  PseudoSequence(Sequence sequence, int indexItemset, int indexItem){
		this.sequence = sequence;
		this.firstItemset = indexItemset;
		this.firstItem = indexItem;
	}
	
	public int size(){
		int size = sequence.size() - firstItemset ;
		if(size == 1 && sequence.getItemsets().get(firstItemset).size() == 0){
			return 0;
		}
		return size;
	}
	
	public int getSizeOfItemsetAt(int index){
		int size = sequence.getItemsets().get(index + firstItemset).size();

		if(isFirstItemset(index)){
			size -=  firstItem;
		}
		return size;
	}

	
	//	return true if this itemset is cut at its left.
	public boolean isPostfix(int indexItemset){
		return indexItemset == 0  && firstItem !=0;
	}
	
	public boolean isFirstItemset(int index){
		return index == 0;
	}

	
	public Integer getItemAtInItemsetAt(int indexItem, int indexItemset){
//		if((firstItemset + indexItemset) > lastItemset){// Protection
//			throw new RuntimeException("Out of bound itemset!");
//		}
//		if(isLastItemset(indexItemset)){// Protection
//			if(isFirstItemset(indexItemset) && (firstItem + indexItem) > lastItem){
//				throw new RuntimeException("Out of bound item!");
//			}else if (indexItem > lastItem){
//				throw new RuntimeException("Out of bound item!");
//			}
//		}
		if(isFirstItemset(indexItemset)){
			return getItemset(indexItemset).get(indexItem + firstItem);
		}else{
			return getItemset(indexItemset).get(indexItem);
		}
	}

	
	private Itemset getItemset(int index){
		return sequence.get(index+firstItemset);
	}

	public int getId() {
		return sequence.getId();
	}

	public void print() {
		System.out.print(toString());
	}
	
//	public String toString(){
//		StringBuffer r = new StringBuffer();
//		for(int i=0; i < size(); i++){
//			for(int j=0; j < getSizeOfItemsetAt(i); j++){
//				if(!isLastItemset(i) || (j <= lastItem)){
//					r.append(getItemAtInItemsetAt(j, i).toString());
//					if(isPostfix(i)){
//						r.append('*');
//					}
//					r.append(' ');
//				}
//			}
//			r.append("}");
//		}
//		r.append("  ");
//		return r.toString();
//	}

	public int indexOf(int indexItemset, int idItem) {
		for(int i=0; i < getSizeOfItemsetAt(indexItemset); i++){
			if(getItemAtInItemsetAt(i, indexItemset) == idItem){
				return i;
			}
		}
		return -1;
	}
	

	
}