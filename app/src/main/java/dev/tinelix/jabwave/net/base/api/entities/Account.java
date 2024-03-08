package dev.tinelix.jabwave.net.base.api.entities;

import dev.tinelix.jabwave.net.base.api.BaseClient;
import dev.tinelix.jabwave.net.telegram.api.TDLibClient;

public class Account {
    protected BaseClient client;
    public Object id;
    public String first_name;
    public String last_name;
    public byte[] photo;
    public byte[] photo_small;
    public String username;

    public Account(Object id, String first_name, String last_name) {
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
    }

    public Account() {

    }

    public Account(BaseClient client) {
        this.client = client;
    }
}
