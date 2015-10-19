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
package net.praqma.jenkins.memorymap.parser;

import hudson.model.Descriptor;
import hudson.util.FormValidation;
import java.util.List;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfigurationDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author Praqma
 */
public abstract class MemoryMapParserDescriptor<T extends AbstractMemoryMapParser> extends Descriptor<AbstractMemoryMapParser> {
    public AbstractMemoryMapParser newInstance( StaplerRequest req, JSONObject formData, AbstractMemoryMapParser instance ) throws FormException { 
        return super.newInstance( req, formData );
    }
    
    public List<MemoryMapGraphConfigurationDescriptor<?>> getGraphOptions() {
        return MemoryMapGraphConfiguration.getDescriptors();
    }
    
    /**
     * This field is required 
     * @param mapFile
     * @return 
     */
    public FormValidation doCheckMapFile(@QueryParameter String mapFile) {
        return FormValidation.validateRequired(mapFile);
    }
    
    public FormValidation doCheckParserUniqueName(@QueryParameter String parserUniqueName) {
        return FormValidation.validateRequired(parserUniqueName);
    }
    
    public FormValidation doCheckConfigurationFile(@QueryParameter String configurationFile) {
        return FormValidation.validateRequired(configurationFile);
    }
    
}

    