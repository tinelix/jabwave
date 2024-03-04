package dev.tinelix.jabwave.net.base.api.listeners;

import java.util.HashMap;

public interface OnClientAPIResultListener {
    boolean onSuccess(HashMap<String, Object> map);
    boolean onFail(HashMap<String, Object> map, Throwable t);
}
