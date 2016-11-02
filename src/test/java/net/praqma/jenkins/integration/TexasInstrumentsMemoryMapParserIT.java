/*
 * The MIT License
 *
 * Copyright 2015 thi.
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
package net.praqma.jenkins.integration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.parser.ti.TexasInstrumentsMemoryMapParser;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 *
 * @author thi
 */
public class TexasInstrumentsMemoryMapParserIT {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testUsageValues() throws Exception {
        MemoryMapGraphConfiguration graphConfiguration = new MemoryMapGraphConfiguration("RAMM0+RAML0_L3", "432");
        TexasInstrumentsMemoryMapParser parser = createParser(graphConfiguration);
        parser.setMapFile("TexasInstrumentsMapFile.txt");
        parser.setConfigurationFile("28069_RAM_lnk.cmd");

        HashMap<String, String> expectedValues = new HashMap<>();
        expectedValues.put("RAMM0", "00000195");
        expectedValues.put("RAML0_L3", "00001a8f");

        TestUtils.testUsageValues(jenkins, parser, "ti.zip", expectedValues);
    }

    private TexasInstrumentsMemoryMapParser createParser(MemoryMapGraphConfiguration... graphConfiguration) {
        return new TexasInstrumentsMemoryMapParser(UUID.randomUUID().toString(), null, null, 8, Arrays.asList(graphConfiguration), true);
    }
}
