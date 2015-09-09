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

import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.parser.*;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.util.MemoryMapError;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;
/**
 *
 * @author Praqma
 */
public class MemoryMapRecorder extends Recorder {
    
    private String mapFile;
    private Integer wordSize;
    private boolean showBytesOnGraph;
    
    @Deprecated
    private transient AbstractMemoryMapParser chosenParser;
    
    @Deprecated
    private transient String configurationFile;
    
    @Deprecated
    public final List<MemoryMapGraphConfiguration> graphConfiguration;
    
    public final String scale;
    private List<AbstractMemoryMapParser> chosenParsers;
    private static final Logger logger = Logger.getLogger(MemoryMapRecorder.class.getName());
        
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }
    
    @DataBoundConstructor
    public MemoryMapRecorder(List<AbstractMemoryMapParser> chosenParsers, boolean showBytesOnGraph, String wordSize, final String scale , final List<MemoryMapGraphConfiguration> graphConfiguration) {
        this.chosenParsers = chosenParsers;
        this.showBytesOnGraph = showBytesOnGraph;
        //TODO: This should be chose at parse-time. The 8 that is...
        this.wordSize = StringUtils.isBlank(wordSize) ? 8 : Integer.parseInt(wordSize);   
        this.scale = scale;
        this.graphConfiguration = graphConfiguration;
    }
    
    public Object readResolve()
    {
        if (getChosenParser() != null) {
            logger.log(Level.FINE, "Entering 1.x compatibility block, adding legacy parser to parser list.");

            //Set the config file, this was moved from the recorder to the parser
            if(getConfigurationFile() != null && getChosenParser().getConfigurationFile() == null){
                getChosenParser().setConfigurationFile(configurationFile);
            }
                    
            //The graphs were also moved to the parser
            if(graphConfiguration != null){
                if(getChosenParser().getGraphConfiguration() == null){
                    getChosenParser().setGraphConfiguration(new ArrayList<MemoryMapGraphConfiguration>());
                }
                
                for(MemoryMapGraphConfiguration graphConfig : graphConfiguration) {
                    getChosenParser().getGraphConfiguration().add(graphConfig);
                }
            }

            List<AbstractMemoryMapParser> parsers = new ArrayList<AbstractMemoryMapParser>();
            parsers.add(getChosenParser());
            setChosenParsers(parsers);
        }

        ArrayList<AbstractMemoryMapParser> deprecatedParsers = new ArrayList<>();
        ArrayList<AbstractMemoryMapParser> newParsers = new ArrayList<>();
        for(AbstractMemoryMapParser oldParser  : chosenParsers){
            if(oldParser.getClass().equals(net.praqma.jenkins.memorymap.parser.TexasInstrumentsMemoryMapParser.class)){
                logger.log(Level.FINE, "Entering Texas Instruments deprecation block, swapping old TI parser with the new one.");
                net.praqma.jenkins.memorymap.parser.ti.TexasInstrumentsMemoryMapParser newParser = new net.praqma.jenkins.memorymap.parser.ti.TexasInstrumentsMemoryMapParser();
                newParser.setBytesOnGraph(oldParser.getBytesOnGraph());
                newParser.setConfigurationFile(oldParser.getConfigurationFile());
                newParser.setMapFile(oldParser.getMapFile());
                newParser.setWordSize(oldParser.getWordSize());
                newParser.setParserTitle(oldParser.getParserTitle());
                newParser.setParserUniqueName(oldParser.getParserUniqueName());
                newParser.setGraphConfiguration(oldParser.getGraphConfiguration());
                deprecatedParsers.add(oldParser);
                newParsers.add(newParser);
            }
        }
        chosenParsers.removeAll(deprecatedParsers);
        chosenParsers.addAll(newParsers);

        return this;
    }
    
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        
        boolean failed = false;
        PrintStream out = listener.getLogger();
                
        HashMap<String, MemoryMapConfigMemory> config = null;
        
        String version = Hudson.getInstance().getPlugin( "memory-map" ).getWrapper().getVersion();
        out.println( "Memory Map Plugin version " + version );
        
        try {
            config = build.getWorkspace().act(new MemoryMapConfigFileParserDelegate(getChosenParsers()));
            config = build.getWorkspace().act(new MemoryMapMapParserDelegate(getChosenParsers(), config));            
        } catch(IOException ex) {
            //Catch all known errors (By using a marker interface)
            if (ex instanceof MemoryMapError) {
                out.println(ex.getMessage());
            } else {
                out.println("Unspecified error. Writing trace to log");
                logger.log(Level.SEVERE, "Abnormal plugin execution, trace written to log", ex);
                throw new AbortException( String.format("Unspecified error. Please review error message.%nPlease install the logging plugin to record the standard java logger output stream."
                        + "%nThe plugin is described here: https://wiki.jenkins-ci.org/display/JENKINS/Logging+plugin and requires core 1.483  "));
            }       
            return false;
        }

        out.println("Printing configuration");
        if(config != null) {
            out.println();
            out.println(config.toString());
        }

        MemoryMapBuildAction mmba = new MemoryMapBuildAction(build, config);
        mmba.setRecorder(this);
        mmba.setMemoryMapConfigs(config);                
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

    /**
     * @return the chosenParser
     * @deprecated use chosenParsers instead
     */
    @Deprecated
    public AbstractMemoryMapParser getChosenParser() {
        return chosenParser;
    }

    /**
     * @param chosenParser the chosenParser to set
     * @deprecated use chosenParsers instead
     */
    @Deprecated
    public void setChosenParser(AbstractMemoryMapParser chosenParser) {
        this.chosenParser = chosenParser;
    }

    /**
     * @return the configurationFile
     * @deprecated moved to parser level
     */
    @Deprecated
    public String getConfigurationFile() {
        return configurationFile;
    }

    /**
     * @param configurationFile the configurationFile to set
     * @deprecated moved to parser level
     */
    @Deprecated
    public void setConfigurationFile(String configurationFile) {
        this.configurationFile = configurationFile;
    }

    /**
     * @return the chosenParsers
     */
    public List<AbstractMemoryMapParser> getChosenParsers() {
        return chosenParsers;
    }

    /**
     * @param chosenParsers the chosenParsers to set
     */
    public void setChosenParsers(List<AbstractMemoryMapParser> chosenParsers) {
        this.chosenParsers = chosenParsers;
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
            save();            
            return instance;
        }
        
        public DescriptorImpl() {
            super(MemoryMapRecorder.class);
            load();
        }
        
        private List<String> getScales(){
            List<String> scales = new ArrayList<String>();
            scales.add("default");
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
         public FormValidation doCheckConfigurationFile(@QueryParameter String configurationFile) {
             return FormValidation.validateRequired(configurationFile);
         }
                 
    }
    
    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new MemoryMapProjectAction(project);
    }    
}
