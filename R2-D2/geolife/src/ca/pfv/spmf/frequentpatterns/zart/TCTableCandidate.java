package ca.pfv.spmf.frequentpatterns.zart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* This class represents the TC table.
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
public class TCTableCandidate {

	public final List<List<ItemsetZart>> levels = new ArrayList<List<ItemsetZart>>();  // itemset classé par taille
	
	Map<ItemsetZart, Integer> mapPredSupp = new HashMap<ItemsetZart, Integer>();
	Map<ItemsetZart, Boolean> mapKey = new HashMap<ItemsetZart, Boolean>();
	Map<ItemsetZart, Integer> mapSupport = new HashMap<ItemsetZart, Integer>();
	
	public boolean thereisARowKeyValueIsTrue(int i) {
		for(ItemsetZart c : levels.get(i)){
			if(mapKey.get(c) == true){
				return true;
			}
		}
		return false;
	}

}
