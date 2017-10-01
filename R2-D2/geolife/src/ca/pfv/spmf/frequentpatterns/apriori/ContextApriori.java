package ca.pfv.spmf.frequentpatterns.apriori;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents a binary context. It can read the context directly from
 * a file. See the ca.pfv.spmf.test folder for some examples of files containing
 * binary contexts.
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
public class ContextApriori {

	// Contexte
	private final Set<Integer> attributes = new HashSet<Integer>();
	private final List<ItemsetApriori> objects = new ArrayList<ItemsetApriori>();

	public void addItemset(ItemsetApriori itemset) {
		objects.add(itemset);
		attributes.addAll(itemset.getItems());
	}

	public void loadFile(String path) throws IOException {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			while ((thisLine = myInput.readLine()) != null) {
				if (thisLine.charAt(0) != '#') {
					addObject(thisLine.split(" "));
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

	public void addObject(String attributs[]) {
		// We assume that there is no empty line
		ItemsetApriori itemset = new ItemsetApriori();
		for (String attribute : attributs) {
			int item = Integer.parseInt(attribute);
			itemset.addItem(item);
			attributes.add(item);
		}
		objects.add(itemset);
	}

	public void printContext() {
		System.out
				.println("===================  BINARY CONTEXT ===================");
		int count = 0;
		for (ItemsetApriori itemset : objects) { // pour chaque objet
			System.out.print("0" + count + ":  ");
			itemset.print();
			System.out.println("");
			count++;
		}
	}

	public int size() {
		return objects.size();
	}

	public List<ItemsetApriori> getObjects() {
		return objects;
	}

	public Set<Integer> getAttributes() {
		return attributes;
	}

}
