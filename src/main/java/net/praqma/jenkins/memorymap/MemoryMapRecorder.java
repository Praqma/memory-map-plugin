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
import hudson.FilePath;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import net.praqma.jenkins.memorymap.parser.*;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.util.MemoryMapError;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;
/**
 *
 * @author Praqma
 */
public class MemoryMapRecorder extends Recorder implements SimpleBuildStep {

    private int wordSize = 8;
    private boolean showBytesOnGraph = false;

    private String scale = "default";
    private List<AbstractMemoryMapParser> chosenParsers;
    private static final Logger logger = Logger.getLogger(MemoryMapRecorder.class.getName());

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    public MemoryMapRecorder(){
    }

    @DataBoundConstructor
    public MemoryMapRecorder(List<AbstractMemoryMapParser> chosenParsers) {
        this.chosenParsers = chosenParsers;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {
        
        PrintStream out = listener.getLogger();

        HashMap<String, MemoryMapConfigMemory> config;

        String version = Jenkins.getActiveInstance().getPlugin( "memory-map" ).getWrapper().getVersion();
        out.println( "Memory Map Plugin version " + version );

        try {
            config = workspace.act(new MemoryMapConfigFileParserDelegate(getChosenParsers()));
            config = workspace.act(new MemoryMapMapParserDelegate(getChosenParsers(), config));
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
            return;
        }

        out.println("Printing configuration");
        if(config != null) {
            out.println();
            out.println(config.toString());
        }

        MemoryMapBuildAction buildAction = new MemoryMapBuildAction(build, config);
        buildAction.setRecorder(this);
        buildAction.setMemoryMapConfigs(config);
        buildAction.setChosenParsers(getChosenParsers());
        build.addAction(buildAction);
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
    @DataBoundSetter
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
    @DataBoundSetter
    public void setWordSize(int wordSize) {
        this.wordSize = wordSize;
    }
    
    /**
     * @return the scale
     */
    public String getScale() {
        return scale;
    }
    
     /**
     * @param scale the scale to set
     */
    @DataBoundSetter
    public void setScale(String scale) {
        this.scale = scale;
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

    @Symbol("MemoryMapRecorder")
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
            List<String> scales = new ArrayList<>();
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
}
