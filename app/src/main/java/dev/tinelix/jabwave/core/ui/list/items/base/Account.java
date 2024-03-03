package dev.tinelix.jabwave.core.ui.list.items.base;

public class Account {
    public Object id;
    public String first_name;
    public String last_name;
    public byte[] photo;
    public byte[] photo_small;

    public Account(Object id, String first_name, String last_name) {
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
    }

    public Account() {

    }
}
