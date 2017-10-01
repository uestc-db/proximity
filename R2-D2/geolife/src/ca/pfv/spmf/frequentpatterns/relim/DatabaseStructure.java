package ca.pfv.spmf.frequentpatterns.relim;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a transaction database for the RELIM algorithm
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
public class DatabaseStructure {

	int[] supports;

	List<List<List<Integer>>> transactions = new ArrayList<List<List<Integer>>>();

	public DatabaseStructure(int[] supports) {
		this.supports = supports;
	}

	public void initializeTransactions() {
		// Initialize "transactions"
		for (int i = 0; i < supports.length; i++) {
			transactions.add(new ArrayList<List<Integer>>());
		}
	}

	public String toString() {
		StringBuffer temp = new StringBuffer();
		temp.append("\n supports : ");
		for (Integer integer : supports) {
			temp.append(integer);
			temp.append(" ");
		}
		temp.append("\nLISTS\n");
		for (int i = 0; i < supports.length; i++) {
			temp.append("sup: " + supports[i] + " "
					+ transactions.get(i).toString() + "\n");
		}

		return temp.toString();
	}
}
