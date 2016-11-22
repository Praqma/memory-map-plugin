package net.praqma.jenkins.memorymap.parser.gcc;

import hudson.AbortException;
import hudson.Extension;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.parser.MemoryMapParserDescriptor;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem;
import net.praqma.jenkins.memorymap.util.HexUtils;
import net.praqma.jenkins.memorymap.util.HexUtils.HexifiableString;
import net.praqma.jenkins.memorymap.util.MemoryMapMemorySelectionError;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author Praqma
 */
public class GccMemoryMapParser extends AbstractMemoryMapParser implements Serializable {

    private static final Pattern MEM_SECTIONS = Pattern.compile("^\\s*(\\S+)(?=.*:)", Pattern.MULTILINE);
    private static final Pattern COMMENT_BLOCKS = Pattern.compile("\\/\\*[\\s\\S]*?\\*\\/");
    private static final Logger LOG = Logger.getLogger(GccMemoryMapParser.class.getName());

    @DataBoundConstructor
    public GccMemoryMapParser(String parserUniqueName, String mapFile, String configurationFile, Integer wordSize, Boolean bytesOnGraph, List<MemoryMapGraphConfiguration> graphConfiguration) {
        super(parserUniqueName, mapFile, configurationFile, wordSize, bytesOnGraph, graphConfiguration);
    }

    /**
     * Strip any c-style block comments, e.g slash-star to star-slash.
     * <b>Note:</b> this function down not correctly handle nested comment
     * blocks.
     * <b>Note:</b> this function is a bit greedy, and will incorrectly strip
     * comments inside strings, but this shouldn't be a problem for memory
     * config files.
     *
     * @param seq The content of a file that might contain c-style
     * block-comments
     * @return The same content, that has now had all block-comments stripped
     * out.
     */
    public static CharSequence stripComments(CharSequence seq) {
        Matcher commentMatcher = COMMENT_BLOCKS.matcher(seq);
        return commentMatcher.replaceAll("");
    }

    /**
     * Parses the MEMORY section of the GCC file. Throws an abort exception
     * which will be shown in the Jenkins console log.
     *
     * @param seq The content of the map file
     * @return a list of the defined MEMORY in the map file
     * @throws hudson.AbortException when a illegal value of memory found
     *
     */
    public MemoryMapConfigMemory getMemory(CharSequence seq) throws AbortException {

        Pattern allMemory = Pattern.compile("^\\s*(\\S+).*?(?:ORIGIN|org|o)\\s*=\\s*([^,]*).*?(?:LENGTH|len|l)\\s*\\=\\s*([^\\s]*)", Pattern.MULTILINE);
        Matcher match = allMemory.matcher(seq);
        MemoryMapConfigMemory memory = new MemoryMapConfigMemory();
        while (match.find()) {
            try {
                String hexLength = new HexUtils.HexifiableString(match.group(3)).toValidHexString().rawString;
                MemoryMapConfigMemoryItem item = new MemoryMapConfigMemoryItem(match.group(1), match.group(2), hexLength);
                memory.add(item);
            } catch (Throwable ex) {
                logger.log(Level.SEVERE, "Unable to convert %s to a valid hex string.", ex);
                throw new AbortException(String.format("Unable to convert %s to a valid hex string.", match.group(3)));
            }
        }
        return memory;
    }

    /*
     * SECTIONS {
     * ---
     * secname start BLOCK(align) (NOLOAD) : AT ( ldadr )
     { contents } >region =fill
     ...
     }
     *
     */
    public List<MemoryMapConfigMemoryItem> getSections(CharSequence m) {
        List<MemoryMapConfigMemoryItem> items = new ArrayList<>();

        Pattern section = Pattern.compile("SECTIONS\\s?\\r?\\n?\\{([\\s\\S]*)\\n\\}", Pattern.MULTILINE);

        Matcher sectionMatched = section.matcher(m);
        String sectionString = null;

        while (sectionMatched.find()) {
            sectionString = sectionMatched.group(1);
        }

        //Find the good stuff (SECTION): *SECTIONS\n\{(.*)\n\}
        Matcher fm = MEM_SECTIONS.matcher(sectionString);

        while (fm.find()) {
            MemoryMapConfigMemoryItem it = new MemoryMapConfigMemoryItem(fm.group(1), "0");
            items.add(it);
        }
        return items;
    }

