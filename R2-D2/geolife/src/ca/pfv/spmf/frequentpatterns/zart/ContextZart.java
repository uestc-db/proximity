package ca.pfv.spmf.frequentpatterns.zart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class represents a transaction database, also called a binary context.
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
public class ContextZart {
	final Map<Integer, Integer> mapItemSupport = new HashMap<Integer, Integer>(); 

	final List<ItemsetZart> objects = new LinkedList<ItemsetZart>();
	
	int transactionCount =0;
	
	public void addItemset(ItemsetZart itemset){
		objects.add(itemset);
	}

	public void loadFile(String path) throws IOException {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			while ((thisLine = myInput.readLine()) != null) {
				if(thisLine.charAt(0) != '#'){ 
					addTransaction(thisLine.split(" "));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
	}
	
	public void addTransaction(String attributs[]){
		// on suppose qu'il n'y a pas de ligne vide
		ItemsetZart itemset = new ItemsetZart();
		for(String attribute:  attributs){
			Integer item = new  Integer(Integer.parseInt(attribute));
			itemset.addItem(item);
			
			// update the support for this item
			Integer count = mapItemSupport.get(item);
			if (count == null) {
				mapItemSupport.put(item, 1);
			} else {
				mapItemSupport.put(item, ++count);
			}
		}
		objects.add(itemset);
		transactionCount++;
	}
	
	public void printContext(){
		System.out
		.println("===================  CONTEXTE SÉQUENCES ===================");
		int count = 0;
		for(ItemsetZart itemset : objects){ // pour chaque objet
			System.out.print("0" + count + ":  ");
			itemset.print();
			System.out.println("");
			count++;
		}
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer("===================  CONTEXTE SÉQUENCES ===================\n");
		int count = 0;
		for(ItemsetZart itemset : objects){ // pour chaque objet
			buffer.append(count);
			buffer.append(":  ");
			buffer.append(itemset.toString());
			buffer.append("\n");
			count++;
		}
		return buffer.toString();
	}
	
	public int size(){
		return objects.size();
	}

	public List<ItemsetZart> getObjects() {
		return objects;
	}
}
