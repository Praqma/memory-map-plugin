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
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import java.io.IOException;
import java.util.List;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.parser.MemoryMapParserDelegate;
import net.praqma.jenkins.memorymap.parser.MemoryMapParserDescriptor;
import net.praqma.jenkins.memorymap.result.MemoryMapParsingResult;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author Praqma
 */
public class MemoryMapRecorder extends Recorder {

    private String mapFile;
    private int ramCapacity;
    private int flashCapacity;
    private AbstractMemoryMapParser chosenParser;
    
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }
    
    @DataBoundConstructor
    public MemoryMapRecorder(AbstractMemoryMapParser chosenParser, int ramCapacity, int flashCapacity) {
        this.chosenParser = chosenParser;
        this.ramCapacity = ramCapacity;
        this.flashCapacity = flashCapacity;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        List<MemoryMapParsingResult> res = build.getWorkspace().act(new MemoryMapParserDelegate(chosenParser, mapFile));

        for(MemoryMapParsingResult result : res) {
            listener.getLogger().println(result);
        }
        /*
         * Create a build action and store the result.
         */
        MemoryMapBuildAction mmba = new MemoryMapBuildAction(build, res);
        
        boolean validFlashCapacity = mmba.validateThreshold(getFlashCapacity(), ".text",".econst");
        int flashCount = mmba.sumOfValues(".text",".econst");
        boolean validRamCapacity = mmba.validateThreshold(getRamCapacity(), ".stack",".cinit",".ebss");
        int ramCount = mmba.sumOfValues( ".stack",".cinit",".ebss");

        listener.getLogger().println("flashCount: "+flashCount);        
        listener.getLogger().println("ramCount: "+ramCount);        
        
        
        if(!validFlashCapacity && !validRamCapacity) {
            listener.getLogger().println("Ram capacity exceeded!");
            build.setResult(Result.FAILURE);            
        } else {
            listener.getLogger().println("Ram capacity is adequate");
        }
        
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
    public int getRamCapacity() {
        return ramCapacity;
    }

    /**
     * @param memoryCapacity the memoryCapacity to set
     */
    public void setRamCapacity(int ramCapacity) {
        this.ramCapacity = ramCapacity;
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

    /**
     * @return the flashCapacity
     */
    public int getFlashCapacity() {
        return flashCapacity;
    }

    /**
     * @param flashCapacity the flashCapacity to set
     */
    public void setFlashCapacity(int flashCapacity) {
        this.flashCapacity = flashCapacity;
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
    
    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new MemoryMapProjectAction(project);
    }
    
}
