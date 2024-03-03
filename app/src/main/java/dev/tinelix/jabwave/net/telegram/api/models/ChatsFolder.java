package dev.tinelix.jabwave.net.telegram.api.models;

import dev.tinelix.jabwave.ui.list.items.base.ChatGroup;

public class ChatsFolder extends ChatGroup {
    // ContactsGroup Class used in Contacts Groups list (AppActivity)
    public String title;

    public ChatsFolder(String title) {
        super(title, 1);
        this.title = title;
    }
}