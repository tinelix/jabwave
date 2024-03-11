package dev.tinelix.jabwave.net.xmpp.api.entities;

public class ContactsGroup extends Chat {
    // ContactsGroup Class used in Contacts Groups list (AppActivity)
    public String title;

    public ContactsGroup(String title) {
        super(title);
        this.title = title;
    }
}
