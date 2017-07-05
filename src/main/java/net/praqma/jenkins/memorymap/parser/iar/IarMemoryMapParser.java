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
import net.praqma.jenkins.memorymap.parser.MemoryMapConfigFileParserDelegate;
import net.praqma.jenkins.memorymap.parser.MemoryMapMapParserDelegate;
import net.praqma.jenkins.memorymap.parser.MemoryMapParserDescriptor;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem;
import net.praqma.jenkins.memorymap.util.MemoryMapMemorySelectionError;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jenkinsci.Symbol;

/**
 *
 * @author ebmpapst-hs <Kay van der Zander>
 * IAR ELF Linker V6.40.2.53884 for ARM
 */
public class IARMemoryMapParser extends AbstractMemoryMapParser {


    /*
     * Flash
     */
    private static final Pattern TEXT_DOT = Pattern.compile("\\.text\\s+\\S+\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern CONST_DOT = Pattern.compile("\\.rodata\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern NOINIT = Pattern.compile("\\.noinit\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    /*
     * Ram
     */
    private static final Pattern BSS_DOT = Pattern.compile("\\.bss\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern DATA = Pattern.compile("\\.data\\s+inited\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);

    @DataBoundConstructor
    public IARMemoryMapParser(String parserUniqueName, String mapFile, String configurationFile, Integer wordSize, List<MemoryMapGraphConfiguration> graphConfiguration, Boolean bytesOnGraph) {
        super(parserUniqueName, mapFile, configurationFile, wordSize, bytesOnGraph, graphConfiguration, TEXT_DOT, CONST_DOT, NOINIT, BSS_DOT, DATA);
    }

    public IARMemoryMapParser() {
        super();
    }

    @Override
    public MemoryMapConfigMemory parseConfigFile(File f) throws IOException {
        MemoryMapConfigMemory config = new MemoryMapConfigMemory();
        CharSequence sequence = createCharSequenceFromFile(f);
        for (MemoryMapGraphConfiguration graph : getGraphConfiguration()) {
            String[] split = graph.getGraphDataList().split(",");
            for (String s : split) {
                String[] multiSections = s.trim().split("\\+");
                for (String ms : multiSections) {
                    Matcher m = MemoryMapConfigFileParserDelegate.getPatternForMemoryLayout(ms.replace(" ", "")).matcher(sequence);
                    MemoryMapConfigMemoryItem item = null;
                    while (m.find()) {
                        item = new MemoryMapConfigMemoryItem(m.group(1), m.group(3), m.group(5));
                        config.add(item);
                    }

                    if (item == null) {
                        logger.logp(Level.WARNING, "parseConfigFile", AbstractMemoryMapParser.class.getName(), String.format("parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) non existing item: %s", s));
                        throw new MemoryMapMemorySelectionError(String.format("No match found for program memory named %s", s));
                    }
                }

            }
        }
        return config;
    }

    @Override
    public MemoryMapConfigMemory parseMapFile(File f, MemoryMapConfigMemory config) throws IOException {
        CharSequence sequence = createCharSequenceFromFile(f);

        for (MemoryMapConfigMemoryItem item : config) {
            Matcher matcher = MemoryMapMapParserDelegate.getPatternForMemorySection(item.getName()).matcher(sequence);
            boolean found = false;
            while (matcher.find()) {
                item.setUsed(matcher.group(8));
                item.setUnused(matcher.group(10));
                found = true;
            }
            if (!found) {
                logger.logp(Level.WARNING, "parseMapFile", AbstractMemoryMapParser.class.getName(), String.format("parseMapFile(File f, MemoryMapConfigMemory configuration) non existing item: %s", item));
                throw new MemoryMapMemorySelectionError(String.format("Linker command element %s not found in .map file", item));
            }
        }
        return config;
    }


    @Symbol("IARParser")
    @Extension
    public static final class DescriptorImpl extends MemoryMapParserDescriptor<TexasInstrumentsMemoryMapParser> {

        @Override
        public String getDisplayName() {
            return "IAR";
        }

        @Override
        public AbstractMemoryMapParser newInstance(StaplerRequest req, JSONObject formData, AbstractMemoryMapParser instance) throws FormException {
            IARMemoryMapParser parser = (IARMemoryMapParser) instance;
            save();
            return parser;
        }
    }
}