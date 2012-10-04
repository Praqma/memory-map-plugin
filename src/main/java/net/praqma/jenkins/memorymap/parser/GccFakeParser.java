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
import java.io.Serializable;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.praqma.jenkins.memorymap.result.MemoryMapParsingResult;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author Praqma
 */
public class GccFakeParser extends AbstractMemoryMapParser implements Serializable {
    
   //private static final Pattern PATTERN_FLASH = Pattern.compile("(FLASH\\s+\\S+\\s+)(\\S)\\s");
    //private static final Pattern PATTERN_FLASH = Pattern.compile("FLASH\\s+\\S+\\s+(\\S+)");
    private static final Pattern TEXT_DOT = Pattern.compile("^\\.text\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern CONST_DOT = Pattern.compile("^\\.econst\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    
    private static final Pattern CINIT_DOT = Pattern.compile("^\\.cinit\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern STACK_DOT = Pattern.compile("^\\.stack\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern BSS_DOT = Pattern.compile("^\\.ebss\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    
    @DataBoundConstructor
    public GccFakeParser(String mapFile) {
        super(mapFile, TEXT_DOT, CONST_DOT, CINIT_DOT, STACK_DOT, BSS_DOT);
    }
    
    public GccFakeParser() { 
        super();
    }

    @Override
    public LinkedList<MemoryMapParsingResult> parse(File f) throws IOException {
        LinkedList<MemoryMapParsingResult> res = new LinkedList<MemoryMapParsingResult>();
        for(Pattern p : patterns) {
            Matcher m = p.matcher(createCharSequenceFromFile(f));
            while(m.find()) {
                String parsedValue = m.group(1);
                MemoryMapParsingResult pres = new MemoryMapParsingResult();                
                if(p.toString().equals(TEXT_DOT.toString())) {
                    pres.setName(".text");
                } else if (p.toString().equals(CONST_DOT.toString())) {
                    pres.setName(".econst");
                } else if (p.toString().equals(CINIT_DOT.toString())) {
                    pres.setName(".cinit");
                } else if (p.toString().equals(STACK_DOT.toString())) {
                    pres.setName(".stack");
                } else if (p.toString().equals(BSS_DOT.toString())) {
                    pres.setName(".ebss");
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
            return "Gcc Fake Parser";
        }

        @Override
        public AbstractMemoryMapParser newInstance(StaplerRequest req, JSONObject formData, AbstractMemoryMapParser instance) throws FormException {
            GccFakeParser parser = (GccFakeParser)instance;
            save();
            return parser;
        } 
    }    
}

