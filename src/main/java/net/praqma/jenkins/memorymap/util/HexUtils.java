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
package net.praqma.jenkins.memorymap.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Praqma
 */
public class HexUtils {
    
    private static final int HEXA_RADIX = 16;
    private static final int BITS_PER_BYTE = 8;
    
    private static final double DEFAULT = 1d;
    private static final double KILO = 1024;
    private static final double MEGA = KILO*1024;
    private static final double GIGA = MEGA*1024;
        
    private static final Map<String, Double> scale = new HashMap<String, Double>();
    private static final Pattern VALID_HEX = Pattern.compile("^[0xX]*[0-9a-fA-F]+$");
    private static final Pattern VALID_NUMERICAL = Pattern.compile("^\\d+[mMgGkK]+$");
    
    static {
        scale.put("default", DEFAULT);
        scale.put("kilo", KILO);
        scale.put("mega", MEGA);
        scale.put("giga", GIGA);
    }
           
    public static double wordCount(String hexString, int wordSize, String scale) {
        return (HexUtils.getRadix(hexString, HEXA_RADIX)) / HexUtils.scale.get(scale.toLowerCase());
    }
       
    public static double byteCount(String hexString, int wordSize, String  scale) {
        return HexUtils.wordCount(hexString, HEXA_RADIX, scale) * (wordSize / BITS_PER_BYTE);
    }
     
    private static double getRadix(String hexString, int radix) {
        Double i = (double)(Integer.parseInt(hexString.replace("0x","").replaceAll("\\s", ""), radix));
        return i;
    }
    
    public static class HexifiableString implements Comparable<HexifiableString> {
        
        public final String rawString;
        
        public HexifiableString(String rawString) {
            this.rawString = rawString.replaceAll("\\s", "");
        }
        
        public HexifiableString(Integer value) {
            this.rawString = Integer.toHexString(value);
        }
        
        public Integer getIntegerValue() {
            return Integer.parseInt(rawString.replace("0x",""), 16);
        }
        
        public boolean isValidMetricValue() {
            return VALID_NUMERICAL.matcher(rawString).matches();
        }
        
        public boolean isValidHexString() {
            return VALID_HEX.matcher(rawString).matches();
        }

        @Override
        public String toString() {
            return rawString;
        }
        
        private HexifiableString convertToHexForm() {
            if(isValidHexString()) {
                return this;
            } else {
                HexifiableString newString = null;
                if (rawString.contains("M") || rawString.contains("m")) {
                    newString = new HexifiableString(Integer.parseInt(rawString.replaceAll("[mM]", ""))*(int)MEGA);
                } else if (rawString.contains("G") || rawString.contains("g")) {
                    newString = new HexifiableString(Integer.parseInt(rawString.replaceAll("[gG]", ""))*(int)GIGA);
                } else if (rawString.contains("K") || rawString.contains("k")) {
                    newString = new HexifiableString(Integer.parseInt(rawString.replaceAll("[kK]", ""))*(int)KILO);
                } else {
                    throw new UnsupportedOperationException(String.format("The string %s contains invalid metric symbols", rawString));
                }
                return newString;
            }
        }
        
        public HexifiableString toValidHexString() {
            if(isValidHexString()) {
                return this;
            } else {
                return convertToHexForm();
            }
        }
        
        public HexUtils.HexifiableString getLengthAsHex(HexifiableString other) {
           int diff = Math.abs(getIntegerValue() - other.getIntegerValue());
           return new HexUtils.HexifiableString(diff);
        }

        @Override
        public int compareTo(HexifiableString t) {
            int current = Integer.parseInt(rawString.trim().replace("0x",""), 16);
            int other = Integer.parseInt(t.rawString.trim().replace("0x",""), 16);
            
            if (other > current) {
                return 1;
            } else if (other < current) {
                return -1;
            } else {
                return 0;
            }            
        }
    }
}
