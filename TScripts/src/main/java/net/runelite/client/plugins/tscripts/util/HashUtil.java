package net.runelite.client.plugins.tscripts.util;

import net.runelite.client.plugins.tscripts.util.TextUtil;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for hashing JSON objects.
 */
public class HashUtil
{
    /**
     * Hashes a JSON object using SHA-256.
     *
     * @param string the string to hash
     * @return the SHA-256 hash of the JSON object
     */
    public static String getSha256Hash(String string) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(
                    string.getBytes(StandardCharsets.UTF_8)
            );
            return TextUtil.bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}