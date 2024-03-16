package dev.tinelix.jabwave.api.base.entities;

import dev.tinelix.jabwave.api.base.BaseClient;

public class Account {
    protected BaseClient client;
    public Object id;
    public String first_name;
    public String last_name;
    public byte[] photo;
    public byte[] photo_small;
    public String username;
    protected String phone_number;
    protected String email;
    public String about;

    /**
     * Default constructor for Account class.
     * @param id Account ID
     * @param first_name First name from account information
     * @param last_name Last name from account information
     */

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

    public String getPhoneNumber() {
        return phone_number;
    }

    public String getEmail() {
        return email;
    }
}
