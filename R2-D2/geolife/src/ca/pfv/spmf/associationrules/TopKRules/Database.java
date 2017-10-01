package ca.pfv.spmf.associationrules.TopKRules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class represents a transaction database.
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
public class Database {
	public int maxItem = 0;
	public int tidsCount = 0;

	private final List<Transaction> transactions = new ArrayList<Transaction>();

	public void loadFile(String path, int begin, int end) throws IOException {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			int i = 0;
			while ((thisLine = myInput.readLine()) != null) {
				addTransaction(thisLine.split(" "));
				i++;
				if (i == end) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	}

	public void loadFile(String path) throws IOException {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			while ((thisLine = myInput.readLine()) != null) {
				addTransaction(thisLine.split(" "));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	}

	public void addTransaction(String attributs[]) {
		// We assume that there is no empty line
		Transaction transaction = new Transaction(attributs.length);
		for (String string : attributs) {
			if ("".equals(string)) {
				continue;
			}
			int item = Integer.parseInt(string);
			if (item >= maxItem) {
				maxItem = item;
			}
			transaction.addItem(item);
		}
		tidsCount++;
		transactions.add(transaction);
		// IMPORTANT : SORT DATABASE TRANSACTION BY DESCENDING ORDER OF
		// ITEMS!!!!!!!
		// THIS IS IMPORTANT FOR OPTIMIZATION OF TopKRules in expandR() and
		// expandLR()
		Collections.sort(transaction.getItems(), new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return o2 - o1;
			}
		});
	}

	public int size() {
		return transactions.size();
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public int checkDatabaseSize(String path) throws IOException {
		int databaseSize = 0;
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			while ((thisLine = myInput.readLine()) != null) {
				databaseSize++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
		return databaseSize;
	}
}
