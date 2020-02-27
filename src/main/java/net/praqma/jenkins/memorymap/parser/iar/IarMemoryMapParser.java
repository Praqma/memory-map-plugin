/*
 * The MIT License
 *
 * Copyright 2017 ebmpapst-hs.
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
package net.praqma.jenkins.memorymap.parser.iar;

import hudson.Extension;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.parser.MemoryMapParserDescriptor;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem;
import net.praqma.jenkins.memorymap.util.MemoryMapMemorySelectionError;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jenkinsci.Symbol;

/**
 *
 * @author Kay van der Zander
 * IAR map files used from ARM, STM8 and AVR.
 */
public class IarMemoryMapParser extends AbstractMemoryMapParser {
    IarParserLogic parserLogic;

    @DataBoundConstructor
    public IarMemoryMapParser(String parserUniqueName, String mapFile, String configurationFile, Integer wordSize, List<MemoryMapGraphConfiguration> graphConfiguration, Boolean bytesOnGraph) {
        super(
            parserUniqueName,
            mapFile,
            configurationFile,
            wordSize,
            bytesOnGraph,
            graphConfiguration
        );
        parserLogic = new IarParserLogic();
    }

    public IarMemoryMapParser() {
        super();
        parserLogic = new IarParserLogic();
    }

    @Override
    public MemoryMapConfigMemory parseConfigFile(File f) throws IOException {
        return new MemoryMapConfigMemory();
    }

    @Override
    public MemoryMapConfigMemory parseMapFile(File f, MemoryMapConfigMemory config) throws IOException {
        CharSequence text = createCharSequenceFromFile(f);
        for (MemoryMapGraphConfiguration graph : getGraphConfiguration()) {
            parserLogic.parseGraphData(text, graph, config);
        }
        return config;
    }

    @Override
    public int getDefaultWordSize() {
        return 16;
    }

    @Symbol("IARParser")
    @Extension
    public static final class DescriptorImpl extends MemoryMapParserDescriptor<IarMemoryMapParser> {

        @Override
        public String getDisplayName() {
            return "IAR";
        }

        @Override
        public AbstractMemoryMapParser newInstance(StaplerRequest req, JSONObject formData, AbstractMemoryMapParser instance) throws FormException {
            IarMemoryMapParser parser = (IarMemoryMapParser) instance;
            save();
            return parser;
        }
    }
}
