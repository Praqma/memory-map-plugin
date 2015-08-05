/*
 * The MIT License
 *
 * Copyright 2015 Praqma.
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

import hudson.model.FreeStyleProject;
import java.net.URL;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.parser.gcc.GccMemoryMapParser;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 *
 * @author thi
 */
public class GccMemoryMapParserIT {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void gcc432_testUsageValues() throws Exception {
        MemoryMapGraphConfiguration graphConfiguration = new MemoryMapGraphConfiguration(".text+.rodata", "432", true);
        GccMemoryMapParser parser = createParser(graphConfiguration);
        parser.setMapFile("prom2_432.map");
        parser.setConfigurationFile("prom432.ld");

        HashMap<String, String> expectedValues = new HashMap<String, String>();
        expectedValues.put(".text", "0x10a008");
        expectedValues.put(".rodata", "0x33fd7");

        TestUtils.testUsageValues(jenkins, parser, "gcc432.zip", expectedValues);
    }

    @Test
    public void gcc482_testUsageValues() throws Exception {
        MemoryMapGraphConfiguration graphConfiguration = new MemoryMapGraphConfiguration(".prom_text+.ram_data", "482", true);
        GccMemoryMapParser parser = createParser(graphConfiguration);
        parser.setMapFile("gcc482.map");
        parser.setConfigurationFile("prom482.ld");

        HashMap<String, String> expectedValues = new HashMap<String, String>();
        expectedValues.put(".prom_text", "0x10cb70");
        expectedValues.put(".ram_data", "0x20c4");

        TestUtils.testUsageValues(jenkins, parser, "gcc482.zip", expectedValues);
    }

    private GccMemoryMapParser createParser(MemoryMapGraphConfiguration... graphConfiguration) {
        return new GccMemoryMapParser(UUID.randomUUID().toString(), null, null, 8, true, Arrays.asList(graphConfiguration));
    }
}