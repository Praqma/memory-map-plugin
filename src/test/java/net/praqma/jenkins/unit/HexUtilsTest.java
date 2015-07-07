/*
 * The MIT License
 *
 * Copyright 2013 jes.
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

import net.praqma.jenkins.memorymap.util.HexUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jes
 */
public class HexUtilsTest {

    public HexUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test aorund the conversion of the number 1024 to kWords 
     */
    @Test
    public void wordCountTestKilo() {
        assertEquals(1023d / 1024, HexUtils.wordCount("3FF", 16, "kilo"), 0);
        assertEquals(1024d / 1024, HexUtils.wordCount("400", 16, "kilo"), 0);
        assertEquals(1025d / 1024, HexUtils.wordCount("401", 16, "kilo"), 0);

    }

    /**
     * Test aorund the conversion of the number 1024 to MWords 
     */
    @Test
    public void wordCountTestMega() {
        assertEquals(1023d / 1024 / 1024, HexUtils.wordCount("3FF", 16, "Mega"), 0);
        assertEquals(1024d / 1024 / 1024, HexUtils.wordCount("400", 16, "Mega"), 0);
        assertEquals(1025d / 1024 / 1024, HexUtils.wordCount("401", 16, "Mega"), 0);

    }

    /**
     * Test aorund the conversion of the number 1024 to GWords 
     */
    @Test
    public void wordCountTestGiga() {
        assertEquals(1023d / 1024 / 1024 / 1024, HexUtils.wordCount("3FF", 16, "Giga"), 0);
        assertEquals(1024d / 1024 / 1024 / 1024, HexUtils.wordCount("400", 16, "Giga"), 0);
        assertEquals(1025d / 1024 / 1024 / 1024, HexUtils.wordCount("401", 16, "Giga"), 0);

    }

    /**
     * Test aorund the conversion of the number 1024 to kBytes 
     */
    @Test
    public void byteCountTestkilo() {
        assertEquals((1023d / 1024) * (16 / 8), HexUtils.byteCount("3FF", 16, "kilo"), 0);
        assertEquals((1024d / 1024) * (16 / 8), HexUtils.byteCount("400", 16, "kilo"), 0);
        assertEquals((1025d / 1024) * (16 / 8), HexUtils.byteCount("401", 16, "kilo"), 0);

    }

    /**
     * Test aorund the conversion of the number 1024 to MBytes 
     */
    @Test
    public void byteCountTestMega() {
        assertEquals((1023d / 1024 / 1024) * (16 / 8), HexUtils.byteCount("3FF", 16, "Mega"), 0);
        assertEquals((1024d / 1024 / 1024) * (16 / 8), HexUtils.byteCount("400", 16, "Mega"), 0);
        assertEquals((1025d / 1024 / 1024) * (16 / 8), HexUtils.byteCount("401", 16, "Mega"), 0);

    }

    /**
     * Test arund the conversion of the number 1024 to GBytes 
     */
    @Test
    public void byteCountTestGiga() {
        assertEquals((1023d / 1024 / 1024 / 1024) * (16 / 8), HexUtils.byteCount("3FF", 16, "Giga"), 0);
        assertEquals((1024d / 1024 / 1024 / 1024) * (16 / 8), HexUtils.byteCount("400", 16, "Giga"), 0);
        assertEquals((1025d / 1024 / 1024 / 1024) * (16 / 8), HexUtils.byteCount("401", 16, "Giga"), 0);

    }
    
    @Test
    public void testValidHexStrings() {
        HexUtils.HexifiableString valid = new HexUtils.HexifiableString("0xffA");
        HexUtils.HexifiableString valid2 = new HexUtils.HexifiableString("ff");
        assertTrue(valid.isValidHexString());
        assertTrue(valid2.isValidHexString());
        
        HexUtils.HexifiableString notValid = new HexUtils.HexifiableString("2M");
        assertFalse(notValid.isValidHexString());
        HexUtils.HexifiableString notValid2 = new HexUtils.HexifiableString("0x");
        assertFalse(notValid2.isValidHexString());
        
        HexUtils.HexifiableString validMetricValue = new HexUtils.HexifiableString("2M");
        assertTrue(validMetricValue.isValidMetricValue());
        
        HexUtils.HexifiableString invalidMetricValue = new HexUtils.HexifiableString("2");
        assertFalse(invalidMetricValue.isValidMetricValue());
        
        HexUtils.HexifiableString metricToHex = new HexUtils.HexifiableString("2m");
        HexUtils.HexifiableString hexified = metricToHex.toValidHexString();       
    }
}
