package dev.tinelix.jabwave.net.base.api.models;

import dev.tinelix.jabwave.net.base.api.entities.Chat;

public class ChatGroup extends Chat {
    public boolean withOnlineCount;
    public ChatGroup(String title, boolean withOnlineCount, int network_type) {
        super(title, network_type);
        this.withOnlineCount = withOnlineCount;
    }
}
