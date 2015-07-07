/*
 * The MIT License
 *
 * Copyright 2013 Martin.
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
package net.praqma.jenkins.ti;

import java.io.File;
import java.io.IOException;
import net.praqma.jenkins.memorymap.parser.TexasInstrumentsMemoryMapParser;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Martin
 */
public class TexasInstrumentsMemoryMapParserTest {

    public TexasInstrumentsMemoryMapParserTest() {
    }

    @Test
    public void testParseConfigFile() throws IOException {
        TexasInstrumentsMemoryMapParser parser = new TexasInstrumentsMemoryMapParser();
        String file = TexasInstrumentsMemoryMapParserTest.class.getResource("28069_RAM_lnk.cmd").getFile();

        File f = new File(file);
        parser.parseConfigFile(f);
    }
    
    @Test
    public void testParseMapFile() throws IOException{
        TexasInstrumentsMemoryMapParser parser = new TexasInstrumentsMemoryMapParser();
        String file = TexasInstrumentsMemoryMapParserTest.class.getResource("TexasInstrumentsMapFile.txt").getFile();
        File f = new File(file);
        
        MemoryMapConfigMemory mmcm = new MemoryMapConfigMemory();
        mmcm.add(new MemoryMapConfigMemoryItem("RAMM0", "00000050", "000003b0", "00000195", "0000021b"));
        mmcm = parser.parseMapFile(f, mmcm);
        assertTrue(mmcm.size() > 0);
        
        for (MemoryMapConfigMemoryItem item : mmcm){
          if(item.getName().equals("RAMM0")){
              assertEquals(item.getUsed(), "00000195");
              assertEquals(item.getUnused(), "0000021b");
          }
        }
            
    }
    
    @Test
    public void testGetDefaultWordSize(){
        TexasInstrumentsMemoryMapParser parser = new TexasInstrumentsMemoryMapParser();
        assertEquals("16 equals parser.getDefaultWordSize()", 16, parser.getDefaultWordSize());      
    }
}
