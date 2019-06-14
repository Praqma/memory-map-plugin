/*
 * The MIT License
 *
 * Copyright 2017 ebmpapst-hs.
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
package net.praqma.jenkins.memorymap.parser.iar;

import hudson.Extension;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.parser.MemoryMapParserDescriptor;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem;
import net.praqma.jenkins.memorymap.util.MemoryMapMemorySelectionError;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jenkinsci.Symbol;

/**
 *
 * @author Kay van der Zander
 * IAR map files used from ARM, STM8 and AVR.
 */
public class IarMemoryMapParser extends AbstractMemoryMapParser {

    private static final HashMap<String, Pattern> PATTERNS = new HashMap<String, Pattern>() {{
        /*********
         * FLASH
         ********/
        put(".text",       Pattern.compile("\\.text\\s+\\S+\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE));          // EWARM_TEXT
        put(".near_func",  Pattern.compile("\\.near_func\\.text\\s+ro\\scode\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE)); // STM8_TEXT
        put("CODE",        Pattern.compile("CODE\\s\\w+\\s\\-\\s\\w+\\s\\((\\S+)\\sbytes\\)", Pattern.MULTILINE));    // AVR_CODE
        put(".rodata",     Pattern.compile("\\.rodata\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE));                // EWARM_CONST
        put(".noinit",     Pattern.compile("\\.noinit\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE));                // EWARM_NOINIT
        put(".near",       Pattern.compile("\\.near\\.noinit\\s+uninit\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE));       // STM8_NOINIT
        put(".iar",        Pattern.compile("\\.iar\\.init_table\\s+const\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE));     // IAR_INIT_TABLE
        put(".intvec",     Pattern.compile("\\.intvec\\s+(?:const|ro\\scode)\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE)); // IAR_INTERRUPT_VECTOR_TABLE
        put("Initializer", Pattern.compile("Initializer\\sbytes\\s+ro\\sdata\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE)); // IAR_INIT_BYTES
        put(".vregs",      Pattern.compile("\\.vregs\\s+uninit\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE));               // STM8_IAR_VREGS
        put(".checksum",   Pattern.compile("\\.checksum\\s+const\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE));             // IAR_CHECKSUM

        /*********
         * RAM
         ********/
         put(".bss",   Pattern.compile("\\.bss\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE));                     // EWARM_BSS
         put(".tiny",  Pattern.compile("\\.tiny\\.bss\\s+zero\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE));              // STM8_BSS_TINY
         put(".near",  Pattern.compile("\\.near\\.bss\\s+zero\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE));              // STM8_BSS_NEAR
         put(".data",  Pattern.compile("\\.data\\s+inited\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE));                  // EWARM_DATA
         put(".tiny",  Pattern.compile("\\.tiny\\.data\\s+inited\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE));           // STM8_DATA
         put("sDATA",  Pattern.compile("\\sDATA\\s\\w+\\s\\-\\s\\w+\\s\\((\\S+)\\sbytes\\)", Pattern.MULTILINE));   // AVR_DATA
         put("CSTACK", Pattern.compile("CSTACK\\s+uninit\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE));                   // IAR_CSTACK

        /*********
         * EEPROM
         ********/
        put("XDATA", Pattern.compile("XDATA\\s\\w+\\s\\-\\s\\w+\\s\\((\\S+)\\sbytes\\)", Pattern.MULTILINE));   // AVR_EEPROM
    }};

    @DataBoundConstructor
    public IarMemoryMapParser(String parserUniqueName, String mapFile, String configurationFile, Integer wordSize, List<MemoryMapGraphConfiguration> graphConfiguration, Boolean bytesOnGraph) {
        super(
            parserUniqueName,
            mapFile,
            configurationFile,
            wordSize,
            bytesOnGraph,
            graphConfiguration
        );
    }

    public IarMemoryMapParser() {
        super();
    }

    @Override
    public MemoryMapConfigMemory parseConfigFile(File f) throws IOException {
        return new MemoryMapConfigMemory();
    }

    @Override
    public MemoryMapConfigMemory parseMapFile(File f, MemoryMapConfigMemory config) throws IOException {
        CharSequence text = createCharSequenceFromFile(f);
        for (MemoryMapGraphConfiguration graph : getGraphConfiguration()) {
            String[] graphSections = graph.getGraphDataList().split(",");
            for (String sectionNotation : graphSections) {
                String[] sections = sectionNotation.trim().split("\\+");
                for (String section : sections) {
                    Matcher matcher = getMatchingPattern(section).matcher(text);

                    MemoryMapConfigMemoryItem item = null;
                    while (matcher.find()) {
                        // Need to build a MemoryMapConfigMemoryItem with as much data as we can squeeze out of the map file:
                        // MemoryMapConfigMemoryItem(String name, String origin, String length, String used, String unused)
                        item = new MemoryMapConfigMemoryItem(section, "not_sure", matcher.group(1));
                        config.add(item);
                    }

                    if (item == null) {
                        logger.logp(
                            Level.WARNING, "parseConfigFile", this.getClass().getName(), String.format("parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) non existing item: %s", section)
                        );
                        throw new MemoryMapMemorySelectionError(String.format("No match found for section named %s", section));
                    }
                }

            }
        }
        return config;
    }

    private Pattern getMatchingPattern(String section) {
        if (PATTERNS.containsKey(section)) {
            return PATTERNS.get(section);
        }

        logger.log(Level.WARNING, "No matching pattern defined for section: " + section + "\nFalling back to default pattern.");
        return Pattern.compile(section + "\\s+\\S+\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    }

    @Override
    public int getDefaultWordSize() {
        return 16;
    }

    @Symbol("IARParser")
    @Extension
    public static final class DescriptorImpl extends MemoryMapParserDescriptor<IarMemoryMapParser> {

        @Override
        public String getDisplayName() {
            return "IAR";
        }

        @Override
        public AbstractMemoryMapParser newInstance(StaplerRequest req, JSONObject formData, AbstractMemoryMapParser instance) throws FormException {
            IarMemoryMapParser parser = (IarMemoryMapParser) instance;
            save();
            return parser;
        }
    }
}
