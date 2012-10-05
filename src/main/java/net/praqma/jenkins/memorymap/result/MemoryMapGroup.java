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
package net.praqma.jenkins.memorymap.result;

import java.util.Arrays;
import java.util.LinkedList;

/**
 *
 * @author Praqma
 */
public class MemoryMapGroup extends LinkedList<String> {
    private String groupName;
    private int threshold = Integer.MAX_VALUE;
    
    public MemoryMapGroup() { }
    
    public MemoryMapGroup(String groupName) {
        this.groupName = groupName;
    }
    
    public MemoryMapGroup(String groupName, int threshold) {
        this.groupName = groupName;
        this.threshold = threshold;
    }
    
    /**
     * Factory default flash group
     * @return 
     */
    public static MemoryMapGroup defaultFlashGroup() {
        MemoryMapGroup group = new MemoryMapGroup("Flash");
        group.addAll(Arrays.asList(".econst",".const",".text",".cinit",".switch",".pinit"));
        return group;
    }
    
    /**
     * Factory default ram group
     * @return 
     */
    public static MemoryMapGroup defaultRamGroup() {
        MemoryMapGroup group = new MemoryMapGroup("Ram");
        group.addAll(Arrays.asList(".stack",".ebss",".bss",".sysmem",".esysmem",".cio",".data"));
        return group;  
    }

    /**
     * @return the groupName
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * @param groupName the groupName to set
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * @return the threshold
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * @param threshold the threshold to set
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}
