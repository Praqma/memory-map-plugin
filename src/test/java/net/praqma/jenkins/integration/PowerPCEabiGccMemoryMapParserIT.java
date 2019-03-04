/*
 * The MIT License
 *
 * Copyright 2019 Praqma.
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
import net.praqma.jenkins.memorymap.parser.powerpceabigcc.PowerPCEabiGccMemoryMapParser;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 *
 * @author thi
 */
public class PowerPCEabiGccMemoryMapParserIT {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void powerpceabigcc_testUsageValues() throws Exception {
        MemoryMapGraphConfiguration graphConfiguration = new MemoryMapGraphConfiguration(".text+.rodata", "432");
        PowerPCEabiGccMemoryMapParser parser = createParser(graphConfiguration);
        parser.setMapFile("prom.map");
        parser.setConfigurationFile("prom.ld");

        HashMap<String, String> expectedValues = new HashMap<>();
        expectedValues.put(".text", "0x10a008");
        expectedValues.put(".rodata", "0x33fd7");

        TestUtils.testUsageValues(jenkins, parser, "powerpceabigcc.zip", expectedValues);
    }

    private PowerPCEabiGccMemoryMapParser createParser(MemoryMapGraphConfiguration... graphConfiguration) {
        return new PowerPCEabiGccMemoryMapParser(UUID.randomUUID().toString(), null, null, 8, true, Arrays.asList(graphConfiguration));
    }
}