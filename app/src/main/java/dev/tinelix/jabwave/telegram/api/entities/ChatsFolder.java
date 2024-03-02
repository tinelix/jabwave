package dev.tinelix.jabwave.telegram.api.entities;

public class ChatsFolder extends Chat {
    // ContactsGroup Class used in Contacts Groups list (AppActivity)
    public String title;

    public ChatsFolder(String title) {
        super(title);
        this.title = title;
    }
}
