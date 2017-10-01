package ca.pfv.spmf.sequential_rules.cmdeogun;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a sequence. A sequence is a list of itemsets.
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
public class Sequence {

	private final List<Itemset> itemsets = new ArrayList<Itemset>();
	private int id; // id de la sequence

	public Sequence(int id) {
		this.id = id;
	}

	public void addItemset(Itemset itemset) {
		itemsets.add(itemset);
	}

	public void print() {
		System.out.print(toString());
	}

	public String toString() {
		StringBuffer r = new StringBuffer("");
		for (Itemset itemset : itemsets) {
			r.append('(');
			for (Integer item : itemset.getItems()) {
				String string = item.toString();
				r.append(string);
				r.append(' ');
			}
			r.append(')');
		}

		return r.append("    ").toString();
	}

	public int getId() {
		return id;
	}

	public List<Itemset> getItemsets() {
		return itemsets;
	}

	public Itemset get(int index) {
		return itemsets.get(index);
	}

	public int size() {
		return itemsets.size();
	}

	public void setID(int id2) {
		id = id2;
	}

}
