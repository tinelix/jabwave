package dev.tinelix.jabwave.net.xmpp.api.models;

import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jxmpp.jid.impl.JidCreate;

import java.util.ArrayList;
import java.util.List;

import dev.tinelix.jabwave.net.base.api.BaseClient;
import dev.tinelix.jabwave.net.base.api.models.NetworkService;
import dev.tinelix.jabwave.net.xmpp.api.XMPPClient;

public class Services extends dev.tinelix.jabwave.net.base.api.models.Services {

    private ServiceDiscoveryManager sdm;
    private XMPPClient client;

    public Services(BaseClient client) {
        super(client);
        this.client = (XMPPClient) client;
    }

    @Override
    public ArrayList<NetworkService> discoverServices() {
        try {
            sdm = ServiceDiscoveryManager.getInstanceFor(client.getConnection());
            List<DiscoverItems.Item> items = sdm.discoverItems(JidCreate.bareFrom(client.server)).getItems();
            netServices = new ArrayList<>();
            for (DiscoverItems.Item item: items) {
                String jid = item.getEntityID().toString();
                DiscoverInfo info = sdm.discoverInfo(JidCreate.bareFrom(jid));
                String title = "";
                if(info.getIdentities().size() > 0) {
                    title = info.getIdentities().get(0).getName();
                }
                boolean isConference = false;
                for (DiscoverInfo.Feature feat: info.getFeatures()) {
                    if (feat.getVar().equals("http://jabber.org/protocol/muc")) {
                        isConference = true;
                        break;
                    }
                }
                int type = 0;
                String node = item.getNode();
                if(node == null) {
                    node = item.getEntityID().toString();
                }
                NetworkService netService = new
                        dev.tinelix.jabwave.net.xmpp.api.entities.NetworkService(
                                jid, type, title, node, isConference
                        );

                netServices.add(netService);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return netServices;
    }

    @Override
    public ArrayList<NetworkService> getServices() {
        return super.getServices();
    }
}
