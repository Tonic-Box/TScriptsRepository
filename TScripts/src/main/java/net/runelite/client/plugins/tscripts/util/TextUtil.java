package net.runelite.client.plugins.tscripts.util;

import java.util.Base64;

/**
 * Utility class for encoding and decoding text.
 */
public class TextUtil {
    /**
     * Encodes a string to a byte array using the Cp1252 character set.
     * @param data the string to encode
     * @param startIndex the start index of the string
     * @param endIndex the end index of the string
     * @param output the byte array to encode to
     * @param outputStartIndex the start index of the byte array
     * @return
     */
    public static int encodeStringCp1252(CharSequence data, int startIndex, int endIndex, byte[] output, int outputStartIndex) {
        int var5 = endIndex - startIndex;

        for(int var6 = 0; var6 < var5; ++var6) {
            char var7 = data.charAt(var6 + startIndex);
            if((var7 <= 0 || var7 >= 128) && (var7 < 160 || var7 > 255)) {
                if(var7 == 8364) {
                    output[var6 + outputStartIndex] = -128;
                } else if(var7 == 8218) {
                    output[var6 + outputStartIndex] = -126;
                } else if(var7 == 402) {
                    output[var6 + outputStartIndex] = -125;
                } else if(var7 == 8222) {
                    output[var6 + outputStartIndex] = -124;
                } else if(var7 == 8230) {
                    output[var6 + outputStartIndex] = -123;
                } else if(var7 == 8224) {
                    output[var6 + outputStartIndex] = -122;
                } else if(var7 == 8225) {
                    output[var6 + outputStartIndex] = -121;
                } else if(var7 == 710) {
                    output[var6 + outputStartIndex] = -120;
                } else if(var7 == 8240) {
                    output[var6 + outputStartIndex] = -119;
                } else if(var7 == 352) {
                    output[var6 + outputStartIndex] = -118;
                } else if(var7 == 8249) {
                    output[var6 + outputStartIndex] = -117;
                } else if(var7 == 338) {
                    output[var6 + outputStartIndex] = -116;
                } else if(var7 == 381) {
                    output[var6 + outputStartIndex] = -114;
                } else if(var7 == 8216) {
                    output[var6 + outputStartIndex] = -111;
                } else if(var7 == 8217) {
                    output[var6 + outputStartIndex] = -110;
                } else if(var7 == 8220) {
                    output[var6 + outputStartIndex] = -109;
                } else if(var7 == 8221) {
                    output[var6 + outputStartIndex] = -108;
                } else if(var7 == 8226) {
                    output[var6 + outputStartIndex] = -107;
                } else if(var7 == 8211) {
                    output[var6 + outputStartIndex] = -106;
                } else if(var7 == 8212) {
                    output[var6 + outputStartIndex] = -105;
                } else if(var7 == 732) {
                    output[var6 + outputStartIndex] = -104;
                } else if(var7 == 8482) {
                    output[var6 + outputStartIndex] = -103;
                } else if(var7 == 353) {
                    output[var6 + outputStartIndex] = -102;
                } else if(var7 == 8250) {
                    output[var6 + outputStartIndex] = -101;
                } else if(var7 == 339) {
                    output[var6 + outputStartIndex] = -100;
                } else if(var7 == 382) {
                    output[var6 + outputStartIndex] = -98;
                } else if(var7 == 376) {
                    output[var6 + outputStartIndex] = -97;
                } else {
                    output[var6 + outputStartIndex] = 63;
                }
            } else {
                output[var6 + outputStartIndex] = (byte)var7;
            }
        }

        return var5;
    }

    private static final char[] cp1252AsciiExtension = new char[]{'\u20AC', '\u0000', '\u201A', '\u0192', '\u201E', '\u2026', '\u2020', '\u2021', '\u02C6', '\u2030', '\u0160', '\u2039', '\u0152', '\u0000', '\u017D', '\u0000', '\u0000', '\u2018', '\u2019', '\u201C', '\u201D', '\u2022', '\u2013', '\u2014', '\u02DC', '\u2122', '\u0161', '\u203A', '\u0153', '\u0000', '\u017E', '\u0178'};

    /**
     * Decodes a byte array to a string using the Cp1252 character set.
     * @param data the byte array to decode
     * @param startIndex the start index of the byte array
     * @param length the length of the byte array
     * @return the decoded string
     */
    public static String decodeStringCp1252(byte[] data, int startIndex, int length) {
        char[] var3 = new char[length];
        int var4 = 0;

        for(int var5 = 0; var5 < length; ++var5) {
            int var6 = data[var5 + startIndex] & 255;
            if (var6 != 0) {
                if (var6 >= 128 && var6 < 160) {
                    char var7 = cp1252AsciiExtension[var6 - 128];
                    if (var7 == 0) {
                        var7 = '?';
                    }

                    var6 = var7;
                }

                var3[var4++] = (char)var6;
            }
        }

        return new String(var3, 0, var4);
    }

