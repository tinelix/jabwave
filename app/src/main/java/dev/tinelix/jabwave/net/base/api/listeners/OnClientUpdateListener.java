package dev.tinelix.jabwave.net.base.api.listeners;

import java.util.HashMap;

public interface OnClientUpdateListener {
    boolean onUpdate(HashMap<String, Object> map);
}
