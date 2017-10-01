package ca.pfv.spmf.gui;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

//import ca.pfv.spmf.associationrules.TNR.AlgoTNR;
import ca.pfv.spmf.associationrules.TopKRules.AlgoTopKRules;
import ca.pfv.spmf.associationrules.TopKRules.Database;
import ca.pfv.spmf.associationrules.agrawal_FPGrowth_version_saveToFile.AlgoAgrawalFaster94_FPGrowth_version_saveToFile;
import ca.pfv.spmf.associationrules.indirect_rules_saveToFile.AlgoINDIRECT;
import ca.pfv.spmf.clustering.hierarchical_clustering.AlgoHierarchicalClustering;
import ca.pfv.spmf.clustering.kmeans.AlgoKMeans;
import ca.pfv.spmf.frequentpatterns.MSApriori_optimized.AlgoMSApriori_saveToFile;
import ca.pfv.spmf.frequentpatterns.apriori.AlgoAprioriClose_saveToFile;
import ca.pfv.spmf.frequentpatterns.apriori.AlgoAprioriInverse_saveToFile;
import ca.pfv.spmf.frequentpatterns.apriori.AlgoAprioriRare_saveToFile;
import ca.pfv.spmf.frequentpatterns.apriori.ContextApriori;
import ca.pfv.spmf.frequentpatterns.aprioriTID_saveToFile.AlgoAprioriTID_File;
import ca.pfv.spmf.frequentpatterns.cfpgrowth.AlgoCFPGrowth_saveToFile;
import ca.pfv.spmf.frequentpatterns.charm_bitset_saveToFile.AlgoCharm_Bitset_saveToFile;
import ca.pfv.spmf.frequentpatterns.dci_closed_optimized.AlgoDCI_Closed_Optimized;
import ca.pfv.spmf.frequentpatterns.eclat_bitset_saveToFile.AlgoEclat_Bitset_saveToFile;
import ca.pfv.spmf.frequentpatterns.fpgrowth.Itemsets;
import ca.pfv.spmf.frequentpatterns.fpgrowth_saveToFile.AlgoFPGrowth;
import ca.pfv.spmf.frequentpatterns.hmine.AlgoHMine;
import ca.pfv.spmf.frequentpatterns.relim.AlgoRelim;
import ca.pfv.spmf.frequentpatterns.uapriori.AlgoUApriori_saveToFile;
import ca.pfv.spmf.frequentpatterns.vme.AlgoVME;
import ca.pfv.spmf.frequentpatterns.zart.AlgoZart;
import ca.pfv.spmf.frequentpatterns.zart.ContextZart;
import ca.pfv.spmf.frequentpatterns.zart.TFTableFrequent;
import ca.pfv.spmf.frequentpatterns.zart.TZTableClosed;
import ca.pfv.spmf.highutility.two_phase_algorithm.AlgoTwoPhase;
import ca.pfv.spmf.multidimensionalpatterns.AlgoDimApriori;
import ca.pfv.spmf.multidimensionalsequentialpatterns.AlgoSeqDim;
import ca.pfv.spmf.multidimensionalsequentialpatterns.MDSequenceDatabase;
import ca.pfv.spmf.sequential_rules.cmdeogun.AlgoCMDeogun;
import ca.pfv.spmf.sequential_rules.cmrules.AlgoCMRules;
import ca.pfv.spmf.sequential_rules.rulegen.AlgoRuleGen;
import ca.pfv.spmf.sequential_rules.rulegrowth.AlgoRULEGROWTH;
import ca.pfv.spmf.sequential_rules.topseqrules.AlgoTopSeqRules;
import ca.pfv.spmf.sequential_rules.trulegrowth.AlgoTRuleGrowth;
import ca.pfv.spmf.sequential_rules.trulegrowth_with_strings.AlgoTRuleGrowth_withStrings;
import ca.pfv.spmf.sequentialpatterns.AlgoFournierViger08;
import ca.pfv.spmf.sequentialpatterns.BIDEPlus_saveToFile.AlgoBIDEPlus;
import ca.pfv.spmf.sequentialpatterns.BIDEPlus_saveToFile_withStrings.AlgoBIDEPlus_withStrings;
import ca.pfv.spmf.sequentialpatterns.prefixspan_for_use_with_multidimensional_pattern_mining.AlgoPrefixSpanMDSPM;
import ca.pfv.spmf.sequentialpatterns.prefixspan_saveToFile.AlgoPrefixSpan;
import ca.pfv.spmf.sequentialpatterns.prefixspan_saveToFile.SequenceDatabase;
import ca.pfv.spmf.sequentialpatterns.prefixspan_with_strings_saveToFile.AlgoPrefixSpanStrings_saveToFile;
import ca.pfv.spmf.sequentialpatterns.spam_saveToFile.AlgoSPAM;
import ca.pfv.spmf.tests.MainTestApriori_saveToFile;
import ca.pfv.spmf.tests.MainTestPrefixSpan_saveToFile;
/**
 * This is a simple user interface for the main algorithms from SPMF.
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
public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextField textFieldParam1;
	private JTextField textFieldParam2;
	private JTextField textFieldParam3;
	private JTextField textFieldParam4;
	private JTextField textFieldParam5;
	private JTextField textFieldParam6;
	
	private JLabel labelParam1;
	private JLabel labelParam2;
	private JLabel labelParam3;
	private JLabel labelParam4;
	private JLabel labelParam5;
	private JLabel labelParam6;
	
	private JLabel lbHelp1;
	private JLabel lbHelp2;
	private JLabel lbHelp3;
	private JLabel lbHelp4;
	private JLabel lbHelp5;
	private JLabel lbHelp6;
	private JTextField textFieldInput;
	private JTextField textFieldOutput;
	
	// IMPORTANT
	private String inputFile =  null;
	private String outputFile =  null;
	private JComboBox<String> comboBox;
	private JTextArea textArea;
	private JButton buttonRun;
	private JCheckBox checkboxOpenOutput;
	private JButton buttonExample;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainWindow() {
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent arg0) {
				System.exit(0);
			}
		});
		setTitle("SPMF GUI");
//		setIconImage(Toolkit.getDefaultToolkit().getImage("spmf.png"));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 575, 561);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		comboBox = new JComboBox<String>(new Vector<String>());
		comboBox.setMaximumRowCount(20);
		comboBox.addItem("");
		comboBox.addItem("  ---- SEQUENTIAL PATTERN MINING ----");
		comboBox.addItem("PrefixSpan");
		comboBox.addItem("PrefixSpan with strings");
		comboBox.addItem("SPAM");
		comboBox.addItem("BIDE+");
		comboBox.addItem("BIDE+ with strings");
		comboBox.addItem("SeqDim (PrefixSpan+Apriori)");
		comboBox.addItem("SeqDim (PrefixSpan+Apriori)+time");
		comboBox.addItem("SeqDim (BIDE+AprioriClose)");
		comboBox.addItem("SeqDim (BIDE+AprioriClose)+time");
		comboBox.addItem("SeqDim (BIDE+Charm)");
		comboBox.addItem("SeqDim (BIDE+Charm)+time");
		comboBox.addItem("  ---- SEQUENTIAL RULE MINING ----");
		comboBox.addItem("RuleGrowth");
		comboBox.addItem("TRuleGrowth");
		comboBox.addItem("TRuleGrowth with strings");
		comboBox.addItem("CMRules");
		comboBox.addItem("CMDeo");
		comboBox.addItem("RuleGen");
		comboBox.addItem("TopSeqRules");
		comboBox.addItem("  ---- ITEMSET MINING----");
		comboBox.addItem("FPGrowth - itemsets");
		comboBox.addItem("Apriori");
		comboBox.addItem("Apriori_TID_bitset");
		comboBox.addItem("AprioriClose");
		comboBox.addItem("AprioriRare");
		comboBox.addItem("AprioriInverse");
		comboBox.addItem("Relim");
		comboBox.addItem("VME");
		comboBox.addItem("Eclat_bitset");
		comboBox.addItem("Zart");
		comboBox.addItem("HMine");
		comboBox.addItem("DCI_Closed");
		comboBox.addItem("Charm_bitset");
		comboBox.addItem("Indirect");
		comboBox.addItem("UApriori");
		comboBox.addItem("Two-Phase");
		comboBox.addItem("MSApriori");
		comboBox.addItem("CFPGrowth");
		comboBox.addItem("  ---- ASSOCIATION RULE MINING ----");
		comboBox.addItem("TopKRules");
//		comboBox.addItem("TNR");
		comboBox.addItem("FPGrowth - association rules");
		comboBox.addItem("FPGrowth - association rules with lift");
		comboBox.addItem("  ---- CLUSTERING ----");
		comboBox.addItem("KMeans");
		comboBox.addItem("Hierarchical clustering");
		
		comboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				// COMBOBOX ITEM SELECTION - ITEM STATE CHANGED
				if(evt.getStateChange() == ItemEvent.SELECTED){
					buttonRun.setEnabled(true);
					buttonExample.setEnabled(true);
					
					if("SPAM".equals(evt.getItem())
							|| "PrefixSpan".equals(evt.getItem())
							|| "SeqDim (PrefixSpan+Apriori)".equals(evt.getItem())
							|| "SeqDim (BIDE+AprioriClose)".equals(evt.getItem())
							|| "SeqDim (BIDE+Charm)".equals(evt.getItem())
							|| "PrefixSpan with strings".equals(evt.getItem())
						|| "BIDE+".equals(evt.getItem())
						|| "BIDE+ with strings".equals(evt.getItem())
						|| "FPGrowth - itemsets".equals(evt.getItem())
						|| "Apriori".equals(evt.getItem())
						|| "Apriori_TID_bitset".equals(evt.getItem())
						|| "AprioriClose".equals(evt.getItem())
						|| "AprioriRare".equals(evt.getItem())
						|| "Charm_bitset".equals(evt.getItem())
						|| "Relim".equals(evt.getItem())
						|| "Eclat_bitset".equals(evt.getItem())
						|| "Zart".equals(evt.getItem())
						)
					{
						hideAllParams();
						setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g. 0.4 or 40%)");
					}else if("SeqDim (PrefixSpan+Apriori)+time".equals(evt.getItem())
							|| "SeqDim (BIDE+AprioriClose)+time".equals(evt.getItem())
							|| "SeqDim (BIDE+Charm)+time".equals(evt.getItem())
							)
						{
							hideAllParams();
							setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g.  0.5  or 50 %)");
							setParam(textFieldParam2, "Choose minInterval:", labelParam2, "(e.g.  1)");
							setParam(textFieldParam3, "Choose maxInterval:", labelParam3, "(e.g.  5)");
							setParam(textFieldParam4, "Choose minWholeInterval:", labelParam4, "(e.g.  1)");
							setParam(textFieldParam5, "Choose maxWholeInterval:", labelParam5, "(e.g.  5)");
						}
					else if("HMine".equals(evt.getItem())
							|| "DCI_Closed".equals(evt.getItem())
							)
						{
							hideAllParams();
							setParam(textFieldParam1, "Choose minsup (integer):", labelParam1, "(e.g. 2)");
						}else if("VME".equals(evt.getItem())
								)
							{
								hideAllParams();
								setParam(textFieldParam1, "Choose threshold (%):", labelParam1, "(e.g. 0.15 or 15%)");
							}else if("AprioriInverse".equals(evt.getItem())
								)
							{
								hideAllParams();
								setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g. 0.001 or 0.1%)");
								setParam(textFieldParam2, "Choose maxsup (%):", labelParam2, "(e.g. 0.06 or 6%)");
							}else if("UApriori".equals(evt.getItem())
									)
								{
									hideAllParams();
									setParam(textFieldParam1, "Choose expected support (%):", labelParam1, "(e.g. 0.10)");
								}else if( "FPGrowth - association rules".equals(evt.getItem())
							|| "RuleGrowth".equals(evt.getItem())
							|| "CMRules".equals(evt.getItem())
							|| "CMDeo".equals(evt.getItem())
						){
						hideAllParams();
						setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g. 0.5 or 50%)");
						setParam(textFieldParam2, "Choose minconf (%):", labelParam2, "(e.g. 0.6 or 60%)");
					}else if( "RuleGen".equals(evt.getItem())
						){
						hideAllParams();
						setParam(textFieldParam1, "Choose minsup (integer):", labelParam1, "(e.g. 3)");
						setParam(textFieldParam2, "Choose minconf (%):", labelParam2, "(e.g. 0.6 or 60%)");
					}else if( "KMeans".equals(evt.getItem())
							){
							hideAllParams();
							setParam(textFieldParam1, "Choose K:", labelParam1, "(e.g. 3)");
					}else if( "Hierarchical clustering".equals(evt.getItem())){
						hideAllParams();
						setParam(textFieldParam1, "Choose max distance:", labelParam1, "(e.g. 4)");
					}else if( "FPGrowth - association rules with lift".equals(evt.getItem())){
						hideAllParams();
						setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g. 0.5 or 50%)");
						setParam(textFieldParam2, "Choose minconf (%):", labelParam2, "(e.g. 0.6 or 60%)");
						setParam(textFieldParam3, "Choose minlift:", labelParam3, "(e.g. 0.2)");
					}else if("TopSeqRules".equals(evt.getItem())
							|| "TopKRules".equals(evt.getItem())){
						hideAllParams();
						setParam(textFieldParam1, "Choose k:", labelParam1, "(e.g. 3)");
						setParam(textFieldParam2, "Choose minconf (%):", labelParam2, "(e.g. 0.8 or 80%)");
					}
					else if("TNR".equals(evt.getItem())){
						hideAllParams();
						setParam(textFieldParam1, "Choose k:", labelParam1, "(e.g. 3)");
						setParam(textFieldParam2, "Choose minconf (%):", labelParam2, "(e.g. 0.8 or 80%)");
						setParam(textFieldParam3, "Choose delta:", labelParam3, "(e.g. 2)");
					}
					else if("Two-Phase".equals(evt.getItem())){
						hideAllParams();
						setParam(textFieldParam1, "Choose minutility:", labelParam1, "(e.g. 30)");
					}else if("MSApriori".equals(evt.getItem())){
						hideAllParams();
						setParam(textFieldParam1, "Choose beta:", labelParam1, "(e.g. 0.4 or 40%)");
						setParam(textFieldParam2, "Choose LS:", labelParam2, "(e.g. 0.2 or 20%)");
					}else if("CFPGrowth".equals(evt.getItem())){
						hideAllParams();
						setParam(textFieldParam1, "MIS file name:", labelParam1, "(e.g. MIS.txt)");
					}
					else if("TRuleGrowth".equals(evt.getItem()) ||
							"TRuleGrowth with strings".equals(evt.getItem())){
						hideAllParams();
						setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g. 0.7 or 70%)");
						setParam(textFieldParam2, "Choose minconf (%):", labelParam2, "(e.g. 0.8 or 80%)");
						setParam(textFieldParam3, "Choose window_size:", labelParam3, "(e.g. 3)");
					}else if("Indirect".equals(evt.getItem())
							){
							hideAllParams();
							setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g. 0.6 or 60%)");
							setParam(textFieldParam2, "Choose ts (%):", labelParam2, "(e.g. 0.50 or 50%)");
							setParam(textFieldParam3, "Choose minconf (%):", labelParam3, "(e.g. 0.1 or 10%)");
					}else {
						hideAllParams();
						buttonRun.setEnabled(false);
						buttonExample.setEnabled(false);
					}
				}else{
					hideAllParams();
					buttonRun.setEnabled(false);
					buttonExample.setEnabled(false);
				}
			}
		});
		comboBox.setBounds(197, 73, 306, 20);
		contentPane.add(comboBox);
		
	//  RUN ALGORITHM BUTTON
		buttonRun = new JButton("Run algorithm");
		buttonRun.setEnabled(false);
		buttonRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try{
					textArea.setText("");
					
					String choice = (String)comboBox.getSelectedItem();
					if("PrefixSpan".equals(choice)){
						SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
						sequenceDatabase.loadFile(inputFile);
//						sequenceDatabase.print();
						int minsup = (int)(getParamAsDouble(textFieldParam1) * sequenceDatabase.size()); // we use a minimum support of 2 sequences.
						
						AlgoPrefixSpan algo = new AlgoPrefixSpan(); 
						algo.runAlgorithm(sequenceDatabase, outputFile, minsup);    
						algo.printStatistics(sequenceDatabase.size());
					}else if("PrefixSpan with strings".equals(choice)){
		
						ca.pfv.spmf.sequentialpatterns.prefixspan_with_strings_saveToFile.SequenceDatabase sequenceDatabase = new ca.pfv.spmf.sequentialpatterns.prefixspan_with_strings_saveToFile.SequenceDatabase(); 
						sequenceDatabase.loadFile(fileToPath("contextPrefixSpanStrings.txt"));
						
						// Create an instance of the algorithm with minsup = 50 %
						AlgoPrefixSpanStrings_saveToFile algo = new AlgoPrefixSpanStrings_saveToFile(); 
						
						int minsup = (int)(getParamAsDouble(textFieldParam1) * sequenceDatabase.size()); // we use a minimum support of 2 sequences.
						
						// execute the algorithm
						algo.runAlgorithm(sequenceDatabase, outputFile, minsup);    
						algo.printStatistics(sequenceDatabase.size());
					}else if("SeqDim (PrefixSpan+Apriori)".equals(choice)){
						
						double minsup = getParamAsDouble(textFieldParam1); // we use a minimum support of 2 sequences.
						
						MDSequenceDatabase contextMDDatabase  = new MDSequenceDatabase(); //
						contextMDDatabase.loadFile(inputFile);
//						contextMDDatabase.printContext();
						
						// If the second boolean is true, the algorithm will use
						// CHARM instead of AprioriClose for mining frequent closed itemsets.
						// This options is offered because on some database, AprioriClose does not
						// perform very well. Other algorithms could be added.
						AlgoDimApriori algoDim = new AlgoDimApriori(false, false);
						
						AlgoSeqDim algoSeqDim = new AlgoSeqDim();
						
						// Apply algorithm
						AlgoPrefixSpanMDSPM prefixSpan = new AlgoPrefixSpanMDSPM(minsup);  
						algoSeqDim.runAlgorithm(contextMDDatabase, prefixSpan, algoDim, false, outputFile);
						
						// Print results
						algoSeqDim.printStatistics(contextMDDatabase.size());
					}else if("SeqDim (PrefixSpan+Apriori)+time".equals(choice)){
						
						double minsup = getParamAsDouble(textFieldParam1); // we use a minimum support of 2 sequences.
						double minInterval = getParamAsDouble(textFieldParam2);
						double maxInterval = getParamAsDouble(textFieldParam3);
						double minWholeInterval = getParamAsDouble(textFieldParam4);
						double maxWholeInterval = getParamAsDouble(textFieldParam5);
						
						
						MDSequenceDatabase contextMDDatabase  = new MDSequenceDatabase(); //
						contextMDDatabase.loadFile(inputFile);
//						contextMDDatabase.printContext();
						
						AlgoDimApriori algoDim = new AlgoDimApriori(false, false); // <-- here
						
						AlgoSeqDim algoSeqDim2 = new AlgoSeqDim();
						
						// Apply algorithm
						AlgoFournierViger08 algoPrefixSpanHirateClustering 
						= new AlgoFournierViger08(minsup,
								minInterval, maxInterval, minWholeInterval, maxWholeInterval, null, false, false);  
						algoSeqDim2.runAlgorithm(contextMDDatabase, algoPrefixSpanHirateClustering, algoDim, false, outputFile);
						
						// Print results
						algoSeqDim2.printStatistics(contextMDDatabase.size());
						// NOTE : IF YOU DON'T WANT TO MINE *CLOSED* MD-SEQUENCES, JUST CHANGE THE FOUR VALUES "true" for
						// "FALSE" in this example. 
					}else if("SeqDim (BIDE+AprioriClose)+time".equals(choice)){
						
						double minsup = getParamAsDouble(textFieldParam1); // we use a minimum support of 2 sequences.
						double minInterval = getParamAsDouble(textFieldParam2);
						double maxInterval = getParamAsDouble(textFieldParam3);
						double minWholeInterval = getParamAsDouble(textFieldParam4);
						double maxWholeInterval = getParamAsDouble(textFieldParam5);
						
						
						MDSequenceDatabase contextMDDatabase  = new MDSequenceDatabase(); //
						contextMDDatabase.loadFile(inputFile);
//						contextMDDatabase.printContext();
						
						AlgoDimApriori algoDim = new AlgoDimApriori(true, false); // <-- here
						
						AlgoSeqDim algoSeqDim2 = new AlgoSeqDim();
						
						// Apply algorithm
						AlgoFournierViger08 algoPrefixSpanHirateClustering 
						= new AlgoFournierViger08(minsup,
								minInterval, maxInterval, minWholeInterval, maxWholeInterval, null, true, true);  
						algoSeqDim2.runAlgorithm(contextMDDatabase, algoPrefixSpanHirateClustering, algoDim, true, outputFile);
						
						// Print results
						algoSeqDim2.printStatistics(contextMDDatabase.size());
						// NOTE : IF YOU DON'T WANT TO MINE *CLOSED* MD-SEQUENCES, JUST CHANGE THE FOUR VALUES "true" for
						// "FALSE" in this example. 
					}else if("SeqDim (BIDE+Charm)+time".equals(choice)){
						
						double minsup = getParamAsDouble(textFieldParam1); // we use a minimum support of 2 sequences.
						double minInterval = getParamAsDouble(textFieldParam2);
						double maxInterval = getParamAsDouble(textFieldParam3);
						double minWholeInterval = getParamAsDouble(textFieldParam4);
						double maxWholeInterval = getParamAsDouble(textFieldParam5);
						
						
						MDSequenceDatabase contextMDDatabase  = new MDSequenceDatabase(); //
						contextMDDatabase.loadFile(inputFile);
//						contextMDDatabase.printContext();
						
						AlgoDimApriori algoDim = new AlgoDimApriori(false, true); // <-- here
						
						AlgoSeqDim algoSeqDim2 = new AlgoSeqDim();
						
						// Apply algorithm
						AlgoFournierViger08 algoPrefixSpanHirateClustering 
						= new AlgoFournierViger08(minsup,
								minInterval, maxInterval, minWholeInterval, maxWholeInterval, null, true, true);  
						algoSeqDim2.runAlgorithm(contextMDDatabase, algoPrefixSpanHirateClustering, algoDim, true, outputFile);
						
						// Print results
						algoSeqDim2.printStatistics(contextMDDatabase.size());
						// NOTE : IF YOU DON'T WANT TO MINE *CLOSED* MD-SEQUENCES, JUST CHANGE THE FOUR VALUES "true" for
						// "FALSE" in this example. 
					}
					
					
					
					
					else if("SeqDim (BIDE+AprioriClose)".equals(choice)){
						
						double minsup = getParamAsDouble(textFieldParam1); // we use a minimum support of 2 sequences.
						
						MDSequenceDatabase contextMDDatabase  = new MDSequenceDatabase(); //
						contextMDDatabase.loadFile(inputFile);
//						contextMDDatabase.printContext();
						
						AlgoDimApriori algoDim = new AlgoDimApriori(true, false);
						
						AlgoSeqDim algoSeqDim = new AlgoSeqDim();
						
						// Apply algorithm
						ca.pfv.spmf.sequentialpatterns.AlgoBIDEPlus bideplus = new ca.pfv.spmf.sequentialpatterns.AlgoBIDEPlus(minsup);  
						algoSeqDim.runAlgorithm(contextMDDatabase, bideplus, algoDim, true, outputFile);
						
						// Print results
						algoSeqDim.printStatistics(contextMDDatabase.size());
					}else if("SeqDim (BIDE+Charm)".equals(choice)){
						
						double minsup = getParamAsDouble(textFieldParam1); // we use a minimum support of 2 sequences.
							
						MDSequenceDatabase contextMDDatabase  = new MDSequenceDatabase(); //
						contextMDDatabase.loadFile(inputFile);
//						contextMDDatabase.printContext();
						
						AlgoDimApriori algoDim = new AlgoDimApriori(false, true);
						
						AlgoSeqDim algoSeqDim = new AlgoSeqDim();
						
						// Apply algorithm
						ca.pfv.spmf.sequentialpatterns.AlgoBIDEPlus bideplus = new ca.pfv.spmf.sequentialpatterns.AlgoBIDEPlus(minsup);  
						algoSeqDim.runAlgorithm(contextMDDatabase, bideplus, algoDim, true, outputFile);
						
						// Print results
						algoSeqDim.printStatistics(contextMDDatabase.size());
					}
					else if("SPAM".equals(choice)){
						AlgoSPAM algo = new AlgoSPAM(); 
						algo.runAlgorithm(inputFile, outputFile, getParamAsDouble(textFieldParam1));    
						algo.printStatistics();
					}else if("BIDE+".equals(choice)){
						ca.pfv.spmf.sequentialpatterns.BIDEPlus_saveToFile.SequenceDatabase sequenceDatabase = new ca.pfv.spmf.sequentialpatterns.BIDEPlus_saveToFile.SequenceDatabase(); 
						sequenceDatabase.loadFile(inputFile);
//						sequenceDatabase.print();
						int minsup = (int)(getParamAsDouble(textFieldParam1) * sequenceDatabase.size()); // we use a minimum support of 2 sequences.
						
						AlgoBIDEPlus algo  = new AlgoBIDEPlus(); 
						algo.runAlgorithm(sequenceDatabase, outputFile, minsup);    
						algo.printStatistics(sequenceDatabase.size());
					}else if("BIDE+ with strings".equals(choice)){
						ca.pfv.spmf.sequentialpatterns.BIDEPlus_saveToFile_withStrings.SequenceDatabase sequenceDatabase = new ca.pfv.spmf.sequentialpatterns.BIDEPlus_saveToFile_withStrings.SequenceDatabase(); 
						sequenceDatabase.loadFile(inputFile);
//						sequenceDatabase.print();
						int minsup = (int)(getParamAsDouble(textFieldParam1) * sequenceDatabase.size()); // we use a minimum support of 2 sequences.
						
						AlgoBIDEPlus_withStrings algo  = new AlgoBIDEPlus_withStrings(); 
						algo.runAlgorithm(sequenceDatabase, outputFile, minsup);    
						algo.printStatistics(sequenceDatabase.size());
					}else if("RuleGrowth".equals(choice)){
						double minsup = getParamAsDouble(textFieldParam1);
						double minconf = getParamAsDouble(textFieldParam2);

					   AlgoRULEGROWTH algo = new AlgoRULEGROWTH();
					   algo.runAlgorithm(minsup, minconf, inputFile, outputFile);
					   algo.printStats();
					}else if("TRuleGrowth".equals(choice)){
						double minsup = getParamAsDouble(textFieldParam1);
						double minconf = getParamAsDouble(textFieldParam2);
						int window = getParamAsInteger(textFieldParam3);

					   AlgoTRuleGrowth algo = new AlgoTRuleGrowth();
					   algo.runAlgorithm(minsup, minconf, inputFile, outputFile, window);
					   algo.printStats();
					}else if("TRuleGrowth with strings".equals(choice)){
						double minsup = getParamAsDouble(textFieldParam1);
						double minconf = getParamAsDouble(textFieldParam2);
						int window = getParamAsInteger(textFieldParam3);

					   AlgoTRuleGrowth_withStrings algo = new AlgoTRuleGrowth_withStrings();
					   algo.runAlgorithm(minsup, minconf, inputFile, outputFile, window);
					   algo.printStats();
					}else if("CMRules".equals(choice)){
						double minsup = getParamAsDouble(textFieldParam1);
						double minconf = getParamAsDouble(textFieldParam2);
				 
						AlgoCMRules algo = new AlgoCMRules();
						algo.runAlgorithm(inputFile, outputFile, minsup, minconf);
						algo.printStats();
					}else if("CMDeo".equals(choice)){
						double minsup = getParamAsDouble(textFieldParam1);
						double minconf = getParamAsDouble(textFieldParam2);
						AlgoCMDeogun algo = new AlgoCMDeogun();
						algo.runAlgorithm(inputFile, outputFile, minsup, minconf);
						algo.printStats();
					}else if("RuleGen".equals(choice)){
						int minsup = getParamAsInteger(textFieldParam1);
						double minconf = getParamAsDouble(textFieldParam2);
						
						AlgoRuleGen rulegen = new AlgoRuleGen();
						rulegen.runAlgorithm(minsup, minconf, inputFile, outputFile);
						rulegen.printStats();
						
					}else if("TopSeqRules".equals(choice)){
						int k = getParamAsInteger(textFieldParam1);
						double minconf = getParamAsDouble(textFieldParam2);
						
						ca.pfv.spmf.sequential_rules.topseqrules.SequenceDatabase sequenceDatabase = new ca.pfv.spmf.sequential_rules.topseqrules.SequenceDatabase(); 
						sequenceDatabase.loadFile(inputFile);

						AlgoTopSeqRules algo = new AlgoTopSeqRules();
						 algo.runAlgorithm(k, sequenceDatabase, minconf);
						algo.printStats();
						algo.writeResultTofile(outputFile);   // to save results to file
					}else if("TopKRules".equals(choice)){
						Database database = new Database(); 
						database.loadFile(inputFile); 
						
						int k = getParamAsInteger(textFieldParam1);
						double minconf = getParamAsDouble(textFieldParam2);
						
						AlgoTopKRules algo = new AlgoTopKRules();
						algo.runAlgorithm(k, minconf, database);
						algo.printStats();
						algo.writeResultTofile(outputFile);   // to save results to file
					}else if("TNR".equals(choice)){
//						ca.pfv.spmf.associationrules.TNR.Database database = new ca.pfv.spmf.associationrules.TNR.Database(); 
//						database.loadFile(inputFile); 
//						
//						int k = getParamAsInteger(textFieldParam1);
//						double minconf = getParamAsDouble(textFieldParam2);
//						int delta = getParamAsInteger(textFieldParam3);
//						
//						AlgoTNR algo = new AlgoTNR();
//						algo.runAlgorithm(k, minconf, database, delta);
//						algo.printStats();
//						algo.writeResultTofile(outputFile);   // to save results to file
					}					
					
					else if("FPGrowth - itemsets".equals(choice)){
						double minsup = getParamAsDouble(textFieldParam1);
						AlgoFPGrowth algo = new AlgoFPGrowth();
						algo.runAlgorithm(inputFile, outputFile, minsup);
						algo.printStats();
					}else if("FPGrowth - association rules".equals(choice)){
						double minsup = getParamAsDouble(textFieldParam1);
						double minconf = getParamAsDouble(textFieldParam2);
						
						ca.pfv.spmf.frequentpatterns.fpgrowth.Database database = new ca.pfv.spmf.frequentpatterns.fpgrowth.Database();
						database.loadFile(inputFile);

						int databaseSize = database.size();
						ca.pfv.spmf.frequentpatterns.fpgrowth.AlgoFPGrowth fpgrowth = new ca.pfv.spmf.frequentpatterns.fpgrowth.AlgoFPGrowth();
						Itemsets patterns = fpgrowth.runAlgorithm(database, minsup);
						fpgrowth.printStats();
						
						// STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
						AlgoAgrawalFaster94_FPGrowth_version_saveToFile algoAgrawal = new AlgoAgrawalFaster94_FPGrowth_version_saveToFile(minconf);
						algoAgrawal.runAlgorithm(patterns, outputFile, databaseSize);
						algoAgrawal.printStats();
					}
					else if("FPGrowth - association rules with lift".equals(choice)){
							double minsup = getParamAsDouble(textFieldParam1);
							double minconf = getParamAsDouble(textFieldParam2);
							double minlift = getParamAsDouble(textFieldParam3);
							
							ca.pfv.spmf.frequentpatterns.fpgrowth.Database database = new ca.pfv.spmf.frequentpatterns.fpgrowth.Database();
							database.loadFile(inputFile);

							int databaseSize = database.size();
							ca.pfv.spmf.frequentpatterns.fpgrowth.AlgoFPGrowth fpgrowth = new ca.pfv.spmf.frequentpatterns.fpgrowth.AlgoFPGrowth();
							Itemsets patterns = fpgrowth.runAlgorithm(database, minsup);
							fpgrowth.printStats();
							
							// STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
							AlgoAgrawalFaster94_FPGrowth_version_saveToFile algoAgrawal = new AlgoAgrawalFaster94_FPGrowth_version_saveToFile(minconf, minlift);
							algoAgrawal.runAlgorithm(patterns, outputFile, databaseSize);
							algoAgrawal.printStats();
						}else if("Apriori_TID_bitset".equals(choice)){
						double minsup = getParamAsDouble(textFieldParam1);
						AlgoAprioriTID_File apriori = new AlgoAprioriTID_File();
						apriori.runAlgorithm(inputFile, outputFile, minsup);
						apriori.printStats();
					}else if("Apriori".equals(choice)){
						double minsup = getParamAsDouble(textFieldParam1);
						
						// Applying the Apriori algorithm, optimized version
						ca.pfv.spmf.frequentpatterns.apriori_optimized.AlgoApriori_saveToFile apriori = new ca.pfv.spmf.frequentpatterns.apriori_optimized.AlgoApriori_saveToFile();
						apriori.runAlgorithm(minsup, inputFile, outputFile);
						apriori.printStats();
					}else if("AprioriClose".equals(choice)){
						double minsup = getParamAsDouble(textFieldParam1);
						
						ContextApriori context = new ContextApriori();
						context.loadFile(inputFile);
						AlgoAprioriClose_saveToFile apriori = new AlgoAprioriClose_saveToFile(context);
						apriori.runAlgorithm(minsup, outputFile);
						apriori.printStats();
					}else if("AprioriRare".equals(choice)){
						double minsup = getParamAsDouble(textFieldParam1);
						
						ContextApriori context = new ContextApriori();
						context.loadFile(inputFile);
						AlgoAprioriRare_saveToFile apriori = new AlgoAprioriRare_saveToFile(context);
						apriori.runAlgorithm(minsup, outputFile);
						apriori.printStats();
					}else if("AprioriInverse".equals(choice)){
						double minsup = getParamAsDouble(textFieldParam1);
						double maxsup = getParamAsDouble(textFieldParam2);
						
						ContextApriori context = new ContextApriori();
						context.loadFile(inputFile);
						AlgoAprioriInverse_saveToFile apriori = new AlgoAprioriInverse_saveToFile(context);
						apriori.runAlgorithm(minsup, maxsup, outputFile);
						apriori.printStats();
					}else if("MSApriori".equals(choice)){
						double beta = getParamAsDouble(textFieldParam1);
						double ls = getParamAsDouble(textFieldParam2);
												
						// Applying the MSApriori algorithm
						AlgoMSApriori_saveToFile apriori = new AlgoMSApriori_saveToFile();
						apriori.runAlgorithm(inputFile, outputFile, beta, ls);
						apriori.printStats();
					}else if("CFPGrowth".equals(choice)){
						String misFile = textFieldParam1.getText();
						
						File file = new File(inputFile);
						String misFileFullPath = file.getParent() + File.separator + misFile;
												
						// Applying the  algorithm
						AlgoCFPGrowth_saveToFile algo = new AlgoCFPGrowth_saveToFile();
						algo.runAlgorithm(inputFile, outputFile, misFileFullPath);
						algo.printStats();
					}
					else if("VME".equals(choice)){
						double threshold = getParamAsDouble(textFieldParam1);
												
						// Applying the  algorithm
						AlgoVME algo = new AlgoVME();
						algo.runAlgorithm(inputFile, outputFile, threshold);
						algo.printStats();
					}else if("KMeans".equals(choice)){
						int k= getParamAsInteger(textFieldParam1);

						// Apply the algorithm
						AlgoKMeans algoKMeans = new AlgoKMeans(); 
						algoKMeans.runAlgorithm(inputFile, k);
						algoKMeans.printStatistics();
						algoKMeans.saveToFile(outputFile);
					}else if("HierarchicalClustering".equals(choice)){
						int maxDistance = getParamAsInteger(textFieldParam1);

						// Apply the algorithm
						AlgoHierarchicalClustering algo = new AlgoHierarchicalClustering(); 
						algo.runAlgorithm(inputFile, maxDistance);
						algo.printStatistics();
						algo.saveToFile(outputFile);
					}
					else if("UApriori".equals(choice)){
						double expectedsup = getParamAsDouble(textFieldParam1);
						
						ca.pfv.spmf.frequentpatterns.uapriori.ContextApriori context = new ca.pfv.spmf.frequentpatterns.uapriori.ContextApriori();
						context.loadFile(inputFile);
						AlgoUApriori_saveToFile apriori = new AlgoUApriori_saveToFile(context);
						apriori.runAlgorithm(expectedsup, outputFile);
						apriori.printStats();
					}
					else if("HMine".equals(choice)){
						int minsup = getParamAsInteger(textFieldParam1);
						AlgoHMine algorithm = new AlgoHMine();
						algorithm.runAlgorithm(inputFile, outputFile, minsup);
						algorithm.printStatistics();
					}else if("DCI_Closed".equals(choice)){
						int minsup = getParamAsInteger(textFieldParam1);
						AlgoDCI_Closed_Optimized algorithm = new AlgoDCI_Closed_Optimized();
						algorithm.runAlgorithm(inputFile, outputFile, minsup);
					}else if("Indirect".equals(choice)){
						double minsup = getParamAsDouble(textFieldParam1);
						double ts = getParamAsDouble(textFieldParam2);
						double minconf = getParamAsDouble(textFieldParam3);
						AlgoINDIRECT indirect = new AlgoINDIRECT();
						indirect.runAlgorithm(inputFile, outputFile, minsup, ts, minconf);
						indirect.printStats();
					}else if("Charm_bitset".equals(choice)){
						double minsup = getParamAsDouble(textFieldParam1);
						AlgoCharm_Bitset_saveToFile algo = new AlgoCharm_Bitset_saveToFile();
						algo.runAlgorithm(inputFile, outputFile, minsup, 100000);
						algo.printStats();
					}
					else if("Relim".equals(choice)){
						double minsup = getParamAsDouble(textFieldParam1);
						
						// Applying the RELIM algorithm
						AlgoRelim algo = new AlgoRelim();
						algo.runAlgorithm(minsup, inputFile, outputFile);
						algo.printStatistics();
					}else if("Eclat_bitset".equals(choice)){
						double minsup = getParamAsDouble(textFieldParam1);
						AlgoEclat_Bitset_saveToFile algo = new AlgoEclat_Bitset_saveToFile();
						algo.runAlgorithm(inputFile, outputFile, minsup);
						algo.printStats();
					}else if("Two-Phase".equals(choice)){
						int minutil = getParamAsInteger(textFieldParam1);
						ca.pfv.spmf.highutility.two_phase_algorithm.Database database = new ca.pfv.spmf.highutility.two_phase_algorithm.Database();
						database.loadFile(inputFile);
						
						// Applying the Two-Phase algorithm
						AlgoTwoPhase twoPhase = new AlgoTwoPhase(database);
						ca.pfv.spmf.highutility.two_phase_algorithm.Itemsets highUtilityItemsets = twoPhase.runAlgorithm(minutil);
						
						highUtilityItemsets.saveResultsToFile(outputFile, database.getTransactions().size());

						twoPhase.printStats();

					}else if("Zart".equals(choice)){
						double minsup = getParamAsDouble(textFieldParam1);
						
						// Load a binary context
						ContextZart context = new ContextZart();
						context.loadFile(inputFile);

						// Apply the Zart algorithm
						AlgoZart zart = new AlgoZart();
						TZTableClosed results = zart.runAlgorithm(context, minsup);
						TFTableFrequent frequents = zart.getTableFrequent();
						zart.printStatistics();
						zart.saveResultsToFile(outputFile);
					}
				}catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null,
						    "Error. Please check the parameters of the algorithm.  The format for numbers is incorrect. \n" +
						    "\n ERROR MESSAGE = " + e.toString(), "Error",
						    JOptionPane.ERROR_MESSAGE);
					return;
				}catch (Exception e) {
					JOptionPane.showMessageDialog(null,
						    "An error while trying to run the algorithm. \n ERROR MESSAGE = " + e.toString(), "Error",
						    JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// IF - the algorithm terminates...
				if(checkboxOpenOutput.isSelected()){
					 Desktop desktop = Desktop.getDesktop();
				  if (desktop.isSupported(Desktop.Action.OPEN)) {
					  try{
					  desktop.open(new File(outputFile));
					  }catch(IOException e){
						  JOptionPane.showMessageDialog(null,
								    "The output file failed to open with the default application. " +
								    "\n This error occurs if there is no default application on your system " +
								    "for opening the output file or the application failed to start. " +
								    "\n\n" +
								    "To fix the problem, consider changing the extension of the output file to .txt." +
								    "\n\n ERROR MESSAGE = " + e.toString(), "Error",
								    JOptionPane.ERROR_MESSAGE);
					  }catch(SecurityException e){
						  JOptionPane.showMessageDialog(null,
								    "A security error occured while trying to open the output file. ERROR MESSAGE = " + e.toString(), "Error",
								    JOptionPane.ERROR_MESSAGE);
					  }catch(Exception e){
						  JOptionPane.showMessageDialog(null,
								    "An error occured while opening the output file. ERROR MESSAGE = " + e.toString(), "Error",
								    JOptionPane.ERROR_MESSAGE);
					  }
				  }
				}  // END IF - CHECBOX
				
			}
		});
		buttonRun.setBounds(216, 343, 119, 23);
		contentPane.add(buttonRun);
		
		JLabel lblChooseAnAlgorithm = new JLabel("Choose an algorithm:");
		lblChooseAnAlgorithm.setBounds(22, 73, 204, 20);
		contentPane.add(lblChooseAnAlgorithm);
		
		JLabel lblNewLabel = new JLabel("New label");
		lblNewLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				openWebPage("http://www.philippe-fournier-viger.com/spmf/");
			}
		});
		lblNewLabel.setIcon(new ImageIcon(MainWindow.class.getResource("spmf.png")));
		lblNewLabel.setBounds(0, 0, 186, 62);
		contentPane.add(lblNewLabel);
		
		textFieldParam1 = new JTextField();
		textFieldParam1.setBounds(197, 164, 157, 20);
		contentPane.add(textFieldParam1);
		textFieldParam1.setColumns(10);
		
		JButton buttonInput = new JButton("...");
		buttonInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try{
					File path;
					String previousPath = PathsManager.getInstance().getInputFilePath();
					if(previousPath == null){
						URL main = MainTestApriori_saveToFile.class.getResource("MainTestApriori_saveToFile.class");
						if (!"file".equalsIgnoreCase(main.getProtocol())){
							path = null;
						}else{
							path = new File(main.getPath());
						}
					}else{
						path = new File(previousPath);
					}
	
					//  CHOOSE INPUT FILE BUTTON
		        	final JFileChooser fc = new JFileChooser(path);
		        	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
					int returnVal = fc.showOpenDialog(MainWindow.this);
	
			        if (returnVal == JFileChooser.APPROVE_OPTION) {
			            File file = fc.getSelectedFile();
			            textFieldInput.setText(file.getName());
			            inputFile = file.getPath();
			        }
			        if(fc.getSelectedFile() != null){
			        	PathsManager.getInstance().setInputFilePath(fc.getSelectedFile().getParent());
			        }
				}catch(Exception e){
			    	JOptionPane.showMessageDialog(null,
						    "An error occured while opening the input file dialog. ERROR MESSAGE = " + e.toString(), "Error",
						    JOptionPane.ERROR_MESSAGE);
			    }

			}
		});
		buttonInput.setBounds(364, 104, 32, 23);
		contentPane.add(buttonInput);
		
		JButton buttonOutput = new JButton("...");
		buttonOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try{
					File path;
					String previousPath = PathsManager.getInstance().getOutputFilePath();
					if(previousPath == null){
						URL main = MainTestApriori_saveToFile.class.getResource("MainTestApriori_saveToFile.class");
						if (!"file".equalsIgnoreCase(main.getProtocol())){
							path = null;
						}else{
							path = new File(main.getPath());
						}
					}else{
						path = new File(previousPath);
					}
					
					// SET OUTPUT BUTTON
		        	final JFileChooser fc;
		        	if(path != null)
		        	{
		        		fc = new JFileChooser(path.getAbsolutePath());
		        	}else{
		        		fc = new JFileChooser();
		        	}
					int returnVal = fc.showSaveDialog(MainWindow.this);
	
			        if (returnVal == JFileChooser.APPROVE_OPTION) {
			            File file = fc.getSelectedFile();
			            textFieldOutput.setText(file.getName());
			            outputFile = file.getPath();
			            if(fc.getSelectedFile() != null){
			            	PathsManager.getInstance().setOutputFilePath(fc.getSelectedFile().getParent());
			            }
			        }
			        
			}catch(Exception e){
		    	JOptionPane.showMessageDialog(null,
					    "An error occured while opening the output file dialog. ERROR MESSAGE = " + e.toString(), "Error",
					    JOptionPane.ERROR_MESSAGE);
		    }
			}
		});
		buttonOutput.setBounds(364, 133, 32, 23);
		contentPane.add(buttonOutput);
		
		labelParam1 = new JLabel("Parameter 1:");
		labelParam1.setBounds(22, 167, 156, 14);
		contentPane.add(labelParam1);
		
		labelParam2 = new JLabel("Parameter 2:");
		labelParam2.setBounds(22, 192, 156, 14);
		contentPane.add(labelParam2);
		
		labelParam3 = new JLabel("Parameter 3:");
		labelParam3.setBounds(22, 217, 156, 14);
		contentPane.add(labelParam3);
		
		labelParam4 = new JLabel("Parameter 4:");
		labelParam4.setBounds(22, 239, 156, 14);
		contentPane.add(labelParam4);
		
		labelParam5 = new JLabel("Parameter 5:");
		labelParam5.setBounds(22, 264, 156, 14);
		contentPane.add(labelParam5);
		
		labelParam6 = new JLabel("Parameter 6:");
		labelParam6.setBounds(22, 289, 156, 14);
		contentPane.add(labelParam6);
		
		textFieldParam2 = new JTextField();
		textFieldParam2.setColumns(10);
		textFieldParam2.setBounds(197, 189, 157, 20);
		contentPane.add(textFieldParam2);
		
		textFieldParam3 = new JTextField();
		textFieldParam3.setColumns(10);
		textFieldParam3.setBounds(197, 214, 157, 20);
		contentPane.add(textFieldParam3);
		
		textFieldParam4 = new JTextField();
		textFieldParam4.setColumns(10);
		textFieldParam4.setBounds(197, 236, 157, 20);
		contentPane.add(textFieldParam4);
		
		textFieldParam5 = new JTextField();
		textFieldParam5.setColumns(10);
		textFieldParam5.setBounds(197, 261, 157, 20);
		contentPane.add(textFieldParam5);
		
		textFieldParam6 = new JTextField();
		textFieldParam6.setColumns(10);
		textFieldParam6.setBounds(197, 286, 157, 20);
		contentPane.add(textFieldParam6);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 377, 549, 148);
		contentPane.add(scrollPane);
		
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		System.setOut(new PrintStream(new TextAreaOutputStream(textArea)));
		
		textFieldInput = new JTextField();
		textFieldInput.setEditable(false);
		textFieldInput.setBounds(197, 105, 157, 20);
		contentPane.add(textFieldInput);
		textFieldInput.setColumns(10);
		
		textFieldOutput = new JTextField();
		textFieldOutput.setEditable(false);
		textFieldOutput.setColumns(10);
		textFieldOutput.setBounds(197, 134, 157, 20);
		contentPane.add(textFieldOutput);
		
		checkboxOpenOutput = new JCheckBox("Open output file when the algorithm terminates");
		checkboxOpenOutput.setSelected(true);
		checkboxOpenOutput.setBounds(22, 310, 358, 23);
		contentPane.add(checkboxOpenOutput);
		
		buttonExample = new JButton("?");
		buttonExample.setEnabled(false);
		buttonExample.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				String choice = (String)comboBox.getSelectedItem();
				if("PrefixSpan".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#examplePrefixSpan");
				}else if("PrefixSpan with strings".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#examplePrefixSpan");
				}else if("SeqDim (PrefixSpan+Apriori)".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#exampleMDSPM1");
				}else if("SeqDim (BIDE+AprioriClose)".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#exampleMDSPM1");
				}else if("SeqDim (BIDE+Charm)".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#exampleMDSPM1");
				}
				else if("SeqDim (PrefixSpan+Apriori)+time".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example14");
				}else if("SeqDim (BIDE+AprioriClose)+time".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example14");
				}else if("SeqDim (BIDE+Charm)+time".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example14");
				}
				else if("SPAM".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#spam");
				}else if("BIDE+".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#exampleBIDE");
				}else if("BIDE+ with strings".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#exampleBIDE");
				}else if("RuleGrowth".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#rulegrowth");
				}else if("TRuleGrowth".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#trulegrowth");
				}else if("TRuleGrowth with strings".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#trulegrowth");
				}else if("CMRules".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#cmrules");
				}else if("CMDeo".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#cmdeo");
				}else if("RuleGen".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#rulegen");
				}else if("TopSeqRules".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#topseqrules");
				}else if("TopKRules".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#topkrules");
				}else if("TNR".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#tnr");
				}
				else if("FPGrowth - itemsets".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#growth");
				}else if("FPGrowth - association rules".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#allassociationrules");
				}else if("FPGrowth - association rules with lift".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#lift");
				}else if("Apriori".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example1");
				}else if("AprioriClose".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example2");
				}else if("AprioriRare".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example17");
				}else if("AprioriInverse".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example18");
				}else if("VME".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#erasable");
				}else if("UApriori".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#uapriori");
				}else if("MSApriori".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#msapriori");
				}else if("CFPGrowth".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#cfpgrowth");
				}
				else if("Apriori_TID_bitset".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#aprioritid");
				}else if("HMine".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#hmine");
				}else if("DCI_Closed".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#dciclosed");
				}else if("Indirect".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#indirect");
				}else if("Charm_bitset".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#e2");
				}else if("Eclat_bitset".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#e1");
				}else if("Relim".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#c23");
				}else if("Zart".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#zart");
				}else if("Two-Phase".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#twophase");
				}else if("KMeans".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example8");
				}else if("HierarchicalClustering".equals(choice)){ 
					openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example1");
				}
			}
		});
		buttonExample.setBounds(513, 72, 46, 23);
		contentPane.add(buttonExample);
		
		JLabel lblChooseInputFile = new JLabel("Choose input file");
		lblChooseInputFile.setBounds(22, 108, 97, 14);
		contentPane.add(lblChooseInputFile);
		
		JLabel lblSetOutputFile = new JLabel("Set output file");
		lblSetOutputFile.setBounds(22, 137, 97, 14);
		contentPane.add(lblSetOutputFile);
		
		lbHelp1 = new JLabel("help1");
		lbHelp1.setBounds(364, 167, 157, 14);
		contentPane.add(lbHelp1);
		
		lbHelp2 = new JLabel("help2");
		lbHelp2.setBounds(364, 192, 157, 14);
		contentPane.add(lbHelp2);
		
		lbHelp3 = new JLabel("help3");
		lbHelp3.setBounds(364, 217, 157, 14);
		contentPane.add(lbHelp3);
		
		lbHelp4 = new JLabel("help4");
		lbHelp4.setBounds(364, 239, 157, 14);
		contentPane.add(lbHelp4);
		
		lbHelp5 = new JLabel("help5");
		lbHelp5.setBounds(364, 264, 157, 14);
		contentPane.add(lbHelp5);
		
		lbHelp6 = new JLabel("help6");
		lbHelp6.setBounds(364, 289, 157, 14);
		contentPane.add(lbHelp6);
		
		hideAllParams();
	}
	
	public  void setParam(JTextField textfield, String name, JLabel label, String helpText){
		label.setText(name);
		textfield.setEnabled(true);
		textfield.setVisible(true);
		label.setVisible(true);
		if(textfield == textFieldParam1){
			lbHelp1.setText(helpText);
			lbHelp1.setVisible(true);
		}else if(textfield == textFieldParam2){
			lbHelp2.setText(helpText);
			lbHelp2.setVisible(true);
		}else if(textfield == textFieldParam3){
			lbHelp3.setText(helpText);
			lbHelp3.setVisible(true);
		}else if(textfield == textFieldParam4){
			lbHelp4.setText(helpText);
			lbHelp4.setVisible(true);
		}else if(textfield == textFieldParam5){
			lbHelp5.setText(helpText);
			lbHelp5.setVisible(true);
		}else if(textfield == textFieldParam6){
			lbHelp6.setText(helpText);
			lbHelp6.setVisible(true);
		}
	}
	
	public static void setHelpTextForParam(JLabel label, String name){
		label.setText(name);
		label.setVisible(true);
	}
	
	public double getParamAsDouble(JTextField textfield){
		if(textfield.getText().contains("%")){
			String value = textfield.getText();
			value = value.substring(0, value.length()-1);
			return Double.parseDouble(value) / 100d;
		}
		return Double.parseDouble(textfield.getText());
	}
	
	public int getParamAsInteger(JTextField textfield){
		return Integer.parseInt(textfield.getText());
	}
	
	public void hideAllParams(){
		labelParam1.setVisible(false);
		labelParam2.setVisible(false);
		labelParam3.setVisible(false);
		labelParam4.setVisible(false);
		labelParam5.setVisible(false);
		labelParam6.setVisible(false);
//		.setVisible(false);
		lbHelp1.setVisible(false);
		lbHelp2.setVisible(false);
		lbHelp3.setVisible(false);
		lbHelp4.setVisible(false);
		lbHelp5.setVisible(false);
		lbHelp6.setVisible(false);
		textFieldParam1.setVisible(false);
		textFieldParam2.setVisible(false);
		textFieldParam3.setVisible(false);
		textFieldParam4.setVisible(false);
		textFieldParam5.setVisible(false);
		textFieldParam6.setVisible(false);
	}

	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestPrefixSpan_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
	
	  class TextAreaOutputStream extends OutputStream {
		   JTextArea textArea;
	
		   public TextAreaOutputStream(JTextArea textArea) {
		    this.textArea = textArea;
		   }
		   public void flush() {
		    textArea.repaint();
		   }
		   public void write(int b) {
		    textArea.append(new String(new byte[] {(byte)b}));
		   }
	  }
	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "SwingAction");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
		}
	}
	
	private void openWebPage(String url) {
		try {
	         java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
	       }
	       catch (java.io.IOException e) {
	           System.out.println(e.getMessage());
	       }
	}
}
