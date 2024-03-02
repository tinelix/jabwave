package dev.tinelix.jabwave.telegram.api.entities;

public class ContactsGroup extends Contact {
    // ContactsGroup Class used in Contacts Groups list (AppActivity)
    public String title;

    public ContactsGroup(String title) {
        super(title);
        this.title = title;
    }
}
