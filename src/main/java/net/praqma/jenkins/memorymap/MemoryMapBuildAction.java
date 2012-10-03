/*
 * The MIT License
 *
 * Copyright 2012 Praqma.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.praqma.jenkins.memorymap;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import java.util.List;
import net.praqma.jenkins.memorymap.result.MemoryMapParsingResult;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 *
 * @author Praqma
 */
public class MemoryMapBuildAction implements Action {

    public List<MemoryMapParsingResult> results;
    private AbstractBuild<?,?> build;
    
    public MemoryMapBuildAction(AbstractBuild<?,?> build,List<MemoryMapParsingResult> results) {
        this.results = results;
        this.build = build;
    }
    
    
    
    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Memory map";
    }

    @Override
    public String getUrlName() {
        return null;
    }
    
    /**
     * Returns an indication wheather as to the requirements are met. You do one check per set of values you wish to compare. 
     * 
     * @param threshold
     * @param valuenames
     * @return 
     */
    public boolean validateThreshold(int threshold, String... valuenames) {
        return sumOfValues(valuenames) <= threshold;
    }
    
    public int sumOfValues(String... valuenames) { 
        int sum = 0;
        for(MemoryMapParsingResult res : results) {
            for(String s : valuenames) {
                if(res.getName().equals(s)) {
                    sum+=res.getValue();
                }
            }
        }
        return sum;
    }
    
        /**
     * Fetches the previous MemoryMap build. Takes all succesful, but failed builds. 
     * 
     * Goes to the end of list.
     */ 
    public MemoryMapBuildAction getPreviousAction(AbstractBuild<?,?> base) {
        MemoryMapBuildAction action = null;
        AbstractBuild<?,?> start = base;
        while(true) {
            start = start.getPreviousCompletedBuild();
            if(start == null) {
                return null;
            }
            action = start.getAction(MemoryMapBuildAction.class);            
            if(action != null) {
                return action;
            }
        }
    }
    
    public MemoryMapBuildAction getPreviousAction() {
        MemoryMapBuildAction action = null;
        AbstractBuild<?,?> start = build;
        while(true) {
            start = start.getPreviousCompletedBuild();
            if(start == null) {
                return null;
            }
            action = start.getAction(MemoryMapBuildAction.class);            
            if(action != null) {
                return action;
            }
        }
    }
    
    
    public void doDrawMemoryMapUsageGraph(StaplerRequest req, StaplerResponse rsp) {
        
    }
    
}
