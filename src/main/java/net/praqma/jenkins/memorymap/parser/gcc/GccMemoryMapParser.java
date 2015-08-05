package net.praqma.jenkins.memorymap.parser.gcc;

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
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author Praqma
 */
public class GccMemoryMapParser extends AbstractMemoryMapParser implements Serializable {

    private static final Pattern MEM_SECTIONS = Pattern.compile("^\\s+(\\S+)( \\S+.*|\\(\\S+\\)| ):", Pattern.MULTILINE);
    private static final Logger LOG = Logger.getLogger(GccMemoryMapParser.class.getName());

    @DataBoundConstructor
    public GccMemoryMapParser(String parserUniqueName, String mapFile, String configurationFile, Integer wordSize, Boolean bytesOnGraph, List<MemoryMapGraphConfiguration> graphConfiguration) {
        super(parserUniqueName, mapFile, configurationFile, wordSize, bytesOnGraph, graphConfiguration);
    }

    /**
     * @param f
     * @throws java.io.IOException
     * @return a list of the defined MEMORY in the map file
     */
    public MemoryMapConfigMemory getMemory(File f) throws IOException {

        Pattern allMemory = Pattern.compile(".*?^(\\s+\\S+).*?ORIGIN.*?=([^,]*).*?LENGTH\\s\\=(.*).*$", Pattern.MULTILINE);
        CharSequence seq = createCharSequenceFromFile(f);

        Matcher match = allMemory.matcher(seq);
        MemoryMapConfigMemory memory = new MemoryMapConfigMemory();
        while (match.find()) {
            String hexLength = new HexUtils.HexifiableString(match.group(3)).toValidHexString().rawString;
            MemoryMapConfigMemoryItem item = new MemoryMapConfigMemoryItem(match.group(1), match.group(2), hexLength);
            memory.add(item);
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
    public List<MemoryMapConfigMemoryItem> getSections(File f) throws IOException {
        List<MemoryMapConfigMemoryItem> items = new ArrayList<MemoryMapConfigMemoryItem>();
        CharSequence m = createCharSequenceFromFile(f);

        Pattern section = Pattern.compile(".*SECTIONS\\s?\\r?\\n?\\{(.*)\\n\\}", Pattern.MULTILINE | Pattern.DOTALL);

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
        Pattern p = Pattern.compile(String.format("^(%s)(\\s+)(\\w+)(\\s+)(\\w+)(\\w*)", sectionName), Pattern.MULTILINE);
        return p;
    }

    public class MemoryMapMemItemComparator implements Comparator<MemoryMapConfigMemoryItem> {

        @Override
        public int compare(MemoryMapConfigMemoryItem t, MemoryMapConfigMemoryItem t1) {
            int vt = new HexifiableString(t.getOrigin()).getIntegerValue();
            int vt1 = new HexifiableString(t1.getOrigin()).getIntegerValue();
            return (vt < vt1 ? -1 : (vt == vt1 ? 1 : 0));
        }

    }

    /**
     * Given an item with length == null. Look down in the list. If we find an
     * item whoose length is not null, set the items length to that
     *
     * @param memory
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
        //The memory are the top level components, sections belong to one of thsese sections
        MemoryMapConfigMemory memconfig = getMemory(f);
        memconfig.addAll(getSections(f));
        for (MemoryMapGraphConfiguration g : getGraphConfiguration()) {
            for (String gItem : g.itemizeGraphDataList()) {
                for (String gSplitItem : gItem.split("\\+")) {
                    //We will fail if the name of the data section does not match any of the named items in the map file.
                    if (!memconfig.containsSectionWithName(gSplitItem)) {                        
                        throw new MemoryMapMemorySelectionError(String.format("The memory section named %s not found in map file%nAvailable sections are:%n%s", gSplitItem, memconfig.getItemNames()));                        
                    }
                }
            }
        }
        return memconfig;
    }

    @Override
    public int getDefaultWordSize() {
        return 8;
    }

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
