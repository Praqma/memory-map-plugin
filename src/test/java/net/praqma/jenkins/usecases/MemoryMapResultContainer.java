/*
 * The MIT License
 *
 * Copyright 2015 Mads.
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
package net.praqma.jenkins.usecases;

import java.util.HashMap;

/**
 *
 * @author Mads
 */
public class MemoryMapResultContainer {

    private HashMap<Integer, HashMap<String, HashMap<String, String>>> buildResults = new HashMap<>();     //The raw numeric values for each section
    private HashMap<Integer, HashMap<String, String>> graphResults = new HashMap<>();     //The graph values for each section

    public MemoryMapResultContainer(HashMap<Integer, HashMap<String, HashMap<String, String>>> buildResults) {
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
