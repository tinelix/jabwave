package dev.tinelix.jabwave.net.xmpp.api.entities;

import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.mam.MamManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.net.xmpp.api.XMPPClient;

public class Chat extends dev.tinelix.jabwave.api.base.entities.Chat {
    // Contact Class used in Contacts list (AppActivity)
    public int type;
    public String title;
    public String jid;
    public String custom_status;
    public ArrayList<String> groups;
    public BareJid bareJid;
    private VCard vCard;

    public Chat(String title) {
        super(title, 0);
        this.title = title;
    }

    public Chat(String jid, String title, ArrayList<String> groups, String custom_status, int status) {
        super(jid, title, 0);
        this.title = title;
        this.jid = jid;
        this.groups = groups;
        this.custom_status = custom_status;
        this.status = status;
    }

    public Chat(String jid, String title, ArrayList<String> groups, int status) {
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

    public Chat(String jid, String title, int type, ArrayList<String> groups, int status) {
        super(jid, title, type, 0);
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

    @Override
    public void loadMessages(BaseClient client) {
        this.messages = new ArrayList<>();
        MamManager mamManager = MamManager.getInstanceFor(((XMPPClient) client).getConnection());
        try {
            MamManager.MamQueryArgs mamQueryArgs = null;
            if(mamManager.isSupported()) {
                mamQueryArgs = MamManager.MamQueryArgs.builder()
                        .limitResultsToJid(JidCreate.from(jid))
                        .setResultPageSizeTo(10)
                        .queryLastPage()
                        .build();
                MamManager.MamQuery mamQuery = mamManager.queryArchive(mamQueryArgs);
                loadMessages(((XMPPClient) client), mamQuery.getMessages());
            } else {
                throw new UnsupportedOperationException("MamManager is unsupported in current XMPP instance.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMessages(XMPPClient client,
                              List<Message> messages) {
        for (int i = 0; i < messages.size(); i++) {
            org.jivesoftware.smack.packet.Message msg = messages.get(i);
            String chat_id = msg.getFrom().toString();
            String text;
            if(msg.getType() != org.jivesoftware.smack.packet.Message.Type.error) {
                text = msg.getBody();
            } else {
                text = "[Unsupported message type]";
            }

            dev.tinelix.jabwave.api.base.entities.Message message =
                    new dev.tinelix.jabwave.api.base.entities.Message(i, this.id, chat_id, text,
                    new Date(System.currentTimeMillis()), !id.equals(client.jid));
            this.messages.add(message);
        }
    }

    @Override
    public ArrayList<dev.tinelix.jabwave.api.base.entities.Message> getMessages() {
        return messages;
    }

    public VCard getVCard() {
        return vCard;
    }

    public void setVCard(VCard vCard) {
        this.vCard = vCard;
    }

    @Override
    public void sendMessage(BaseClient client, String text, OnClientAPIResultListener listener) {
        try {
            ChatManager chatmanager =
                    ChatManager.getInstanceFor(((XMPPClient) client).getConnection());
            org.jivesoftware.smack.chat2.Chat chat =
                    chatmanager.chatWith(JidCreate.entityBareFrom(jid));
            chatmanager.addOutgoingListener((to, messageBuilder, chat1) -> {
                Message msg = messageBuilder.build();
                if(text.length() > 0) {
                    dev.tinelix.jabwave.api.base.entities.Message message =
                            new dev.tinelix.jabwave.api.base.entities.Message(
                                    0, msg.getFrom(), msg.getFrom(),
                                    text, new Date(System.currentTimeMillis()), false
                            );
                    messages.add(message);
                }
                listener.onSuccess(new HashMap<>());
            });
            chat.send(text);
        } catch (Exception e) {
            e.printStackTrace();
            listener.onFail(new HashMap<>(), e);
        }
    }
}
