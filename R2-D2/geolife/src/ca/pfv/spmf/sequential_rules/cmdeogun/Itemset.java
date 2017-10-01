package ca.pfv.spmf.sequential_rules.cmdeogun;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents an itemset (a set of items)
 * 
 * Copyright (c) 2008-2012 Philippe Fournier-Viger
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */
public class Itemset {
	private final List<Integer> items = new ArrayList<Integer>(); // ordered
	public Set<Integer> transactionsIds = new HashSet<Integer>();

	public Itemset() {
	}

	public double getRelativeSupport(int nbObject) {
		return ((double) transactionsIds.size()) / ((double) nbObject);
	}

	public String getSupportRelatifFormatted(int nbObject) {
		double frequence = ((double) transactionsIds.size())
				/ ((double) nbObject);
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0);
		format.setMaximumFractionDigits(2);
		return format.format(frequence);
	}

	public int getAbsoluteSupport() {
		return transactionsIds.size();
	}

	public void addItem(Integer value) {
		items.add(value);
	}

	public List<Integer> getItems() {
		return items;
	}

	public Integer get(int index) {
		return items.get(index);
	}

	public void print() {
		System.out.print(toString());
	}

	public String toString() {
		StringBuffer r = new StringBuffer();
		for (Integer attribute : items) {
			r.append(attribute.toString());
			r.append(' ');
		}
		return r.toString();
	}

	public boolean contains(Integer item) {
		for (Integer itemI : items) {
			if (itemI.equals(item)) {
				return true;
			} else if (itemI > item) {
				return false;
			}
		}
		return false;
	}

	public void setTIDs(Set<Integer> listTransactionIds) {
		this.transactionsIds = listTransactionIds;
	}

	public int size() {
		return items.size();
	}

	public boolean allTheSameExceptLastItemV2(Itemset itemset2) {
		if (itemset2.size() != items.size()) {
			return false;
		}
		for (int i = 0; i < items.size() - 1; i++) {
			// if they are not the last items, they should be the same
			if (!items.get(i).equals(itemset2.get(i))) {
				return false;
			}
		}
		return true;
	}

	public Integer getLastItem() {
		return items.get(size() - 1);
	}

	/**
	 * This method compare this itemset with another itemset to see if they are
	 * equal. The method assume that the two itemsets are lexically ordered.
	 * 
	 * @return true or false
	 */
	public boolean allTheSame(Itemset itemset2) {
		if (itemset2.size() != items.size()) {
			return false;
		}
		for (int i = 0; i < itemset2.size(); i++) {
			if (!itemset2.getItems().get(i).equals(getItems().get(i))) {
				return false;
			}
		}
		return true;
	}

	public Set<Integer> getTransactionsIds() {
		return transactionsIds;
	}
}
