package dev.tinelix.jabwave.net.xmpp.api.models;

import com.mediaparkpk.base58android.Base58;

import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.nio.charset.StandardCharsets;

import dev.tinelix.jabwave.net.xmpp.api.XMPPClient;

public class SuperChats {
    private final MultiUserChatManager mucm;

    public SuperChats(XMPPClient client) {
        this.mucm = MultiUserChatManager.getInstanceFor(client.getConnection());
    }

    public MultiUserChat getMuc(String room, String host) {
        if(this.mucm != null) {
            String jid = String.format("%s@%s", room, host);
            try {
                return mucm.getMultiUserChat(JidCreate.entityBareFrom(jid));
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean join(String room, String host, String nickname, String password_hash) {
        MultiUserChat muc = getMuc(room, host);
        if(muc != null) {
            if(!muc.isJoined()) {
                try {
                    muc.join(
                            Resourcepart.from(nickname),
                            new String(Base58.decode(password_hash), StandardCharsets.UTF_8)
                    );
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            } else {
                throw new IllegalStateException(String.format("MUC with JID %s@%s already joined.", room, host));
            }
        }
        return false;
    }
}
