package net.runelite.client.plugins.tscripts.util.controlflow;

import com.google.gson.JsonObject;
import net.runelite.client.plugins.tscripts.util.TextUtil;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for hashing JSON objects.
 */
public class JsonHashUtil
{
    /**
     * Hashes a JSON object using SHA-256.
     *
     * @param jsonObject the JSON object to hash
     * @return the SHA-256 hash of the JSON object
     */
    public static String getSha256Hash(JsonObject jsonObject) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(
                    jsonObject.toString().getBytes(StandardCharsets.UTF_8)
            );
            return TextUtil.bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}