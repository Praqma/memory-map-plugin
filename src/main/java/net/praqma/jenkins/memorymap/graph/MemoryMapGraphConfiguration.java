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
package net.praqma.jenkins.memorymap.graph;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Praqma
 */
public class MemoryMapGraphConfiguration implements Describable<MemoryMapGraphConfiguration>, ExtensionPoint, Serializable {

    private String graphCaption = "Specify graph caption";
    private String graphDataList = "Specify graph data sets for graph";
    
    @DataBoundConstructor
    public MemoryMapGraphConfiguration(String graphDataList, String graphCaption) {
        this.graphDataList = graphDataList;
        this.graphCaption = graphCaption;
    }
    
    public MemoryMapGraphConfiguration() { }
    
    public String[] itemizeGraphDataList() {
        return graphDataList.split(",");
    }
    /**
     * @return the graphCaption
     */
    public String getGraphCaption() {
        return graphCaption;
    }

    /**
     * @param graphCaption the graphCaption to set
     */
    public void setGraphCaption(String graphCaption) {
        this.graphCaption = graphCaption;
    }

    /**
     * @return the graphDataList
     */
    public String getGraphDataList() {
        return graphDataList;
    }

    /**
     * @param graphDataList the graphDataList to set
     */
    public void setGraphDataList(String graphDataList) {
        this.graphDataList = graphDataList;
    }

    @Override
    public Descriptor<MemoryMapGraphConfiguration> getDescriptor() {
        return (Descriptor<MemoryMapGraphConfiguration>) Jenkins.getActiveInstance().getDescriptorOrDie(getClass());
    }

    @Extension
    public static final class DescriptorImpl extends MemoryMapGraphConfigurationDescriptor<MemoryMapGraphConfiguration> {

        @Override
        public String getDisplayName() {
            return "MemoryMap bar graph";
        }

        @Override
        public MemoryMapGraphConfiguration newInstance(StaplerRequest req, JSONObject formData, MemoryMapGraphConfiguration instance) throws FormException {
            save();
            return instance;
        }
    }

    public static DescriptorExtensionList<MemoryMapGraphConfiguration, MemoryMapGraphConfigurationDescriptor<MemoryMapGraphConfiguration>> all() {
        return Jenkins.getActiveInstance().<MemoryMapGraphConfiguration, MemoryMapGraphConfigurationDescriptor<MemoryMapGraphConfiguration>>getDescriptorList(MemoryMapGraphConfiguration.class);
    }

    public static List<MemoryMapGraphConfigurationDescriptor<?>> getDescriptors() {
        return all().stream().collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", getGraphCaption(), getGraphDataList());
    }
    
    
}
