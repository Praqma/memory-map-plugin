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
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import net.praqma.jenkins.memorymap.result.MemoryMapParsingResult;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author Praqma
 */
public class MemoryMapParsingResultTest {
    
    @BeforeClass
    public static void setUp() {
        
    }
    
    @Test
    public void data_MemoryMapParsingResult_test() throws Exception {
        MemoryMapParsingResult mmpr = new MemoryMapParsingResult();
        String name = "dataName";
        String rawvalue = "00010100111";
        int value = 1000;
        
        mmpr.setName(name);
        mmpr.setRawvalue(rawvalue);
        mmpr.setValue(value);
        
        assertEquals(name, mmpr.getName());
        assertEquals(rawvalue, mmpr.getRawvalue());
        assertEquals(value, mmpr.getValue());
    }
    
    @Test
    public void data_MemoryMapParsingResult_serialization_test() throws Exception {
        File f = File.createTempFile("testSerialization", ".test");
        FileOutputStream fos = new FileOutputStream(f);
        ObjectOutputStream ous = new ObjectOutputStream(fos);
        ous.writeObject(new MemoryMapParsingResult());
        ous.close();
        f.deleteOnExit();
    }
   
}
