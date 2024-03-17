package dev.tinelix.jabwave.api.base.entities;

import android.content.Context;

import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;

public class Account {

    public static final int PASSWORD_TYPE_RESET_AWAIT_7_DAYS = 100;
    public static final int PASSWORD_TYPE_RESET_AWAIT_14_DAYS = 101;
    public static final int PASSWORD_TYPE_RESET_AWAIT_28_DAYS = 102;

    public static final int PASSWORD_STATE_ACTIVE = 3;
    public static final int PASSWORD_STATE_RESET = 2;
    public static final int PASSWORD_STATE_RESET_AWAITING = 1;
    public static final int PASSWORD_STATE_NOTHING = 0;
    protected int password_type;
    protected int password_state;

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

    public void resetPassword(String newPassword) {

    }

    public void resetPassword(String newPassword, OnClientAPIResultListener listener) {

    }

    public int getPasswordType() {
        return password_type;
    }

    public int getPasswordState() {
        return password_state;
    }
}
