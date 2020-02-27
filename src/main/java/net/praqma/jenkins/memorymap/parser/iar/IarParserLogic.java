package net.praqma.jenkins.memorymap.parser.iar;

import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem;
import net.praqma.jenkins.memorymap.util.MemoryMapMemorySelectionError;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IarParserLogic {
    private static final Logger logger = Logger.getLogger(AbstractMemoryMapParser.class.toString());

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

    public void parseGraphData(CharSequence text, MemoryMapGraphConfiguration graph , MemoryMapConfigMemory config) throws MemoryMapMemorySelectionError {
        String[] graphSections = graph.getGraphDataList().split(",");
        for (String sectionNotation : graphSections) {
            String[] sections = sectionNotation.trim().split("\\+");
            for (String section : sections) {
                Matcher matcher = getMatchingPattern(section).matcher(text);

                MemoryMapConfigMemoryItem item = null;
                while (matcher.find()) {
                    item = new MemoryMapConfigMemoryItem(section, "N/A", "N/A", matcher.group(1), "N/A");
                    logger.log(Level.INFO, "parseMapFile -- " + item.toString());
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

    private Pattern getMatchingPattern(String section) {
        if (PATTERNS.containsKey(section)) {
            return PATTERNS.get(section);
        }

        logger.log(Level.WARNING, "No matching pattern defined for section: " + section + "\nFalling back to default pattern.");
        return Pattern.compile(section + "\\s+\\S+\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    }
}
