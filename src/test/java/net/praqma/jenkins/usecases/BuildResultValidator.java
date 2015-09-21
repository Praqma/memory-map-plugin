package net.praqma.jenkins.usecases;

import hudson.model.Run;
import java.io.File;
import java.util.HashMap;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class BuildResultValidator {

    private Run<?, ?> build;
    private MemoryMapResultContainer expectedResults;
    private boolean validateGraphs = true;
    private boolean validateValues = true;

    public BuildResultValidator() {
    }

    public BuildResultValidator expect(String json) {
        this.expectedResults = JsonParser.gson.fromJson(json, MemoryMapResultContainer.class);
        return this;
    }

    public BuildResultValidator forBuild(Run<?, ?> build) {
        this.build = build;
        return this;
    }

    public BuildResultValidator validateGraphs(boolean validateGraphs) {
        this.validateGraphs = validateGraphs;
        return this;
    }

    public BuildResultValidator validateValues(boolean validateValues) {
        this.validateValues = validateValues;
        return this;
    }

    public void validate() throws Exception {
        HashMap<String, HashMap<String, String>> expectedValues = expectedResults.getBuildResults().get(build.number);
        HashMap<String, MemoryMapConfigMemoryItem> actualValues = getMemoryItems(build);
        if (validateValues) {
            for (String key : expectedValues.keySet()) {
                assertNotNull(String.format("Expected value for key '%s' not found in build result for build #%s", key, build.number), actualValues.get(key));
                String expectedValue = expectedValues.get(key).get("used");
                String actualValue = actualValues.get(key).getUsed();
                assertEquals(String.format("Expected %s, was %s for key '%s' for build #%s", expectedValue, actualValue, key, build.number), expectedValue, actualValue);
            }
        }

        if (validateGraphs) {
            //TODO: Implement me
        }
    }

    private HashMap<String, MemoryMapConfigMemoryItem> getMemoryItems(Run<?, ?> build) throws Exception {
        File buildFile = new File(build.getLogFile().getParent() + "/build.xml");
        Document document = JsonParser.parseXml(buildFile);

        HashMap<String, MemoryMapConfigMemoryItem> usageMap = new HashMap<>();

        NodeList allNodes = document.getElementsByTagName("*");
        for (int i = 0; i < allNodes.getLength(); i++) {
            if (allNodes.item(i).getNodeName().equals("net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem")) {
                String nodeRaw = JsonParser.getRawXml(allNodes.item(i));
                MemoryMapConfigMemoryItem item = JsonParser.gsonXml.fromXml(nodeRaw, MemoryMapConfigMemoryItem.class);
                usageMap.put(item.getName(), item);
            }
        }
        return usageMap;
    }
}
