package ca.pfv.spmf.frequentpatterns.dci_closed_optimized;

import java.util.BitSet;

/**
 * This class represents a bix matrix
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
public class BitMatrix {

	private BitSet[] matrixItemTIDs;
	private int[] support1item; // array to keep the support of each item of
								// size 1.

	BitMatrix(int itemCount, int transactionCount) {
		support1item = new int[itemCount];
		matrixItemTIDs = new BitSet[itemCount];
		for (int i = 0; i < matrixItemTIDs.length; i++) {
			matrixItemTIDs[i] = new BitSet(transactionCount);
		}
	}

	public void addTidForItem(Integer item, int bit) {
		matrixItemTIDs[item - 1].set(bit, true);
	}

	public int getSupportOfItemFirstTime(int i) {
		support1item[i - 1] = matrixItemTIDs[i - 1].cardinality();
		return support1item[i - 1];
	}

	public int getSupportOfItem(int i) {
		return support1item[i - 1];
	}

	public BitSet getBitSetOf(Integer i) {
		return matrixItemTIDs[i - 1];
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (BitSet bitset : matrixItemTIDs) {
			buffer.append(bitset.toString());
		}
		return buffer.toString();
	}
}
