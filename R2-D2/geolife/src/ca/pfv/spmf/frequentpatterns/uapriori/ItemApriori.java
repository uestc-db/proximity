package ca.pfv.spmf.frequentpatterns.uapriori;

/**
 * This class represents an item from a binary context or itemset.
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
public class ItemApriori {

	private final int id;
	private final double probability;

	public ItemApriori(int id, double probability) {
		this.id = id;
		this.probability = probability;
	}

	public int getId() {
		return id;
	}

	public String toString() {
		return "" + getId() + " (" + probability + ")";
	}

	public boolean equals(Object object) {
		ItemApriori item = (ItemApriori) object;
		if ((item.getId() == this.getId())) {
			return true;
		}
		return false;
	}

	public int hashCode() {
		String string = "" + getId();
		return string.hashCode();
	}

	public double getProbability() {
		return probability;
	}
}
