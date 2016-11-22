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

import net.praqma.jenkins.memorymap.util.HexUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 *
 * @author Praqma
 */
public class MemoryMapConfigMemoryItem implements Serializable, Comparable<MemoryMapConfigMemoryItem> {

    private MemoryMapConfigMemoryItem parent;
    private String name;

    //The "start" address. Not always relevant but in some cases we use the 'origin' and the 'endAddress' to calculate the size
    private String origin;
    private String endAddress;

    //The "length" attribute is used to display MAX values on graphs.
    private String length;

    //The used attributes is what is consumed by the component
    private String used;
    private String unused;

    public MemoryMapConfigMemoryItem() {
    }

    public MemoryMapConfigMemoryItem(String name, String origin) {
        this.name = name != null ? name.trim() : "";
        this.origin = origin;
    }

    public MemoryMapConfigMemoryItem(String name, String origin, String length) {
        this.name = name != null ? name.trim() : "";
        this.origin = origin;
        this.length = length.trim();
    }

    public MemoryMapConfigMemoryItem(String name, String origin, String length, String used, String unused) {
        this.name = name != null ? name.trim() : "";
        this.origin = origin;
        this.length = length;
        this.unused = unused;
        this.used = used;
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
        this.name = name != null ? name.trim() : "";
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
     * @return the endAddress of the segment
     */
    public String getEndAddress() {
        return endAddress;
    }

    /**
     *
     * @param endAddress of the segment
     */
    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    /**
     * Calculates the "length" of a segment if the map file does not explicitly
     * tell it to do so.
     *
     * @param startHex Hex address start
     * @param endHex Hex address end
     */
    public void setCalculatedLength(String startHex, String endHex) {
        HexUtils.HexifiableString sHex = new HexUtils.HexifiableString(startHex);
        HexUtils.HexifiableString eHex = new HexUtils.HexifiableString(endHex);
        HexUtils.HexifiableString len = sHex.getLengthAsHex(eHex);
        setLength(len.rawString);
    }

    private Object getValueOrNotApplicable(Object o) {
        if (o == null) {
            return "N/A";
        } else {
            return o;
        }
    }

    @Override
    public String toString() {
        String base = String.format("%s [origin = %s, length = %s, used = %s, unused = %s, endAddress = %s]", getName(), getOrigin(), getValueOrNotApplicable(getLength()), getUsed(), getValueOrNotApplicable(getUnused()), getValueOrNotApplicable(getEndAddress()));
        if (parent != null) {
            base = base + String.format("%n---- %s", parent);
        }

        return base;
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

    /**
     *
     * @return maximum top level memory
     */
    public String getTopLevelMemoryMax() {
        MemoryMapConfigMemoryItem item = this;
        String max = null;
        while (item != null) {
            max = item.getLength();
            item = item.getParent();
        }
        return max;
    }

    public static boolean allBelongSameParent(MemoryMapConfigMemoryItem... items) {

        MemoryMapConfigMemoryItem parent = null;

        for (MemoryMapConfigMemoryItem it : items) {
            if (!StringUtils.isNumeric(it.getOrigin())) {
                if (it.getParent() == null) {
                    //This is a parent return false
                    return false;
                }

                if (parent == null) {
                    parent = it.getParent();
                }

                if (parent != null && !parent.getName().equals(it.getParent().getName())) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MemoryMapConfigMemoryItem) {
            MemoryMapConfigMemoryItem item = (MemoryMapConfigMemoryItem) obj;
            if (item.name.equals(this.name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 83 * hash + (this.origin != null ? this.origin.hashCode() : 0);
        hash = 83 * hash + (this.length != null ? this.length.hashCode() : 0);
        hash = 83 * hash + (this.endAddress != null ? this.endAddress.hashCode() : 0);
        hash = 83 * hash + (this.used != null ? this.used.hashCode() : 0);
        hash = 83 * hash + (this.unused != null ? this.unused.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(MemoryMapConfigMemoryItem t) {
        HexUtils.HexifiableString hexStringThis = new HexUtils.HexifiableString(getOrigin());
        HexUtils.HexifiableString hexStringOther = new HexUtils.HexifiableString(t.getOrigin());
        return hexStringThis.compareTo(hexStringOther);
    }

    /**
     * @return the parent
     */
    public MemoryMapConfigMemoryItem getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(MemoryMapConfigMemoryItem parent) {
        this.parent = parent;
    }
}
