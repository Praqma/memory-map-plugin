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

import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.util.FileFoundable;

/**
 * Class to wrap the FileCallable method. Serves as a proxy to the parser method. 
 * @author Praqma
 */
public class MemoryMapMapParserDelegate extends FileFoundable<HashMap<String,MemoryMapConfigMemory>> 
{
    private static final Logger log = Logger.getLogger(MemoryMapMapParserDelegate.class.getName());
    private List<AbstractMemoryMapParser> parsers;
    private HashMap<String, MemoryMapConfigMemory> config;
    private static HashMap<String,Pattern> patternRegistry;
    
    //Empty constructor. For serialization purposes.
    public MemoryMapMapParserDelegate() { }

    public MemoryMapMapParserDelegate(List<AbstractMemoryMapParser> parsers) {
        this.parsers = parsers;
    }
    
    public MemoryMapMapParserDelegate(List<AbstractMemoryMapParser> parsers, HashMap<String,MemoryMapConfigMemory> config) {
        this.parsers = parsers;
        this.config = config;        
    }

    @Override
    public HashMap<String, MemoryMapConfigMemory> invoke(File file, VirtualChannel vc) throws IOException, InterruptedException {        
        for(AbstractMemoryMapParser parser : parsers) {
            MemoryMapConfigMemory mem = parser.parseMapFile(findFile(file, parser.mapFile), config.get(parser.getParserUniqueName()));
        }
        return config;
    }

    /**
     * @return the parser
     */
    public List<AbstractMemoryMapParser> getParsers() {
        return parsers;
    }

    /**
     * @param parser the parser to set
     */
    public void setParsers(List<AbstractMemoryMapParser> parsers) {
        this.parsers = parsers;
    }
    
    public static Pattern getPatternForMemorySection(String sectionName) {
        if(patternRegistry == null) {
            patternRegistry = new HashMap<String, Pattern>();
        }
        if(patternRegistry.containsKey(sectionName)) {
            return patternRegistry.get(sectionName);
        } else {
            String regex = String.format("^(\\s+)(\\b%s)(\\s+)(\\S+)(\\s+)(\\S+)(\\s+)(\\S+)(\\s+)(\\S+)(\\s+)(\\S+)", sectionName);
            Pattern memsection = Pattern.compile(regex,Pattern.MULTILINE);
            patternRegistry.put(sectionName, memsection);
            return memsection;
        }
      
    }
}
