package dev.tinelix.jabwave.net.base;

import com.mediaparkpk.base58android.Base58;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class SecureStorage {

    public SecureStorage() {

    }

    public HashMap<String, Object> loadAppToken() {
        return null;
    }

    public HashMap<String, String> createCredentialsMap(
            String username,
            String password
    ) {
        HashMap<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", Base58.encode(password.getBytes(StandardCharsets.UTF_8)));
        return map;
    }

    public HashMap<String, String> createCredentialsMap(
            String server,
            String username,
            String password
    ) {
        HashMap<String, String> map = new HashMap<>();
        map.put("server", server);
        map.put("username", username);
        map.put("password", Base58.encode(password.getBytes(StandardCharsets.UTF_8)));
        return map;
    }

    public HashMap<String, String> createCredentialsMap(
            String username
    ) {
        HashMap<String, String> map = new HashMap<>();
        map.put("username", username);
        return map;
    }
}
