package dev.tinelix.jabwave.xmpp.api.entities;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.util.ArrayList;

import dev.tinelix.jabwave.core.ui.list.items.base.Chat;

public class Contact extends Chat {
    // Contact Class used in Contacts list (AppActivity)
    public int type;
    public String title;
    public String jid;
    public String custom_status;
    public ArrayList<String> groups;
    public int status;
    private VCard vCard;

    public Contact(String title) {
        super(title, 0);
        this.title = title;
    }

    public Contact(String title, String jid, ArrayList<String> groups, String custom_status, int status) {
        super(jid, title, 0);
        this.title = title;
        this.jid = jid;
        this.groups = groups;
        this.custom_status = custom_status;
        this.status = status;
    }

    public Contact(String title, String jid, ArrayList<String> groups, int status) {
        super(jid, title, 0);
        /* Main statuses in XMPP Contacts:
        /       0 - offline,
        /       1 - online,
        /       2 - idle,
        /       3 - not available,
        /       4 - dnd (Do Not Disturb)
         */
        this.title = title;
        this.jid = jid;
        this.groups = groups;
        this.status = status;
    }

    public VCard getVCard() {
        return vCard;
    }

    public void setVCard(VCard vCard) {
        this.vCard = vCard;
    }
}
