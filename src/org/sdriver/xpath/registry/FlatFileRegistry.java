/*
Copyright 2009,
Vassilios Karakoidas (bkarak@aueb.gr)
Dimitrios Mitropoulos (dimitro@aueb.gr)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY <copyright holder> ''AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <copyright holder> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.sdriver.xpath.registry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.Set;

/**
 * 
 * @author Vassilios Karakoidas (bkarak@aueb.gr)
 *
 */
public class FlatFileRegistry implements Registry {
    class RegistryFile {
        private final static String FILENAME = "ids.registry";
        
        public RegistryFile() {
            // empty
        }
        
        public String[] load() {
            try {
                List<String> r = new ArrayList<String>();
                BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(FILENAME)));
                
                while(bf.ready()) {
                    r.add(bf.readLine().trim());
                }
                
                bf.close();
                
                return r.toArray(new String[] {});
            } catch (FileNotFoundException e) {
                System.err.println("ERROR: Could not open log file (" + FILENAME + ")");
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("ERROR: Could not read log file (" + FILENAME + ")");
                e.printStackTrace();
            }
            
            return new String[] {};
        }
        
        public void save(String[] s) {
            try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILENAME)));
                
                for ( String str : s ) {
                    bw.write(str);
                    bw.newLine();
                }
                
                bw.close();
            } catch (FileNotFoundException e) {
                System.err.println("ERROR: Could not open log file (" + FILENAME + ")");
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("ERROR: Could not write log file (" + FILENAME + ")");
                e.printStackTrace();
            }
        }
    }
    
    private Set<String> ids;
    
    public FlatFileRegistry() {
        ids = new TreeSet<String>();
        for ( String i : (new RegistryFile()).load() ) {
            ids.add(i);
        }
    }
    
    public void addID(String id) {
        ids.add(id);
        (new RegistryFile()).save(ids.toArray(new String[] {}));
    }
    
    public boolean exists(String id) {
        return ids.contains(id);
    }
    
    public String[] getIDs() {
        return ids.toArray(new String[] {});
    }
}