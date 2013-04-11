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

import com.sun.jna.StringArray;
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
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.io.PrintStream;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.parser.MemoryMapConfigFileParserDelegate;
import net.praqma.jenkins.memorymap.parser.MemoryMapMapParserDelegate;
import net.praqma.jenkins.memorymap.parser.MemoryMapParserDescriptor;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.result.MemoryMapGroup;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author Praqma
 */
public class MemoryMapRecorder extends Recorder {

    private String mapFile;
    private Integer wordSize;
    private String configurationFile;
    private boolean showBytesOnGraph;
    public final String scale;
    private AbstractMemoryMapParser chosenParser;
    private List<MemoryMapGraphConfiguration> graphConfiguration;
        
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }
    
    @DataBoundConstructor
    public MemoryMapRecorder(AbstractMemoryMapParser chosenParser, String configurationFile, boolean showBytesOnGraph, String wordSize, final String scale ) {
        this.chosenParser = chosenParser;
        this.configurationFile = configurationFile;        
        this.showBytesOnGraph = showBytesOnGraph;
        this.wordSize = StringUtils.isBlank(wordSize) ? 16 : Integer.parseInt(wordSize);   
        this.scale = scale;
    }
    
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        
        boolean failed = false;
        PrintStream out = listener.getLogger();
                
        MemoryMapConfigMemory config = null;
        
        String version = Hudson.getInstance().getPlugin( "memory-map" ).getWrapper().getVersion();
		out.println( "Memory Map Plugin version " + version );
        
        try { 
            chosenParser.setConfigurationFile(configurationFile);
            config = build.getWorkspace().act(new MemoryMapConfigFileParserDelegate(graphConfiguration, chosenParser));
            config = build.getWorkspace().act(new MemoryMapMapParserDelegate(chosenParser, config));
        } catch(IOException ex) {
            ex.printStackTrace(out);
            failed = true;
        }

        out.println("Printing configuration");
        if(config != null) {
            out.println("== Configuration start ==");
            out.println();
            out.println(config.toString());
            out.println("== Configuration end ==");
        }

        MemoryMapBuildAction mmba = new MemoryMapBuildAction(build, config);
        mmba.setRecorder(this);
        mmba.setMemoryMapConfig(config);        
        
        if(failed) {
            build.setResult(Result.FAILURE);
            return false;
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

    /**
     * @return the graphConfiguration
     */    
    public List<MemoryMapGraphConfiguration> getGraphConfiguration() {
        return graphConfiguration;
    }

    /**
     * @param graphConfiguration the graphConfiguration to set
     */
    public void setGraphConfiguration(List<MemoryMapGraphConfiguration> graphConfiguration) {
        this.graphConfiguration = graphConfiguration;
    }

    /**
     * @return the showBytesOnGraph
     */
    public Boolean getShowBytesOnGraph() {
        return showBytesOnGraph;
    }

    /**
     * @param showBytesOnGraph the showBytesOnGraph to set
     */
    public void setShowBytesOnGraph(Boolean showBytesOnGraph) {
        this.showBytesOnGraph = showBytesOnGraph;
    }

    /**
     * @return the wordSize
     */
    public Integer getWordSize() {
        return wordSize;
    }

    /**
     * @param wordSize the wordSize to set
     */
    public void setWordSize(Integer wordSize) {
        this.wordSize = wordSize;
    }
    
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

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
            
            List<MemoryMapGraphConfiguration> graphConfiguration = req.bindParametersToList(MemoryMapGraphConfiguration.class, "graph.config.");                        
            if(graphConfiguration != null) {
                instance.setGraphConfiguration(graphConfiguration);
            }
            save();            
            return instance;
        }
        
        public DescriptorImpl() {
            super(MemoryMapRecorder.class);
            load();
        }
        
        
        
        private List<String> getScales(){
            List<String> scales = new ArrayList<String>();
            scales.add("kilo");
            scales.add("Mega");
            scales.add("Giga");
            return scales;
        }
        
         public ListBoxModel doFillScaleItems() {
            ListBoxModel items = new ListBoxModel();
            for (String scale : getScales()) {
                items.add(scale);
            }
            return items;
        }
        
    }
    
    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new MemoryMapProjectAction(project);
    }    
}
