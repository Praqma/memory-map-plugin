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

/**
 *
 * @author Praqma 
 * 
 * The name of the result is the 'section' name in the configuration file.
*/
public class MemoryMapParsingResult implements Serializable {
    private String name;
    private String rawvalue;
    private int value;
    
    private String length;
    private String used;
    private String unused;
    
    
    //Empty constructor for serialization
    public MemoryMapParsingResult() {}

    @Override
    public String toString() {
        return String.format("[name = %s, value = %s, raw value = %s]", getName(), getValue(), getRawvalue());
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
     * @return the rawvalue
     * 
     */
    @Deprecated
    public String getRawvalue() {
        return rawvalue;
    }

    /**
     * @param rawvalue the rawvalue to set
     */
    @Deprecated
    public void setRawvalue(String rawvalue) {
        this.rawvalue = rawvalue;
    }

    /**
     * @return the value
     */
    @Deprecated
    public int getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    @Deprecated
    public void setValue(int value) {
        this.value = value;
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
     * @return the used
     */
    public String getUsed() {
        return used;
    }

    /**
     * @param used the used to set
     */
    public void setUsed(String used) {
        this.used = used;
    }

    /**
     * @return the unused
     */
    public String getUnused() {
        return unused;
    }

    /**
     * @param unused the unused to set
     */
    public void setUnused(String unused) {
        this.unused = unused;
    }

}
