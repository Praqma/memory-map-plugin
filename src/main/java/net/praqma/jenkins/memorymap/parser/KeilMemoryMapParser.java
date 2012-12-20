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

import hudson.Extension;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.result.MemoryMapParsingResult;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author Praqma
 */
public class KeilMemoryMapParser extends AbstractMemoryMapParser {
    
    /*
     * Flash
     */
    private static final Pattern TEXT_DOT = Pattern.compile("^\\.text\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern CONST_DOT = Pattern.compile("^\\.const\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern ECONST_DOT = Pattern.compile("^\\.econst\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern PINIT = Pattern.compile("^\\.pinit\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern SWITCH = Pattern.compile("^\\.switch\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    
    private static final Pattern CINIT_DOT = Pattern.compile("^\\.cinit\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern STACK_DOT = Pattern.compile("^\\.stack\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern BSS_DOT = Pattern.compile("^\\.bss\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern EBSS_DOT = Pattern.compile("^\\.ebss\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern SYSMEM = Pattern.compile("^\\.sysmem\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern ESYSMEM = Pattern.compile("^\\.esysmem\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern CIO = Pattern.compile("^\\.cio\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern DATA = Pattern.compile("^\\.data\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    
    
    @DataBoundConstructor
    public KeilMemoryMapParser(String mapFile, String configurationFile, Integer wordSize, Boolean bytesOnGraph) {
        super(mapFile, configurationFile, wordSize,bytesOnGraph, TEXT_DOT, CONST_DOT, ECONST_DOT, PINIT, SWITCH, CINIT_DOT, STACK_DOT, BSS_DOT, EBSS_DOT, SYSMEM, ESYSMEM, CIO, DATA);
    }
    
    public KeilMemoryMapParser() {
        super();
    }
    
    @Override
    public LinkedList<MemoryMapParsingResult> parseMapFile(File f) throws IOException {
        LinkedList<MemoryMapParsingResult> res = new LinkedList<MemoryMapParsingResult>();
        for(Pattern p : patterns) {
            Matcher m = p.matcher(createCharSequenceFromFile(f));
            while(m.find()) {
                String parsedValue = m.group(1);
                MemoryMapParsingResult pres = new MemoryMapParsingResult();                
                if(p.toString().equals(TEXT_DOT.toString())) {
                    pres.setName(".text");
                } else if (p.toString().equals(ECONST_DOT.toString())) {
                    pres.setName(".econst");
                } else if (p.toString().equals(CINIT_DOT.toString())) {
                    pres.setName(".cinit");
                } else if (p.toString().equals(STACK_DOT.toString())) {
                    pres.setName(".stack");
                } else if (p.toString().equals(EBSS_DOT.toString())) {
                    pres.setName(".ebss");
                } else if (p.toString().equals(BSS_DOT.toString())) {
                    pres.setName(".bss");
                } else if (p.toString().equals(CONST_DOT.toString())) {
                    pres.setName(".const");
                } else if (p.toString().equals(PINIT.toString())) {
                    pres.setName(".pinit");
                } else if (p.toString().equals(SWITCH.toString())) {
                    pres.setName(".switch");
                } else if (p.toString().equals(SYSMEM.toString())) {
                    pres.setName(".sysmem");
                } else if (p.toString().equals(ESYSMEM.toString())) {
                    pres.setName(".esysmem");
                } else if(p.toString().equals(CIO.toString())) {
                    pres.setName(".cio");
                } else if(p.toString().equals(DATA.toString())) {
                    pres.setName(".data");
                } else {
                    throw new IOException("Illegal pattern passed to method", new IllegalArgumentException(p.toString()));
                }
                pres.setRawvalue(parsedValue);
                pres.setValue(Integer.parseInt(parsedValue, 16));
                res.add(pres);
            }
        }        
        return res;
    }
    
    @Extension
    public static final class DescriptorImpl extends MemoryMapParserDescriptor<TexasInstrumentsMemoryMapParser> {

        @Override
        public String getDisplayName() {
            return "Keil";
        }

        @Override
        public AbstractMemoryMapParser newInstance(StaplerRequest req, JSONObject formData, AbstractMemoryMapParser instance) throws FormException {
            KeilMemoryMapParser parser = (KeilMemoryMapParser)instance;
            save();
            return parser;
        } 
    }    
}
