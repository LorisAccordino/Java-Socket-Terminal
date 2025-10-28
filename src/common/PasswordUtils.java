package common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordUtils {

    private PasswordUtils() {}

    // Hash of the password with MD5
    public static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(password.getBytes());
            // Convert in hex
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not available", e);
        }
    }

    // Check if the password match the hash
    public static boolean verify(String password, String hash) {
        return hash(password).equals(hash);
    }
}