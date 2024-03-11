package dev.tinelix.jabwave.api.base.listeners;

import java.util.HashMap;

public interface OnClientUpdateListener {
    boolean onUpdate(HashMap<String, Object> map);
}
