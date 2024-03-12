package dev.tinelix.jabwave.net.xmpp.api.entities;

import android.util.Log;

import com.mediaparkpk.base58android.Base58;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.mam.MamManager;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
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
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.net.xmpp.api.XMPPClient;

public class SuperChat extends dev.tinelix.jabwave.api.base.entities.SuperChat {

    private MultiUserChatManager mucm;
    private MultiUserChat muc;

    public SuperChat(String title, int network_type) {
        super(title, network_type, true);
    }

    public SuperChat(Object id, String title) {
        super(id, title, 0, 0, true);
    }

    @Override
    public void join(BaseClient client, String nickname) {
        if(client instanceof XMPPClient xmppClient) {
            try {
                mucm = MultiUserChatManager.getInstanceFor(xmppClient.getConnection());
                muc = mucm.getMultiUserChat(JidCreate.entityBareFrom((String) id));
                if(requiredAuth) {
                    muc.join(Resourcepart.from(nickname));
                    isJoined = true;
                    requiredAuth = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void join(BaseClient client, String nickname, String password_hash) {
        if(password_hash == null) {
            join(client, nickname);
            return;
        }
        if(client instanceof XMPPClient xmppClient) {
            mucm = MultiUserChatManager.getInstanceFor(xmppClient.getConnection());
            try {
                muc = mucm.getMultiUserChat(JidCreate.entityBareFrom((String) id));
                if(requiredAuth) {
                    muc.join(
                            Resourcepart.from(nickname),
                            new String(Base58.decode(password_hash), StandardCharsets.UTF_8)
                    );
                    isJoined = true;
                    requiredAuth = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void loadMessages(BaseClient client) {
        try {
            if(client instanceof XMPPClient xmppClient) {
                mucm = MultiUserChatManager.getInstanceFor(xmppClient.getConnection());
                muc = mucm.getMultiUserChat(JidCreate.entityBareFrom((String) id));
                join(client, "tretdm-jabwave");
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
                MamManager.MamQueryArgs queryArgs = MamManager.MamQueryArgs.builder()
                        .limitResultsSince(calendar.getTime())
                        .setResultPageSize(1000000000)
                        .queryLastPage()
                        .build();
                MamManager.MamQuery mamQuery = mamManager.queryArchive(queryArgs);
                loadMessages(xmppClient, mamQuery.getMessages());
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
            muc.addMessageListener(msg -> {
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
            muc.sendMessage(text);
        } catch (Exception e) {
            e.printStackTrace();
            listener.onFail(new HashMap<>(), e);
        }
    }
}
