/*
 * The MIT License
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
import net.praqma.jenkins.memorymap.parser.iar.IarMemoryMapParser;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class IarMemoryMapParserIT {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testUsageValues() throws Exception {
        MemoryMapGraphConfiguration graphConfiguration = new MemoryMapGraphConfiguration(".text+.near", "FLASH");
        IarMemoryMapParser parser = createParser(graphConfiguration);
        parser.setMapFile("EWARM_7.60.1_linker_stm32f091xC.map");
        parser.setConfigurationFile("EWARM_7.60.1_linker_stm32f091xC.icf");

        HashMap<String, String> expectedValues = new HashMap<>();
        expectedValues.put("someSection", "0xFF");

        TestUtils.testUsageValues(jenkins, parser, "iar-ewarm-7.60.1.zip", expectedValues);
    }

    private IarMemoryMapParser createParser(MemoryMapGraphConfiguration... graphConfiguration) {
        return new IarMemoryMapParser(UUID.randomUUID().toString(), null, null, 8, Arrays.asList(graphConfiguration), true);
    }
}
