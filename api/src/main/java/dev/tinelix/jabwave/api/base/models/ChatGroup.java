package dev.tinelix.jabwave.api.base.models;

import dev.tinelix.jabwave.api.base.entities.Chat;

public class ChatGroup extends Chat {
    public boolean withOnlineCount;
    public ChatGroup(String title, boolean withOnlineCount, int network_type) {
        super(title, network_type);
        this.withOnlineCount = withOnlineCount;
    }
}
