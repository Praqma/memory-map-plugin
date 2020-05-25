package net.praqma.jenkins.usecases;

import hudson.model.Run;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem;
import net.praqma.jenkins.memorymap.util.HexUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.apache.commons.io.FileUtils;

public class BuildResultValidator {

    private Run<?, ?> build;
    private ResultContainer expectedResults;
    private boolean validateValues = true;

    public BuildResultValidator expect(String json) {
        this.expectedResults = JsonParser.gson.fromJson(json, ResultContainer.class);
        return this;
    }

    public BuildResultValidator forBuild(Run<?, ?> build) {
        this.build = build;
        return this;
    }

    public BuildResultValidator validateGraphs(boolean validateGraphs) {
        return this;
    }

    public BuildResultValidator validateValues(boolean validateValues) {
        this.validateValues = validateValues;
        return this;
    }

    public void validate() throws Exception {
        HashMap<String, HashMap<String, String>> expectedValues = expectedResults.getBuildResults().get(build.number);
        HashMap<String, MemoryMapConfigMemoryItem> actualValues = getMemoryItems(build);

        System.out.println("¤¤¤¤¤ VALIDATING BUILD " + build.number + " ¤¤¤¤¤");
        //printExpected(expectedValues);
        printActual(actualValues);

        if (validateValues) {
            for (Map.Entry<String, HashMap<String, String>> expectedSection : expectedValues.entrySet()) {
                String expectedSectionName = expectedSection.getKey();
                MemoryMapConfigMemoryItem actualSection = actualValues.get(expectedSectionName);
                assertNotNull(String.format("Expected value for key '%s' not found in build result for build #%s: Possible values: %n%s%n======", expectedSectionName, build.number, actualValues), actualSection);

                for (Map.Entry<String, String> x : expectedSection.getValue().entrySet()) {
                    String expectedField = x.getKey();
                    String expectedValue = x.getValue() == null || x.getValue().equals("N/A")
                            ? "N/A"
                            : new HexUtils.HexifiableString(x.getValue()).toFormattedHexString().rawString;

                    Object fieldValue = FieldUtils.readField(actualSection, expectedField, true);
                    String actualValue = fieldValue == null
                            ? "N/A"
                            : new HexUtils.HexifiableString(fieldValue.toString()).toFormattedHexString().rawString;

                    assertEquals(String.format("Expected %s, was %s for key '%s' for build #%s", expectedValue, actualValue, expectedSection, build.number), expectedValue, actualValue);
                }
            }
        }

    }

    public void printExpected(HashMap<String, HashMap<String, String>> expectedValues) {
        System.out.println("¤¤¤ EXPECTED ¤¤¤");
        for (Map.Entry<String, HashMap<String, String>> section : expectedValues.entrySet()) {
            System.out.println("----------");
            System.out.println(section.getKey());
            System.out.println("----------");
            for (Map.Entry<String, String> entry : section.getValue().entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }
        System.out.println("¤ ¤ ¤ ¤ ¤ ¤ ¤ ¤ ¤ ¤");
    }

    public void printActual(HashMap<String, MemoryMapConfigMemoryItem> actualValues) {
        System.out.println("¤¤¤¤ ACTUAL ¤¤¤");
        for (Map.Entry<String, MemoryMapConfigMemoryItem> section : actualValues.entrySet()) {
            System.out.println("----------");
            System.out.println(section.getKey());
            System.out.println("----------");
            System.out.println("origin: " + section.getValue().getOrigin());
            System.out.println("length: " + section.getValue().getLength());
            System.out.println("used:" + section.getValue().getUsed());
        }
        System.out.println("¤ ¤ ¤ ¤ ¤ ¤ ¤ ¤ ¤ ¤");
    }

    private HashMap<String, MemoryMapConfigMemoryItem> getMemoryItems(Run<?, ?> build) throws Exception {
        HashMap<String, MemoryMapConfigMemoryItem> usageMap = new HashMap<>();

        File buildFile = new File(build.getLogFile().getParent() + "/build.xml");
        String fileF = FileUtils.readFileToString(buildFile);
        System.out.println("===XML===");
        System.out.println(fileF);
        System.out.println("===XML===");
        Document document = JsonParser.parseXml(buildFile);
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
