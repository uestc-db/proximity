package ca.pfv.spmf.gui;

import java.util.prefs.Preferences;

/**
 * This is class is used to manage registry keys for
 * storing user preferences.
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
public class PathsManager {   
    public static final String REGKEY_SPMF_INPUT_FILE = "ca.pfv.spmf.gui.input";
    public static final String REGKEY_SPMF_OUTPUT_FILE = "ca.pfv.spmf.gui.output";

    private static PathsManager instance;

    private PathsManager(){

    }
    
    public static PathsManager getInstance(){
        if(instance == null){
            instance = new PathsManager();
        }
        return instance;
    }
    
    
    public String getInputFilePath() {
        //      read back from registry HKCurrentUser
        Preferences p = Preferences.userRoot();
        return p.get(REGKEY_SPMF_INPUT_FILE, null);
    }

    public void setInputFilePath(String filepath) {
        // write into HKCurrentUser
        Preferences p = Preferences.userRoot();
        p.put(REGKEY_SPMF_INPUT_FILE, filepath);
    }
    
    public String getOutputFilePath() {
        //      read back from registry HKCurrentUser
        Preferences p = Preferences.userRoot();
        return p.get(REGKEY_SPMF_OUTPUT_FILE, null);
    }

    public void setOutputFilePath(String filepath) {
        // write into HKCurrentUser
        Preferences p = Preferences.userRoot();
        p.put(REGKEY_SPMF_OUTPUT_FILE, filepath);
    }

    
}