    /**
     * Decodes a byte array to a string using the UTF-8 character set.
     * @param bytes the byte array to decode
     * @param offset the start index of the byte array
     * @param length the length of the byte array
     * @return the decoded string
     */
    public static String decodeUtf8(byte[] bytes, int offset, int length) {
        char[] chars = new char[length];
        int charIndex = 0;
        int byteIndex = offset;

        while (byteIndex < offset + length) {
            int b1 = bytes[byteIndex++] & 0xFF;
            int codePoint;
            if (b1 < 128) {
                codePoint = b1;
            } else if (b1 < 192) {
                codePoint = 65533;
            } else if (b1 < 224) {
                if (byteIndex < offset + length && (bytes[byteIndex] & 192) == 128) {
                    int b2 = bytes[byteIndex++] & 0x3F;
                    codePoint = ((b1 & 31) << 6) | b2;
                    if (codePoint < 128) {
                        codePoint = 65533;
                    }
                } else {
                    codePoint = 65533;
                }
            } else if (b1 < 240) {
                if (byteIndex + 1 < offset + length && (bytes[byteIndex] & 192) == 128 && (bytes[byteIndex + 1] & 192) == 128) {
                    int b2 = bytes[byteIndex++] & 0x3F;
                    int b3 = bytes[byteIndex++] & 0x3F;
                    codePoint = ((b1 & 15) << 12) | (b2 << 6) | b3;
                    if (codePoint < 2048) {
                        codePoint = 65533;
                    }
                } else {
                    codePoint = 65533;
                }
            } else if (b1 < 248) {
                if (byteIndex + 2 < offset + length && (bytes[byteIndex] & 192) == 128 && (bytes[byteIndex + 1] & 192) == 128 && (bytes[byteIndex + 2] & 192) == 128) {
                    int b2 = bytes[byteIndex++] & 0x3F;
                    int b3 = bytes[byteIndex++] & 0x3F;
                    int b4 = bytes[byteIndex++] & 0x3F;
                    codePoint = ((b1 & 7) << 18) | (b2 << 12) | (b3 << 6) | b4;
                    if (codePoint >= 65536 && codePoint <= 1114111) {
                        codePoint = 65533;
                    } else {
                        codePoint = 65533;
                    }
                } else {
                    codePoint = 65533;
                }
            } else {
                codePoint = 65533;
            }

            chars[charIndex++] = (char) codePoint;
        }

        return new String(chars, 0, charIndex);
    }

    /**
     * Encodes a string to a byte array using the UTF-8 character set.
     * @param dest the byte array to encode to
     * @param destOffset the start index of the byte array
     * @param str the string to encode
     * @return the length of the encoded string
     */
    public static int encodeUtf8(byte[] dest, int destOffset, CharSequence str) {
        int length = str.length();
        int destPos = destOffset;

        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if (c <= 0x7F) {
                dest[destPos++] = (byte) c;
            } else if (c <= 0x7FF) {
                dest[destPos++] = (byte) (0xC0 | c >> 6);
                dest[destPos++] = (byte) (0x80 | c & 0x3F);
            } else {
                dest[destPos++] = (byte) (0xE0 | c >> 12);
                dest[destPos++] = (byte) (0x80 | c >> 6 & 0x3F);
                dest[destPos++] = (byte) (0x80 | c & 0x3F);
            }
        }

        return destPos - destOffset;
    }

    /**
     * Escapes HTML characters in a string.
     * @param input the string to escape
     * @return the escaped string
     */
    public static String escapeHtml(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return input.replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("/", "&#x2F;");
    }

    /**
     * Converts a byte array to a hexadecimal string.
     * @param bytes the byte array to convert
     * @return the hexadecimal string
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
            hex.append(String.format("%02x", b));
        return hex.toString().toLowerCase();
    }

    /**
     * Check if a string is numeric.
     * @param str the string to check
     * @return true if the string is numeric, false otherwise
     */
    public static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Cleans the ironman status icon from player name string if present and
     * corrects spaces.
     * @param text Player name to lookup.
     * @return Cleaned player name.
     */
    public static String sanitize(String text)
    {
        if(text == null)
        {
            return null;
        }
        String cleaned = text.replace('\u00A0', ' ').replace('_', ' ');
        return (cleaned.contains("<img") ? cleaned.substring(text.lastIndexOf('>') + 1) : cleaned).trim().replaceAll("<[^>]+>", "");
    }

    /**
     * Decodes a base64 encoded string.
     * @param encodedString the string to decode
     * @return the decoded string
     */
    public static String decodeBase64(String encodedString) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        return new String(decodedBytes);
    }

    public static String getHex(int number) {
        return Integer.toString(number, 16);
    }
}
