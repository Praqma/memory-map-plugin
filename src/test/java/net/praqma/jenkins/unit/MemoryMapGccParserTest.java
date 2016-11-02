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
import hudson.AbortException;
import net.praqma.jenkins.memorymap.parser.gcc.GccMemoryMapParser;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
/**
 *
 * @author mads
 */
public class MemoryMapGccParserTest {
        
    @Rule
    public ExpectedException thrown = ExpectedException.none();    
    
    @Test
    public void testParsingOfMemorySegmentInLinkerCommandFile() throws IOException {
        GccMemoryMapParser parser = new GccMemoryMapParser();

        String fileNameLinker = MemoryMapGccParserTest.class.getResource("prom.ld").getFile();
        String fileNameMap = MemoryMapGccParserTest.class.getResource("prom.map").getFile();

        assertNotNull(fileNameLinker);
        assertNotNull(fileNameMap);

        File f = new File(fileNameLinker);
        MemoryMapConfigMemory mem = parser.parseConfigFile(f);
        
        File f2 = new File(fileNameMap);
        parser.parseMapFile(f2, mem);
    }

    @Test
    public void testStrippingNoComments() {
        String testData = "This string contains a line-change" +
                "but no comments";
        String result = GccMemoryMapParser.stripComments(testData).toString();
        assertEquals("Testing successful stripping of lots of block comments", testData, result);
    }

    @Test
    public void testStrippingSimpleComment() {
        String testData = "This string contains an inline /* block comment */ and more";
        String expected = "This string contains an inline  and more";
        String result = GccMemoryMapParser.stripComments(testData).toString();
        assertEquals("Testing successful stripping of lots of block comments", expected, result);
    }

    @Test
    public void testStrippingMultiLineBlockComment() {
        String testData = "This string contains a " +
                "/* multiline" +
                " * block" +
                " * comment */" +
                " and more";
        String expected = "This string contains a " +
                "" +
                " and more";
        String result = GccMemoryMapParser.stripComments(testData).toString();
        assertEquals("Testing successful stripping of lots of block comments", expected, result);
    }

    @Test
    public void testStrippingLotsOfCrazyComments() {
        String testDataWithLotsOfCrazyComments =
                "MEMORY/*" +
                        "" +
                        "" +
                        "*/\n" +
                " {\n" +
                "  /* start comment */\n" +
                "  /* start line comment */ reserved1 (!A)  : ORIGIN = 0, LENGTH = 0x00FFFFF\n" +
                "  application (rx) : ORIGIN = 0x100000, LENGTH = 2M\n" +
                "  reserved2 (!A)  : ORI/**/GIN = 0x03/* ***** */00000, LENGTH = 0x04FFFFF /* COMMENT */\n" +
                "  ram (w)     : ORIGIN = 0x0800000, LENGTH = 2M /* 0x04FFFFF */\n" +
                "  /* more " +
                        "comment */\n" +
                " }";

        String expectedResult =
                "MEMORY\n" +
                        " {\n" +
                        "  \n" +
                        "   reserved1 (!A)  : ORIGIN = 0, LENGTH = 0x00FFFFF\n" +
                        "  application (rx) : ORIGIN = 0x100000, LENGTH = 2M\n" +
                        "  reserved2 (!A)  : ORIGIN = 0x0300000, LENGTH = 0x04FFFFF \n" +
                        "  ram (w)     : ORIGIN = 0x0800000, LENGTH = 2M \n" +
                        "  \n" +
                        " }";

        String result = GccMemoryMapParser.stripComments(testDataWithLotsOfCrazyComments).toString();
        assertEquals("Testing successful stripping of lots of block comments", expectedResult, result);
    }
    

    
    @Test
    public void testAssertCorrectExceptionThrown() throws Exception {
        String illegalAmountOfMemory =
            "MEMORY/*" +
                    "" +
                    "" +
                    "*/\n" +
            " {\n" +
            "  /* start comment */\n" +
            "  /* start line comment */ reserved1 (!A)  : ORIGIN = 0, LENGTH = 0xÅP\n" +
            "  application (rx) : ORIGIN = 0x100000, LENGTH = 2M\n" +
            "  reserved2 (!A)  : ORI/**/GIN = 0x03/* ***** */00000, LENGTH = 0x04ÅN /* COMMENT */\n" +
            "  ram (w)     : ORIGIN = 0x0800000, LENGTH = 2M /* 0x04FFFFF */\n" +
            "  /* more " +
                    "comment */\n" +
            " }";
        
        
        thrown.expect(AbortException.class);
        thrown.expectMessage("Unable to convert 0xÅP to a valid hex string.");  
        
        GccMemoryMapParser parser = new GccMemoryMapParser();
        parser.getMemory(illegalAmountOfMemory);
    }
}
