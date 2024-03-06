package dev.tinelix.jabwave.net.xmpp.api.entities;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smackx.mam.MamManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dev.tinelix.jabwave.net.base.api.BaseClient;
import dev.tinelix.jabwave.net.base.api.entities.Message;
import dev.tinelix.jabwave.net.base.api.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.net.xmpp.api.XMPPClient;

public class Chat extends dev.tinelix.jabwave.net.base.api.entities.Chat {
    // Contact Class used in Contacts list (AppActivity)
    public int type;
    public String title;
    public String jid;
    public ArrayList<String> groups;
    public int status;
    private VCard vCard;
    private MamManager.MamQueryArgs mamQueryArgs;

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

    @Override
    public void loadMessages(BaseClient client, OnClientAPIResultListener listener) {
        MamManager mamManager = MamManager.getInstanceFor(((XMPPClient) client).getConnection());
        try {
            if(mamManager.isSupported())
                mamQueryArgs = MamManager.MamQueryArgs.builder()
                        .limitResultsToJid(JidCreate.from(jid))
                        .setResultPageSizeTo(10)
                        .queryLastPage()
                        .build();
            MamManager.MamQuery mamQuery = mamManager.queryArchive(mamQueryArgs);
            loadMessages(((XMPPClient) client), mamQuery.getMessages());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMessages(XMPPClient client,
                              List<org.jivesoftware.smack.packet.Message> messages) {
        ArrayList<Message> msgs = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            org.jivesoftware.smack.packet.Message msg = messages.get(i);
            String chat_id = msg.getFrom().toString();
            String text;
            if(msg.getType() != org.jivesoftware.smack.packet.Message.Type.error) {
                text = msg.getBody();
            } else {
                text = "[Unsupported message type]";
            }

            Message message = new Message(i, this.id, chat_id, text,
                    new Date(System.currentTimeMillis()), !id.equals(client.jid));
            msgs.add(message);
        }
        this.messages = msgs;
    }
}
