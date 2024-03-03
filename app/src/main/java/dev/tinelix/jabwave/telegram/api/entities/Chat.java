package dev.tinelix.jabwave.telegram.api.entities;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Chat extends dev.tinelix.jabwave.core.ui.list.items.base.Chat {
    // Contact Class used in Contacts list (AppActivity)
    public int type;
    public String title;
    public long id;
    public ArrayList<String> groups;
    public int status;
    private VCard vCard;

    public Chat(String title) {
        super(title, 1);
        this.title = title;
    }

    public Chat(long id, String title, ArrayList<String> groups, int status) {
        super(id, title, 1);
        this.title = title;
        this.id = id;
        this.groups = groups;
        this.status = status;
    }

    public Chat(long id) {
        super(id, "(Unknown)", 1);
        this.id = id;
    }

    public VCard getVCard() {
        return vCard;
    }

    public void setVCard(VCard vCard) {
        this.vCard = vCard;
    }

    public void loadPhoto(byte[] bytes, File file) {
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(bytes);
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
