package dev.tinelix.jabwave.net.xmpp.api.entities;

import android.util.Log;

import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.net.xmpp.api.XMPPClient;

public class Account extends dev.tinelix.jabwave.api.base.entities.Account {

    public Account(XMPPClient client) {
        super(client);
        this.client = client;
        try {
            Log.d(JabwaveApp.XMPP_SERV_TAG, "Loading Account vCard...");
            VCard card = VCardManager
                    .getInstanceFor(client.getConnection())
                    .loadVCard(client.entitiyBareJid);
            if(card.getFirstName() != null && card.getLastName() != null) {
                first_name = card.getFirstName();
                last_name = card.getLastName();
            } else if(card.getField("FN") != null) {
                String[] names = card.getField("FN").split(" ");
                first_name = names[0];
                if(names.length > 1) {
                    last_name = names[1];
                }
            } else {
                first_name = card.getNickName() != null ? card.getNickName() : "";
                last_name = "";
            }
            id = String.format("%s@%s", client.jid, client.server);
            username = String.format("%s@%s", client.jid, client.server);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
