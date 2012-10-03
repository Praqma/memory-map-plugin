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

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.parser.MemoryMapParserDelegate;
import net.praqma.jenkins.memorymap.parser.MemoryMapParserDescriptor;
import net.praqma.jenkins.memorymap.result.MemoryMapParsingResult;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author Praqma
 */
public class MemoryMapRecorder extends Recorder {

    private String mapFile;
    private int memoryCapacity;
    private AbstractMemoryMapParser chosenParser;
    
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }
    
    @DataBoundConstructor
    public MemoryMapRecorder(AbstractMemoryMapParser chosenParser) {
        this.chosenParser = chosenParser;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        
        
        List<MemoryMapParsingResult> res = build.getWorkspace().act(new MemoryMapParserDelegate(chosenParser));
        
        
        for(MemoryMapParsingResult result : res) {
            listener.getLogger().println(result);
        }
        
        
        
        
        
        /*
         * Create a build action and store the result.
         */
        MemoryMapBuildAction mmba = new MemoryMapBuildAction(res);
        build.getActions().add(mmba);
        
        return true;        
    } 

    /**
     * @return the mapFile
     */
    public String getMapFile() {
        return mapFile;
    }

    /**
     * @param mapFile the mapFile to set
     */
    public void setMapFile(String mapFile) {
        this.mapFile = mapFile;
    }

    /**
     * @return the memoryCapacity
     */
    public int getMemoryCapacity() {
        return memoryCapacity;
    }

    /**
     * @param memoryCapacity the memoryCapacity to set
     */
    public void setMemoryCapacity(int memoryCapacity) {
        this.memoryCapacity = memoryCapacity;
    }

    /**
     * @return the chosenParser
     */
    public AbstractMemoryMapParser getChosenParser() {
        return chosenParser;
    }

    /**
     * @param chosenParser the chosenParser to set
     */
    public void setChosenParser(AbstractMemoryMapParser chosenParser) {
        this.chosenParser = chosenParser;
    }
    
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> type) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Memory Map Parser";
        }
        
        public List<MemoryMapParserDescriptor<?>> getParsers() {
			return AbstractMemoryMapParser.getDescriptors();
		}
    }
    
    /**
     * Small class to abstract the task of using the file callable away from the parser 
     */

    
}
