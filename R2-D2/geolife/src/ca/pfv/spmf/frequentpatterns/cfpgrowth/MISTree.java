package ca.pfv.spmf.frequentpatterns.cfpgrowth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is an implementation of a MISTree.
 * 
 * This implementation was made by Azadeh Soltani based on the FPGrowth
 * implementation by Philippe Fournier-Viger
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
public class MISTree {
	// List of items in the header table
	List<Integer> headerList = null;
	// List of pairs (item, node) of the header table
	Map<Integer, MISNode> mapItemNodes = new HashMap<Integer, MISNode>();

	// root of the tree
	MISNode root = new MISNode(); // null node

	/**
	 * Constructor
	 */
	MISTree() {

	}

	/**
	 * Method for adding a transaction to the fp-tree (for the initial
	 * construction of the FP-Tree).
	 * 
	 * @param transaction
	 */
	public void addTransaction(List<Integer> transaction) {
		MISNode currentNode = root;
		// For each item in the transaction
		for (Integer item : transaction) {
			// look if there is a node already in the FP-Tree
			MISNode child = currentNode.getChildWithID(item);
			if (child == null) {
				// there is no node, we create a new one
				MISNode newNode = new MISNode();
				newNode.itemID = item;
				newNode.parent = currentNode;
				// we link the new node to its parrent
				currentNode.childs.add(newNode);

				// we take this node as the current node for the next for loop
				// iteration
				currentNode = newNode;

				// We update the header table.
				// We check if there is already a node with this id in the
				// header table
				MISNode headernode = mapItemNodes.get(item);
				if (headernode == null) { // there is not
					mapItemNodes.put(item, newNode);
				} else { // there is
							// we find the last node with this id.
					while (headernode.nodeLink != null) {
						headernode = headernode.nodeLink;
					}
					headernode.nodeLink = newNode;
				}
			} else {
				// there is a node already, we update it
				child.counter++;
				currentNode = child;
			}
		}
	}

	/**
	 * Method for adding a prefixpath to a fp-tree.
	 * 
	 * @param prefixPath
	 *            The prefix path
	 * @param mapSupportBeta
	 *            The frequencies of items in the prefixpaths
	 * @param relativeMinsupp
	 */
	public void addPrefixPath(List<MISNode> prefixPath,
			Map<Integer, Integer> mapSupportBeta, int minMIS) {
		// the first element of the prefix path contains the path support
		int pathCount = prefixPath.get(0).counter;

		MISNode currentNode = root;
		// For each item in the transaction (in backward order)
		// (and we ignore the first element of the prefix path)
		for (int i = prefixPath.size() - 1; i >= 1; i--) {
			MISNode pathItem = prefixPath.get(i);
			// if the item is not frequent we skip it
			if (mapSupportBeta.get(pathItem.itemID) < minMIS) {
				continue;
			}

			// look if there is a node already in the FP-Tree
			MISNode child = currentNode.getChildWithID(pathItem.itemID);
			if (child == null) {
				// there is no node, we create a new one
				MISNode newNode = new MISNode();
				newNode.itemID = pathItem.itemID;
				newNode.parent = currentNode;
				newNode.counter = pathCount; // SPECIAL
				currentNode.childs.add(newNode);
				currentNode = newNode;
				// We update the header table.
				// We check if there is already a node with this id in the
				// header table
				MISNode headernode = mapItemNodes.get(pathItem.itemID);
				if (headernode == null) { // there is not
					mapItemNodes.put(pathItem.itemID, newNode);
				} else { // there is
							// we find the last node with this id.
					while (headernode.nodeLink != null) {
						headernode = headernode.nodeLink;
					}
					headernode.nodeLink = newNode;
				}
			} else {
				// there is a node already, we update it
				child.counter += pathCount;
				currentNode = child;
			}
		}
	}

	/**
	 * Mehod for creating the list of items in the header table, in descending
	 * order of frequency.
	 * 
	 * @param itemComparator
	 * @param mapSupport
	 *            the frequencies of each item.
	 */
	// az--------------------------
	public void createHeaderList(Comparator<Integer> itemComparator) {
		headerList = new ArrayList<Integer>(mapItemNodes.keySet());
		Collections.sort(headerList, itemComparator);
	}

	public void deleteFromHeaderList(int item,
			Comparator<Integer> itemComparator) {
		int index = Collections.binarySearch(headerList, item, itemComparator);
		headerList.remove(index);
	}

	public void MISPrunning(int item) {
		MISNode headernode = mapItemNodes.get(item);
		while (headernode != null) {
			// if it is a leaf then remove the link directly
			if (headernode.childs.isEmpty()) {
				headernode.parent.childs.remove(headernode);
			}// if
				// removed the node and parent node will be linked to its child
				// node
			else {
				headernode.parent.childs.remove(headernode); // pfv : moved it
																// before adding
																// the childs
				headernode.parent.childs.addAll(headernode.childs);
				for (MISNode node : headernode.childs) {
					node.parent = headernode.parent;
				}
			}// else
			headernode = headernode.nodeLink;
		}// while
	}

	public void MISMerge(MISNode treeRoot) {
		// stop recursion
		if (treeRoot == null)
			return;

		for(int i=0; i< treeRoot.childs.size(); i++){
			MISNode node1 = treeRoot.childs.get(i);
			for(int j=i+1; j< treeRoot.childs.size(); j++){
				MISNode node2 = treeRoot.childs.get(j);
				
				if (node2.itemID == node1.itemID) {
					// (1) merge node1 and node2 
					node1.counter += node2.counter;
					node1.childs.addAll(node2.childs);
					// remove node 2 from child list
					treeRoot.childs.remove(j);   
					j--;                         
					
					// (2) remove node2 from the header list
					// If node2 is the first item in the header list:
					MISNode headernode = mapItemNodes.get(node1.itemID);   
					if(headernode == node2){
						mapItemNodes.put(node2.itemID, node2.nodeLink);
					}
					else{// Otherwise, search for node 2 and then remove it	 p
						while (headernode.nodeLink != node2){ 
							headernode = headernode.nodeLink;
						}
						headernode.nodeLink = headernode.nodeLink.nodeLink; // fix nodelink
					}
				}// if
			}
		}
		// for all children, merge their children
		for (MISNode node1 : treeRoot.childs){
			MISMerge(node1);
		}
	}

	public void print(MISNode TRoot) {
		// char a[]={'z','a','b','c','d','e','f','g','h'};
		// prefix print
		if (TRoot.itemID != -1)
			System.out.print(TRoot.itemID);
		System.out.print(' ');
		for (MISNode node : TRoot.childs) {
			print(node);
		}

	}
}