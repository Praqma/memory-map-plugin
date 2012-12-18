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
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.parser.MemoryMapConfigFileParserDelegate;
import net.praqma.jenkins.memorymap.parser.MemoryMapMapParserDelegate;
import net.praqma.jenkins.memorymap.parser.MemoryMapParserDescriptor;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.result.MemoryMapGroup;
import net.praqma.jenkins.memorymap.result.MemoryMapParsingResult;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author Praqma
 */
public class MemoryMapRecorder extends Recorder {

    private String mapFile;
    private int ramCapacity;
    private int flashCapacity;
    private String configurationFile;
    
    private AbstractMemoryMapParser chosenParser;
    private List<MemoryMapGroup> groups;
    
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }
    
    @DataBoundConstructor
    public MemoryMapRecorder(AbstractMemoryMapParser chosenParser, List<MemoryMapGroup> groups, int ramCapacity, int flashCapacity, String configurationFile) {
        this.chosenParser = chosenParser;
        this.ramCapacity = ramCapacity;
        this.flashCapacity = flashCapacity;
        this.groups = groups;
        this.configurationFile = configurationFile;              
    }
    
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        
        boolean failed = false;
        PrintStream out = listener.getLogger();
                
        List<MemoryMapParsingResult> res = new LinkedList<MemoryMapParsingResult>();
        MemoryMapConfigMemory config = null;
        
        String version = Hudson.getInstance().getPlugin( "memory-map" ).getWrapper().getVersion();
		out.println( "Memory Map Plugin version " + version );
        
        try { 
            chosenParser.setConfigurationFile(configurationFile);
            res = build.getWorkspace().act(new MemoryMapMapParserDelegate(chosenParser));
            config = build.getWorkspace().act(new MemoryMapConfigFileParserDelegate(chosenParser));
        } catch(IOException ex) {
            out.println(ex.getCause().getMessage());
            failed = true;
        }
        //TODO:Remove before release
        out.println("Printing configuration");
        out.println(chosenParser.getConfigurationFile());
        if(config != null) {
            out.println(config.toString());
        }
        //

        for(MemoryMapParsingResult result : res) {
            out.println(result);
        }        
        /*
         * Create a build action and store the result.
         */
        MemoryMapBuildAction mmba = new MemoryMapBuildAction(build, res);
        mmba.setRecorder(this);
        
        if(failed) {
            build.setResult(Result.FAILURE);
            return false;
        }
        
        
        
        boolean validFlashCapacity = mmba.validateThreshold(getFlashCapacity(), MemoryMapGroup.defaultFlashGroup());
        int flashCount = mmba.sumOfValues(MemoryMapGroup.defaultFlashGroup());
        
        boolean validRamCapacity = mmba.validateThreshold(getRamCapacity(), MemoryMapGroup.defaultRamGroup());
        int ramCount = mmba.sumOfValues(MemoryMapGroup.defaultRamGroup());

        out.println("Recorded flash memory usage: "+flashCount);        
        out.println("Recorded ram usage: "+ramCount);        
        
        out.println(String.format("Maximum flash setting: %s", getFlashCapacity()));
        out.println(String.format("Maximum ram setting: %s", getRamCapacity()));
        
        if(!validFlashCapacity) {
            out.println("Flash capacity exceeded.");
            build.setResult(Result.FAILURE);            
        }
        
        if(!validRamCapacity) {
            out.println("Ram capacity exceeded.");
            build.setResult(Result.FAILURE);            
        }
        
        if(validRamCapacity && validFlashCapacity) {
            out.println("Ram and flash usage within capacity");
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

    /**
     * @return the groups
     */
    public List<MemoryMapGroup> getGroups() {
        if(groups == null) {
            groups = new ArrayList<MemoryMapGroup>();
            groups.add(MemoryMapGroup.defaultFlashGroup());
            groups.add(MemoryMapGroup.defaultRamGroup());
        }
        return groups;
    }

    /**
     * @param groups the groups to set
     */
    public void setGroups(List<MemoryMapGroup> groups) {
        this.groups = groups;
    }

    /**
     * @return the configurationFile
     */
    public String getConfigurationFile() {
        return configurationFile;
    }

    /**
     * @param configurationFile the configurationFile to set
     */
    public void setConfigurationFile(String configurationFile) {
        this.configurationFile = configurationFile;
    }
    
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        
        public FormValidation doCheckFlashCapacity(@QueryParameter String value) {
            try 
            {
                int val = Integer.parseInt(value);        
                if(val < 0) {
                    return FormValidation.error("Number must be 0 or greater");
                }
            } catch (NumberFormatException nfe) {
                return FormValidation.error("Not a valid integer value");
            }
            return FormValidation.ok();          
        }

        public FormValidation doCheckRamCapacity(@QueryParameter String value) {
            try 
            {
                int val = Integer.parseInt(value);
                if(val < 0) {
                    return FormValidation.error("Number must be 0 or greater");
                }
            } catch (NumberFormatException nfe) {
                return FormValidation.error("Not a valid integer value");
            }
            return FormValidation.ok();           
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> type) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Memory Map Publisher";
        }
        
        public List<MemoryMapParserDescriptor<?>> getParsers() {
            return AbstractMemoryMapParser.getDescriptors();
        }
        
        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            MemoryMapRecorder instance = req.bindJSON(MemoryMapRecorder.class, formData);
            List<MemoryMapGroup> groups = req.bindParametersToList(MemoryMapGroup.class, "group.");
            instance.setGroups(groups);
            save();
            return instance;
        }
        
        public DescriptorImpl() {
            super(MemoryMapRecorder.class);
            load();
        }
    }
    
    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new MemoryMapProjectAction(project);
    }    
}
