/*
 * The MIT License
 *
 * Copyright 2013 mads.
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
package net.praqma.jenkins.unit;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.parser.gcc.GccMemoryMapParser;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import static org.junit.Assert.*;
import org.junit.Test;
/**
 *
 * @author mads
 */
public class MemoryMapGccParserTest {
    
    @Test
    public void testParsingOfMemorySegmentInLinkerCommandFile() throws IOException {
        GccMemoryMapParser parser = new GccMemoryMapParser();
        MemoryMapGraphConfiguration conf = new MemoryMapGraphConfiguration("application", "Application test", Boolean.TRUE);
        List<MemoryMapGraphConfiguration> singletonList = Collections.singletonList(conf);
        
        String fileNameLinker = MemoryMapGccParserTest.class.getResource("prom.ld").getFile();
        String fileNameMap = MemoryMapGccParserTest.class.getResource("prom.map").getFile();
        
        File f = new File(fileNameLinker);
        MemoryMapConfigMemory mem = parser.parseConfigFile(f);
        
        File f2 = new File(fileNameMap);
        mem = parser.parseMapFile(f2, mem);
    }
}
