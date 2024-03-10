package dev.tinelix.jabwave.net.xmpp.api.models;

import android.util.Log;

import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.List;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.net.base.api.BaseClient;
import dev.tinelix.jabwave.net.base.api.entities.Service;
import dev.tinelix.jabwave.net.xmpp.api.XMPPClient;

public class NetworkServices extends dev.tinelix.jabwave.net.base.api.models.NetworkServices {

    private ServiceDiscoveryManager sdm;
    private XMPPClient client;

    public NetworkServices(BaseClient client) {
        super(client);
        this.client = (XMPPClient) client;
    }

    @Override
    public ArrayList<Service> discoverServices() {
        try {
            sdm = ServiceDiscoveryManager.getInstanceFor(client.getConnection());
            List<DiscoverItems.Item> items = sdm.discoverItems(JidCreate.entityBareFrom(client.server)).getItems();
            services = new ArrayList<>();
            for (DiscoverItems.Item item: items) {
                String jid = item.getEntityID().toString();
                String title = item.getName();
                int type = 0;
                String node = item.getNode();
                Service service = new Service(jid, type, title, node);
                services.add(service);
                Log.d(JabwaveApp.XMPP_SERV_TAG, String.format("XMPP Service XML:\r\n%s", item.toXML().toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return services;
    }

    @Override
    public ArrayList<Service> getServices() {
        return super.getServices();
    }
}
