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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Praqma
 */
public class MemoryMapConfigMemory extends LinkedList<MemoryMapConfigMemoryItem> implements Serializable {

    private static final Logger LOG = Logger.getLogger(MemoryMapConfigMemory.class.getName());

    public MemoryMapConfigMemory() {
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (MemoryMapConfigMemoryItem item : this) {
            builder.append(String.format("[%s] %s", this.indexOf(item), item));
            builder.append("\n");
        }

        return builder.toString();
    }

    public List<String> getItemNames() {
        return stream().map(MemoryMapConfigMemoryItem::getName).collect(Collectors.toList());
    }

    public boolean containsSectionWithName(String name) {
        for (MemoryMapConfigMemoryItem item : this) {
            if (item.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public List<MemoryMapConfigMemoryItem> getItemByNames(String... name) {
        ArrayList<MemoryMapConfigMemoryItem> items = new ArrayList<>();
        for (MemoryMapConfigMemoryItem item : this) {
            for (String memoryName : name) {
                if (item.getName().equals(memoryName)) {
                    items.add(item);
                }
            }
        }
        return items;
    }
}
