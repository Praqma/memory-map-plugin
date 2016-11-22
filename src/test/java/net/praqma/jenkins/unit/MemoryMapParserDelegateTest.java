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
package net.praqma.jenkins.unit;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.parser.MemoryMapMapParserDelegate;
import net.praqma.jenkins.memorymap.parser.ti.TexasInstrumentsMemoryMapParser;
import net.praqma.jenkins.memorymap.util.MemoryMapFileNotFoundError;
import org.apache.commons.lang.SerializationUtils;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Praqma
 */
public class MemoryMapParserDelegateTest {
    File file = null;

    @Before
    public void setUp(){

        try {
            file = File.createTempFile("testFile", ".map");
        } catch (IOException ex) {
            fail("Parser did not find the file"+ex);
        }
    }

    @After
    public void tearDown(){
        file.deleteOnExit();
    }

    @Test
    public void isMemoryMapParserDelegateSerializable_test() {
        SerializationUtils.serialize(new MemoryMapMapParserDelegate());
    }

    @Test
    public void findFilePatternWorks_test() throws IOException {

        MemoryMapMapParserDelegate delegate = new MemoryMapMapParserDelegate();

        MemoryMapGraphConfiguration gc = new MemoryMapGraphConfiguration(null, null);
        gc.setGraphDataList("CODE,DATA,CONST");
        gc.setGraphCaption("Config Memory Graph");
        List<MemoryMapGraphConfiguration> graphConfig = Collections.singletonList(gc);

        AbstractMemoryMapParser parser = new TexasInstrumentsMemoryMapParser("TI", "TexasInstrumentsMapFile.txt", "28069_RAM_lnk.cmd", 16, graphConfig, Boolean.TRUE);
        List<AbstractMemoryMapParser> parsers = Collections.singletonList(parser);
        delegate.setParsers(parsers);

        assertNotNull(delegate.getParsers());
        assertNotNull(parser.getMapFile());

        File test = new File(file.getAbsolutePath().substring(0,file.getAbsolutePath().lastIndexOf(File.separator)));
        assertTrue(test.isDirectory());


        try {
            delegate.findFile(test,"*.map");

        } catch(Exception ex) {

            fail("Parser did not find the file"+ex);
        }
    }

    @Test (expected = MemoryMapFileNotFoundError.class)
    public void testFileNotFoundWithBlankPattern() throws IOException{

        File filePath = new File(file.getAbsolutePath().substring(0,file.getAbsolutePath().lastIndexOf(File.separator)));

        MemoryMapMapParserDelegate delegate = new MemoryMapMapParserDelegate();
        delegate.findFile(filePath, " ");
    }

    @Test (expected = MemoryMapFileNotFoundError.class)
    public void testFileNotFoundWithNullPattern() throws IOException{

        File filePath = new File(file.getAbsolutePath().substring(0,file.getAbsolutePath().lastIndexOf(File.separator)));

        MemoryMapMapParserDelegate delegate = new MemoryMapMapParserDelegate();
        delegate.findFile(filePath, null);
    }

    @Test
    public void testFileFound() throws IOException{

        File filePath = new File(file.getAbsolutePath().substring(0,file.getAbsolutePath().lastIndexOf(File.separator)));

        MemoryMapMapParserDelegate delegate = new MemoryMapMapParserDelegate();
        delegate.findFile(filePath, file.getName());
    }
}
