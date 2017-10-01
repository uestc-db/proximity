package ca.pfv.spmf.frequentpatterns.cfpgrowth;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of a MISTree node.
 *
 * This implementation was made by Azadeh Soltani based on the FPGrowth
 * implementation by Philippe Fournier-Viger
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
public class MISNode {
	int itemID = -1;  // item id
	int counter = 1;  // frequency counter
	
	MISNode parent = null; 
	List<MISNode> childs = new ArrayList<MISNode>();
	
	MISNode nodeLink = null; // link to next node with the same item id (for the header table).
	
	/**
	 * constructor
	 */
	MISNode(){
		
	}

	/**
	 * Return the immmediate child of this node having a given ID.
	 * If there is no such child, return null;
	 */
	public MISNode getChildWithID(int id) {
		for(MISNode child : childs){
			if(child.itemID == id){
				return child;
			}
		}
		return null;
	}
	public int getChildIndexWithID(int id) {
		int i=0;
		for(MISNode child : childs){
			if(child.itemID == id){
				return i;
			}
			i++;
		}
		return -1;
	}
}
