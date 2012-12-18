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

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Praqma
 */
public class MemoryMapConfigMemoryItem implements Serializable{
    private String name;
    private String origin;
    private String length;
    private List<String> associatedSections;
    
    public MemoryMapConfigMemoryItem() {} 
    
    public MemoryMapConfigMemoryItem(String name, String origin, String length) {
        this.name = name;
        this.origin = origin;
        this.length = length;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the origin
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * @param origin the origin to set
     */
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    /**
     * @return the length
     */
    public String getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(String length) {
        this.length = length;
    }

    /**
     * @return the associatedSections
     */
    public List<String> getAssociatedSections() {
        return associatedSections;
    }

    /**
     * @param associatedSections the associatedSections to set
     */
    public void setAssociatedSections(List<String> associatedSections) {
        this.associatedSections = associatedSections;
    }

    @Override
    public String toString() {
        return String.format("%s [origin = 0x%s, length = 0x%s]",name, origin, length);
    }
    
    
        
}
