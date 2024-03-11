package dev.tinelix.jabwave.api.tdlwrap.models;

import dev.tinelix.jabwave.api.base.models.ChatGroup;

public class ChatsFolder extends ChatGroup {
    // ContactsGroup Class used in Contacts Groups list (AppActivity)
    public String title;

    public ChatsFolder(String title) {
        super(title, true, 1);
        this.title = title;
    }
}
