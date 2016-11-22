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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 *
 * @author jes
 */
public class HexUtilsTest {
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test the conversion of the number 1024 to kWords
     */
    @Test
    public void wordCountTestKilo() {
        assertEquals(1023d / 1024, HexUtils.wordCount("3FF", 16, "kilo"), 0);
        assertEquals(1024d / 1024, HexUtils.wordCount("400", 16, "kilo"), 0);
        assertEquals(1025d / 1024, HexUtils.wordCount("401", 16, "kilo"), 0);

    }

    /**
     * Test the conversion of the number 1024 to MWords
     */
    @Test
    public void wordCountTestMega() {
        assertEquals(1023d / 1024 / 1024, HexUtils.wordCount("3FF", 16, "Mega"), 0);
        assertEquals(1024d / 1024 / 1024, HexUtils.wordCount("400", 16, "Mega"), 0);
        assertEquals(1025d / 1024 / 1024, HexUtils.wordCount("401", 16, "Mega"), 0);

    }

    /**
     * Test the conversion of the number 1024 to GWords
     */
    @Test
    public void wordCountTestGiga() {
        assertEquals(1023d / 1024 / 1024 / 1024, HexUtils.wordCount("3FF", 16, "Giga"), 0);
        assertEquals(1024d / 1024 / 1024 / 1024, HexUtils.wordCount("400", 16, "Giga"), 0);
        assertEquals(1025d / 1024 / 1024 / 1024, HexUtils.wordCount("401", 16, "Giga"), 0);

    }

    /**
     * Test the conversion of the number 1024 to kBytes
     */
    @Test
    public void byteCountTestKilo() {
        assertEquals((1023d / 1024) * (16 / 8), HexUtils.byteCount("3FF", 16, "kilo"), 0);
        assertEquals((1024d / 1024) * (16 / 8), HexUtils.byteCount("400", 16, "kilo"), 0);
        assertEquals((1025d / 1024) * (16 / 8), HexUtils.byteCount("401", 16, "kilo"), 0);

    }

    /**
     * Test the conversion of the number 1024 to MBytes
     */
    @Test
    public void byteCountTestMega() {
        assertEquals((1023d / 1024 / 1024) * (16 / 8), HexUtils.byteCount("3FF", 16, "Mega"), 0);
        assertEquals((1024d / 1024 / 1024) * (16 / 8), HexUtils.byteCount("400", 16, "Mega"), 0);
        assertEquals((1025d / 1024 / 1024) * (16 / 8), HexUtils.byteCount("401", 16, "Mega"), 0);

    }

    /**
     * Test the conversion of the number 1024 to GBytes
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
        HexUtils.HexifiableString hexed = metricToHex.toValidHexString();
    }

    @Test
    public void testFormatting() {
        Map<String, String> stringTests = new HashMap<String,String>(){{
            put("0x00123456", "0x00123456");
            put("7fc3", "0x00007fc3");
            put("64M", "0x04000000");
        }};
        for (Map.Entry<String, String> test : stringTests.entrySet()) {
            HexUtils.HexifiableString hexString = new HexUtils.HexifiableString(test.getKey()).toValidHexString();
            assertTrue("Failure for test '" + test.getKey() + "': Not a valid hex string.", hexString.isValidHexString());
            assertEquals("Failure for test '" + test.getKey() + "': Expected formatted hex string.", test.getValue(), hexString.toFormattedHexString().rawString);
        }

        Map<Long, String> nrTests = new HashMap<Long,String>(){{
            put(1L,"0x00000001");
            put(316L, "0x0000013c");
            put(2000000000L, "0x77359400");
            put(1095216660480L, "0x000000ff00000000"); //JENKINS-31200

        }};
        for (Map.Entry<Long, String> test : nrTests.entrySet()) {
            HexUtils.HexifiableString hexString = new HexUtils.HexifiableString(test.getKey()).toValidHexString();
            assertTrue("Failure for test '" + test.getKey() + "': Not a valid hex string.", hexString.isValidHexString());
            assertEquals("Failure for test '" + test.getKey() + "': Expected formatted hex string.", test.getValue(), hexString.toFormattedHexString().rawString);
        }
    }
}
