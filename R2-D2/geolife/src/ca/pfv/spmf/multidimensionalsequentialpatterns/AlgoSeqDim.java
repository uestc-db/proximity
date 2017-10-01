package ca.pfv.spmf.multidimensionalsequentialpatterns;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.multidimensionalpatterns.AlgoDimApriori;
import ca.pfv.spmf.multidimensionalpatterns.MDPattern;
import ca.pfv.spmf.multidimensionalpatterns.MDPatterns;
import ca.pfv.spmf.multidimensionalpatterns.MDPatternsDatabase;
import ca.pfv.spmf.sequentialpatterns.AbstractAlgoPrefixSpan;
import ca.pfv.spmf.sequentialpatterns.Sequence;
import ca.pfv.spmf.sequentialpatterns.Sequences;

/**
 * Implementation of the SeqDim algorithm. Uses AlgoPrefixspan and the AlgoDim
 * algorithm. Based on the article of Helen Pinto et al. (2001).
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
public class AlgoSeqDim {
	protected MDSequences sequences = new MDSequences("FREQUENT MD-SEQUENCES");
	private long startTime;
	private long endTime;
	private boolean mineClosedPatterns = false;
	
	BufferedWriter writer = null;
	private int patternCount; // the number of frequent itemsets found (for
								// statistics)

	double maxMemory = 0;

	public MDSequences runAlgorithm(MDSequenceDatabase context,
			AbstractAlgoPrefixSpan algoPrefixSpan, AlgoDimApriori algoDim,
			boolean mineClosedPatterns, String output) throws IOException {

		maxMemory = 0;
		patternCount =0;
		startTime = System.currentTimeMillis();
		writer = new BufferedWriter(new FileWriter(output));
		
		this.mineClosedPatterns = mineClosedPatterns;
		
		// (1) First mine sequential patterns.
		Sequences sequencesFound = algoPrefixSpan.runAlgorithm(context
				.getSequenceDatabase());

		// (2) For each sequential pattern, form projected MD-Database
		// and then find MD-patterns within projected databases
		for (int j = 0; j < sequencesFound.getLevelCount(); j++) {
			List<Sequence> sequencesList = sequencesFound.getLevel(j);
			for (Sequence sequence : sequencesList) {
				// we have to calculate the minsupp absolute and relative here.
				if (sequence.getSequencesID().size() == 0) {
					System.out.println("TEST");
				}

				trySequence(sequence, context, algoPrefixSpan.getMinSupp(),
						algoDim);
			}
		}

		// (3) Eliminate non-closed multidimensional sequential patterns
		if (mineClosedPatterns) {
			removeRedundancy();
		}
		
		endTime = System.currentTimeMillis();
		checkMemory();
		writer.close();
		return sequences;
	}


	private void trySequence(Sequence sequence, MDSequenceDatabase contexte,
			double minsupp, AlgoDimApriori algoDim) throws IOException {
		// (a) Project the database
		MDPatternsDatabase newContexte = createProjectedDatabase(
				sequence.getSequencesID(), contexte.getPatternDatabase());

		// (b) Run the DIM algorithm
		double newMinSupp = minsupp * contexte.size() / newContexte.size();
		MDPatterns patterns = algoDim.runAlgorithm(newContexte, newMinSupp);

		// (c) Create MD-Sequences and add them
		for (int i = 0; i < patterns.getLevelCount(); i++) {
			for (MDPattern pattern : patterns.getLevel(i)) {
				MDSequence mdsequence = new MDSequence(0, pattern, sequence);
				mdsequence.setTransactioncount(pattern.getAbsoluteSupport()); 
				savePattern(sequence, mdsequence);
			}
		}

	}

	private void savePattern(Sequence sequence, MDSequence mdsequence) throws IOException {
		if(mineClosedPatterns == false){
			writeToFile(mdsequence);
		}else{
			sequences.addSequence(mdsequence, sequence.size());
		}
	}


	private void writeToFile(MDSequence mdsequence) throws IOException {
		StringBuffer buffer = new StringBuffer();
		buffer.append(mdsequence.getMdpattern().toStringShort());
		buffer.append(mdsequence.getSequence().toStringShort());
		buffer.append("  supp:");
		buffer.append(mdsequence.getAbsoluteSupport());
		writer.write(buffer.toString());
		writer.newLine();
		patternCount++;
	}
	
	
	/**
	 * Create a projected database from a set of IDs of MDpatterns to keep
	 * 
	 * @param patternsIds
	 *            The list of of MDPatterns to keep
	 * @param context
	 *            The initial context
	 * @return A new context containing only the MDPatterns to keep.
	 */
	private MDPatternsDatabase createProjectedDatabase(
			Set<Integer> patternsIds, MDPatternsDatabase context) {
		MDPatternsDatabase projectedDatabase = new MDPatternsDatabase();
		for (MDPattern pattern : context.getMDPatterns()) {
			if (patternsIds.contains(pattern.getId())) {
				projectedDatabase.addMDPattern(pattern);
			}
		}
		return projectedDatabase;
	}


	public void printStatistics(int objectsCount) {
		StringBuffer r = new StringBuffer(140);
		r.append("=============  SEQ-DIM - STATISTICS =============\n Total time ~ ");
		r.append(endTime - startTime);
		r.append(" ms\n");
		r.append(" max memory : ");
		r.append(maxMemory);
		r.append("\n Frequent sequences count : ");
		r.append(patternCount);
		System.out.println(r.toString());
//		sequences.printFrequentSequences(objectsCount);
		System.out
				.println("===================================================");
	}

	/**
	 * Eliminate non-closed multidimensional sequential patterns by simply
	 * looping and eliminating redundant patterns. This is necessary if we want
	 * to mine closed multi-dim. seq. patterns, because: closed sequential patt.
	 * mining + closed itemset mining != closed multi-dim seq. patt. mining. REF
	 * : Panida Songram, Veera Boonjing and Sarun Intakosum (2006)
	 * @throws IOException 
	 */
	private void removeRedundancy() throws IOException {

		// For each MDSequence
		for (int i = sequences.getLevels().size() - 1; i > 0; i--) {
			for (MDSequence sequence : sequences.getLevel(i)) {
				// We check if the sequence is included in another sequence
				// with a size greater or equal.
				boolean included = false;
				for (int j = i; j < sequences.getLevels().size() && !included; j++) {
					for (MDSequence sequence2 : sequences.getLevel(j)) {
						if (sequence != sequence2
								&& sequence2.getAbsoluteSupport() == sequence
										.getAbsoluteSupport()
								&& sequence2.contains(sequence)) {
							included = true;
							break;
						}
					}
				}
				if (!included) {
					writeToFile(sequence);
				}
			}
		}
	}


	private void checkMemory() {
		double currentMemory = ((double) ((double) (Runtime.getRuntime()
				.totalMemory() / 1024) / 1024))
				- ((double) ((double) (Runtime.getRuntime().freeMemory() / 1024) / 1024));
		if (currentMemory > maxMemory) {
			maxMemory = currentMemory;
		}
	}
	
}
