package dev.tinelix.jabwave.net.telegram.api;

import com.mediaparkpk.base58android.Base58;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import dev.tinelix.jabwave.BuildConfig;

public class SecureStorage extends dev.tinelix.jabwave.api.base.SecureStorage {
    /**
        <h2>Secure storage for Telegram API Keys</h2>
        See <i><code>getTDAppToken()</code></i> function in <code>build.gradle</code>
        project file for changing API keys.
        <br>
        <br>
        Application Token & ID can be obtained from
        <a href="my.telegram.org">Telegram Developers website</a>.
    **/
    private String app_id = "";
    private String app_key = "";

    public SecureStorage() {

    }

    @Override
    public HashMap<String, Object> loadAppToken() {
        String build_var = BuildConfig.BUILD_VARIANT;
        if(build_var.equals("combo") || build_var.equals("tdlib")) {
            try {
                String decoded_token = new String(
                        Base58.decode(BuildConfig.TDLIB_APP_TOKEN), StandardCharsets.UTF_8
                );
                // кек, этот реджекс похож на логотип вкусно и точка
                app_id = decoded_token.split("\\.")[0];
                app_key = decoded_token.split("\\.")[1];
                HashMap<String, Object> map = new HashMap<>();
                map.put("app_id", Integer.parseInt(app_id));
                map.put("app_hash", app_key);
                return map;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public HashMap<String, String> createCredentialsMap(String username) {
        return super.createCredentialsMap(username);
    }
}
