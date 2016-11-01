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
import org.apache.commons.lang.StringUtils;

/**
 * @author Praqma
 */
public class HexUtils {

    private static final int BITS_PER_BYTE = 8;

    private static final long DEFAULT = 1;
    private static final long KILO = 1024;
    private static final long MEGA = KILO * 1024;
    private static final long GIGA = MEGA * 1024;

    private static final Map<String, Long> scale = new HashMap<>();
    private static final Pattern VALID_HEX = Pattern.compile("^[0xX]*[0-9a-fA-F]+$");
    private static final Pattern VALID_NUMERICAL = Pattern.compile("^\\d+[mMgGkK]+$");

    static {
        scale.put("default", DEFAULT);
        scale.put("kilo", KILO);
        scale.put("mega", MEGA);
        scale.put("giga", GIGA);
    }

    public static double wordCount(String hexString, int wordSize, String scale) {
        return HexUtils.getRadix(hexString, wordSize) / (double) HexUtils.scale.get(scale.toLowerCase());
    }

    public static double byteCount(String hexString, int wordSize, String scale) {
        return HexUtils.wordCount(hexString, wordSize, scale) * (wordSize / (double) BITS_PER_BYTE);
    }

    private static double getRadix(String hexString, int radix) {
        return (double) Long.parseLong(hexString.replace("0x", "").replaceAll("\\s", ""), radix);
    }

    public static class HexifiableString implements Comparable<HexifiableString> {

        public final String rawString;

        public HexifiableString(String rawString) {
            this.rawString = rawString.replaceAll("\\s", "");
        }

        public HexifiableString(Long value) {
            this.rawString = Long.toHexString(value);
        }

        public Long getLongValue() {
            return Long.parseLong(rawString.replace("0x", ""), 16);
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
            if (isValidHexString()) {
                return this;
            } else {
                HexifiableString newString;
                if (rawString.contains("M") || rawString.contains("m")) {
                    newString = new HexifiableString(Long.parseLong(rawString.replaceAll("[mM]", "")) * MEGA);
                } else if (rawString.contains("G") || rawString.contains("g")) {
                    newString = new HexifiableString(Long.parseLong(rawString.replaceAll("[gG]", "")) * GIGA);
                } else if (rawString.contains("K") || rawString.contains("k")) {
                    newString = new HexifiableString(Long.parseLong(rawString.replaceAll("[kK]", "")) * KILO);
                } else {
                    throw new UnsupportedOperationException(String.format("The string %s contains invalid metric symbols", rawString));
                }
                return newString;
            }
        }

        public HexifiableString toValidHexString() {
            if (isValidHexString()) {
                return this;
            } else {
                return convertToHexForm();
            }
        }

        public HexUtils.HexifiableString getLengthAsHex(HexifiableString other) {
            long diff = Math.abs(getLongValue() - other.getLongValue());
            return new HexUtils.HexifiableString(diff);
        }

        @Override
        public int compareTo(HexifiableString t) {
            long current = getLongValue();
            long other = t.getLongValue();

            if (other > current) {
                return 1;
            } else if (other < current) {
                return -1;
            } else {
                return 0;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (!HexifiableString.class.isAssignableFrom(obj.getClass())) return false;
            final HexifiableString other = (HexifiableString) obj;
            long mine = getLongValue();
            long his  = other.getLongValue();
            return mine == his;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + getLongValue().intValue();
            return hash;
        }

        /**
         * Returns a new HexifiableString with a formatted rawString. Format:
         * 0x\d{8}
         *
         * @return A HexifiableString with a formatted rawString.
         */
        public HexifiableString toFormattedHexString() {
            HexifiableString hexString = toValidHexString();
            String baseString = hexString.rawString.replace("0x", "");

            int targetLength = ((baseString.length() - 1) | 7) + 1; // Next multiple of 8.
            String formattedRawString = "0x" + StringUtils.leftPad(baseString, targetLength, "0");

            return new HexifiableString(formattedRawString);
        }
    }
}
