package dev.tinelix.jabwave.net.base.api.entities;

import java.util.ArrayList;

public class Chat {
    // Base Contact Class used in Contacts list (AppActivity)
    public int type;
    public int network_type;
    public String title;
    public Object id;
    public ArrayList<String> groups;
    public int status;
    public byte[] photo;

    public Chat(String title, int network_type) {
        /*
            Network Type available values:
            0 - XMPP
            1 - Telegram
         */
        this.network_type = network_type;
        this.title = title;
        this.groups = new ArrayList<>();
    }

    public Chat(Object id, String title, int network_type) {
        /*
            Network Type available values:
            0 - XMPP
            1 - Telegram
         */
        this.network_type = network_type;
        this.title = title;
        this.id = id;
        this.groups = new ArrayList<>();
    }
}