    public GccMemoryMapParser() {
        super();
    }

    public Pattern getLinePatternForMapFile(String sectionName) {
        return Pattern.compile(String.format("^(%s)(\\s+)(\\w+)(\\s+)(\\w+)(\\w*)", sectionName), Pattern.MULTILINE);
    }

    private static class MemoryMapMemItemComparator implements Comparator<MemoryMapConfigMemoryItem>, Serializable {
        @Override
        public int compare(MemoryMapConfigMemoryItem t, MemoryMapConfigMemoryItem t1) {
            long vt = new HexifiableString(t.getOrigin()).getLongValue();
            long vt1 = new HexifiableString(t1.getOrigin()).getLongValue();
            return (vt < vt1 ? -1 : (vt == vt1 ? 1 : 0));
        }
    }

    /**
     * Given an item with length == null. Look down in the list. If we find an
     * item whose length is not null, set the items length to that
     *
     * @param memory the memory list
     * @return a more complete configuration, where i have better values
     */
    public MemoryMapConfigMemory guessLengthOfSections(MemoryMapConfigMemory memory) {
        Collections.sort(memory, new MemoryMapMemItemComparator());

        for (MemoryMapConfigMemoryItem item : memory) {
            if (item.getLength() == null) {
                int itemIndex = memory.indexOf(item);
                for (int i = itemIndex; i > 1; i--) {
                    if (memory.get(i).getLength() != null) {
                        item.setParent(memory.get(i));
                        break;
                    }
                }

            }
        }
        return memory;
    }

    @Override
    public MemoryMapConfigMemory parseMapFile(File f, MemoryMapConfigMemory configuration) throws IOException {
        CharSequence sequence = createCharSequenceFromFile(f);

        for (MemoryMapConfigMemoryItem item : configuration) {
            Matcher m = getLinePatternForMapFile(item.getName()).matcher(sequence);
            while (m.find()) {
                item.setOrigin(m.group(3));
                item.setUsed(m.group(5));
            }
        }

        configuration = guessLengthOfSections(configuration);

        return configuration;
    }

    @Override
    public MemoryMapConfigMemory parseConfigFile(File f) throws IOException {
        //Collect sections from both the MEMORY and the SECTIONS areas from the command file.
        //The memory are the top level components, sections belong to one of these sections
        CharSequence stripped = stripComments(createCharSequenceFromFile(f));

        MemoryMapConfigMemory memConfig = getMemory(stripped);
        memConfig.addAll(getSections(stripped));
        for (MemoryMapGraphConfiguration g : getGraphConfiguration()) {
            for (String gItem : g.itemizeGraphDataList()) {
                for (String gSplitItem : gItem.split("\\+")) {
                    //We will fail if the name of the data section does not match any of the named items in the map file.
                    if (!memConfig.containsSectionWithName(gSplitItem)) {
                        throw new MemoryMapMemorySelectionError(String.format("The memory section named %s not found in map file%nAvailable sections are:%n%s", gSplitItem, memConfig.getItemNames()));
                    }
                }
            }
        }
        return memConfig;
    }

    @Override
    public int getDefaultWordSize() {
        return 8;
    }

    @Symbol("GccMemoryMapParser")
    @Extension
    public static final class DescriptorImpl extends MemoryMapParserDescriptor<GccMemoryMapParser> {

        @Override
        public String getDisplayName() {
            return "Gcc";
        }

        @Override
        public AbstractMemoryMapParser newInstance(StaplerRequest req, JSONObject formData, AbstractMemoryMapParser instance) throws FormException {
            GccMemoryMapParser parser = (GccMemoryMapParser) instance;
            save();
            return parser;
        }
    }
}
