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

import net.praqma.jenkins.memorymap.parser.TexasIntrumentsMemoryMapParser;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.praqma.jenkins.memorymap.result.MemoryMapParsingResult;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author Praqma
 */
public class MemoryMapRecorder extends Recorder {

    private String mapFile;
    private int memoryCapacity;
    
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }
    
    @DataBoundConstructor
    public MemoryMapRecorder(String mapFile, String memoryCapacity ) {
        this.mapFile = mapFile;
        this.memoryCapacity = Integer.parseInt(memoryCapacity);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        
        //Example parser
        TexasIntrumentsMemoryMapParser parser = new TexasIntrumentsMemoryMapParser(mapFile);
      
        List<MemoryMapParsingResult> res = build.getWorkspace().act(parser);
        
        for(MemoryMapParsingResult result : res) {
            listener.getLogger().println(result);
        }
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
    }
            
    
}
