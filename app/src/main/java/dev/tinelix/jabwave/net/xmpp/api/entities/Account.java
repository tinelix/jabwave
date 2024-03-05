package dev.tinelix.jabwave.net.xmpp.api.entities;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.EntityBareJid;

import dev.tinelix.jabwave.net.xmpp.api.XMPPClient;

public class Account extends dev.tinelix.jabwave.net.base.api.entities.Account {
    private final XMPPClient client;

    public Account(XMPPClient client) {
        super(client);
        this.client = client;
        try {
            VCard card = VCardManager
                    .getInstanceFor(client.getConnection())
                    .loadVCard((EntityBareJid) client.bareJid);
            first_name = card.getFirstName();
            last_name = card.getFirstName();
            id = card.getJabberId();
        } catch (SmackException.NoResponseException |
                XMPPException.XMPPErrorException |
                SmackException.NotConnectedException |
                InterruptedException e) {
            e.printStackTrace();
        }
    }
}
