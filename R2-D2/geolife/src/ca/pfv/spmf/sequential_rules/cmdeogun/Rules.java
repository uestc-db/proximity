package ca.pfv.spmf.sequential_rules.cmdeogun;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a set of sequential rules.
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
public class Rules {
	public final List<Rule> rules = new ArrayList<Rule>();  // rules
	
	private final String name;
	
	public Rules(String name){
		this.name = name;
	}
	
	public void printRules(int objectsCount){
		System.out.println(" ------- " + name + " -------");
		int i=0;
		for(Rule rule : rules){
			System.out.print("  rule " + i + ":  ");
			rule.print();
			System.out.print("support: " + rule.getRelativeSupport(objectsCount) +
					" (" + rule.getSupportAbsolu() + "/" + objectsCount + ")  ");
			System.out.print("confidence: " + rule.getConfidence());

			System.out.println("");
			i++;
		}
		System.out.println(" --------------------------------");
	}
	
	public void addRule(Rule rule){
		rules.add(rule);
	}
	
	public int getRulesCount(){
		return rules.size();
	}

	public List<Rule> getRules() {
		return rules;
	}
}
