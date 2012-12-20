/*
 * The MIT License
 *
 * Copyright 2012 Praqma.
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
package net.praqma.jenkins.memorymap.graph;

import java.io.Serializable;

/**
 *
 * @author Praqma
 */

/**
 * FIXME:
 * 
 * This class should be a databound class, see FB case 8235 for references.
 * 
 * @author Praqma
 */
public class MemoryMapGraphConfiguration implements Serializable {
    
    private String graphCaption = "Specify graph caption";
    private String graphDataList ="Specify graph datasets for graph";
    private Boolean displayUsageInBytes = false;
    
    public MemoryMapGraphConfiguration() { }
    public MemoryMapGraphConfiguration(String graphDataList, String graphCaption, Boolean displayUsageInBytes) {
        this.graphDataList = graphDataList;
        this.graphCaption = graphCaption;
        this.displayUsageInBytes = (displayUsageInBytes == null ? false : displayUsageInBytes);
    }
    /**
     * @return the graphCaption
     */
    public String getGraphCaption() {
        return graphCaption;
    }

    /**
     * @param graphCaption the graphCaption to set
     */
    public void setGraphCaption(String graphCaption) {
        this.graphCaption = graphCaption;
    }

    /**
     * @return the graphDataList
     */
    public String getGraphDataList() {
        return graphDataList;
    }

    /**
     * @param graphDataList the graphDataList to set
     */
    public void setGraphDataList(String graphDataList) {
        this.graphDataList = graphDataList;
    }

    /**
     * @return the displayUsageInBytes
     */
    public Boolean getDisplayUsageInBytes() {
        return displayUsageInBytes;
    }

    /**
     * @param displayUsageInBytes the displayUsageInBytes to set
     */
    public void setDisplayUsageInBytes(Boolean displayUsageInBytes) {
        this.displayUsageInBytes = displayUsageInBytes;
    }
    
}
