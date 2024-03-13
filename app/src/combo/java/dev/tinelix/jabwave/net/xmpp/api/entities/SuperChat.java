package dev.tinelix.jabwave.net.xmpp.api.entities;

import android.util.Log;

import com.mediaparkpk.base58android.Base58;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.mam.MamManager;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.entities.ChatSender;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.api.base.listeners.OnClientUpdateListener;
import dev.tinelix.jabwave.net.xmpp.api.XMPPClient;

public class SuperChat extends dev.tinelix.jabwave.api.base.entities.SuperChat {

    private MultiUserChatManager mucm;
    private MultiUserChat muc;
    private int message_counter;
    private String nickname;
    private String occupant_id;
    private MessageListener msg_listener;

    public SuperChat(String title, int network_type) {
        super(title, network_type, true);
    }

    public SuperChat(Object id, String title) {
        super(id, title, 0, 0, true);
    }

    @Override
    public void join(BaseClient client, String nickname, OnClientUpdateListener listener) {
        this.nickname = nickname;
        this.occupant_id = String.format("%s/%s", id, nickname);
        if(client instanceof XMPPClient xmppClient) {
            try {
                mucm = MultiUserChatManager.getInstanceFor(xmppClient.getConnection());
                muc = mucm.getMultiUserChat(JidCreate.entityBareFrom((String) id));
                muc.join(Resourcepart.from(nickname));
                msg_listener = msg -> {
                    try {
                        String full_id = msg.getFrom().asFullJidIfPossible().toString();
                        dev.tinelix.jabwave.api.base.entities.Message message =
                                new dev.tinelix.jabwave.api.base.entities.Message(
                                        message_counter++,
                                        msg.getFrom(),
                                        full_id,
                                        msg.getBody(),
                                        new Date(System.currentTimeMillis() / 1000),
                                        !full_id.equals(SuperChat.this.occupant_id)
                                );
                        ChatSender sender = new ChatSender(client, full_id, 0);
                        sender.name = full_id.split("/")[1];
                        message.setSender(sender);
                        messages.add(message);
                        listener.onUpdate(new HashMap<>());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
                muc.addMessageListener(msg_listener);
                isJoined = true;
                requiredAuth = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void join(BaseClient client, String nickname, String password_hash, OnClientUpdateListener listener) {
        this.nickname = nickname;
        this.occupant_id = String.format("%s/%s", id, nickname);
        if(password_hash == null) {
            join(client, nickname);
            return;
        }
        if(client instanceof XMPPClient xmppClient) {
            mucm = MultiUserChatManager.getInstanceFor(xmppClient.getConnection());
            try {
                muc = mucm.getMultiUserChat(JidCreate.entityBareFrom((String) id));
                muc.join(
                        Resourcepart.from(nickname),
                        new String(Base58.decode(password_hash), StandardCharsets.UTF_8)
                );
                message_counter = 0;
                msg_listener = msg -> {
                    try {
                        String full_id = msg.getFrom().asFullJidIfPossible().toString();
                        dev.tinelix.jabwave.api.base.entities.Message message =
                                new dev.tinelix.jabwave.api.base.entities.Message(
                                        message_counter++,
                                        msg.getFrom(),
                                        msg.getFrom().asFullJidIfPossible().toString().split("/")[1],
                                        msg.getBody(),
                                        new Date(System.currentTimeMillis() / 1000),
                                        !full_id.equals(SuperChat.this.occupant_id)
                                );
                        ChatSender sender = new ChatSender(client, full_id, 0);
                        sender.name = full_id.split("/")[1];
                        message.setSender(sender);
                        messages.add(message);
                        listener.onUpdate(new HashMap<>());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
                muc.addMessageListener(msg_listener);
                isJoined = true;
                requiredAuth = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void loadMessages(BaseClient client) {
        try {
            if(client instanceof XMPPClient xmppClient) {
                messages = new ArrayList<>();
                if (requiredAuth && !isJoined) {
                    Log.e(JabwaveApp.XMPP_SERV_TAG,
                            String.format("SuperChat #%s requires authorization.", id));
                    return;
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date(System.currentTimeMillis()));
                calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 7);
                MamManager mamManager = MamManager.getInstanceFor(muc);
                if(mamManager.isSupported()) {
                    MamManager.MamQueryArgs queryArgs = MamManager.MamQueryArgs.builder()
                            .limitResultsSince(calendar.getTime())
                            .setResultPageSize(1000000000)
                            .queryLastPage()
                            .build();
                    MamManager.MamQuery mamQuery = mamManager.queryArchive(queryArgs);
                    loadMessages(xmppClient, mamQuery.getMessages());
                }
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
    public void sendMessage(BaseClient client, String text) {
        try {
            muc.sendMessage(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessage(BaseClient client, String text, OnClientAPIResultListener listener) {
        try {
            dev.tinelix.jabwave.api.base.entities.Message message =
            new dev.tinelix.jabwave.api.base.entities.Message(
                    message_counter++, occupant_id, occupant_id,
                    occupant_id, new Date(System.currentTimeMillis()),
                   false
            );
            messages.add(message);
            muc.sendMessage(text);
            listener.onSuccess(new HashMap<>());
        } catch (Exception e) {
            e.printStackTrace();
            listener.onFail(new HashMap<>(), e);
        }
    }

    @Override
    public void leave() {
        try {
            muc.removeMessageListener(msg_listener);
            muc.leave();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.leave();
    }
}
