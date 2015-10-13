package net.praqma.jenkins.usecases;

import java.util.HashMap;

public class ResultContainer {
    // The raw numeric values for each section
    // BUILD NUMBER: SECTION: NAME, VALUE
    // e.g. 1: "RAM": "used", "0x01000000"
    private HashMap<Integer, HashMap<String, HashMap<String, String>>> buildResults = new HashMap<>();
    
    //The graph values for each section
    private HashMap<Integer, HashMap<String, String>> graphResults = new HashMap<>();     

    public ResultContainer(HashMap<Integer, HashMap<String, HashMap<String, String>>> buildResults) {
        this.buildResults = buildResults;
    }

    public HashMap<Integer, HashMap<String, HashMap<String, String>>> getBuildResults() {
        return buildResults;
    }

    public void setBuildResults(HashMap<Integer, HashMap<String, HashMap<String, String>>> buildResults) {
        this.buildResults = buildResults;
    }

    public HashMap<Integer, HashMap<String, String>> getGraphResults() {
        return graphResults;
    }

    public void setGraphResults(HashMap<Integer, HashMap<String, String>> graphResults) {
        this.graphResults = graphResults;
    }

}
