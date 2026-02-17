package dev.tinelix.jabwave.api.xmpp.models;

import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jxmpp.jid.impl.JidCreate;

import java.util.ArrayList;
import java.util.List;

import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.models.NetworkService;
import dev.tinelix.jabwave.net.xmpp.api.XMPPClient;

public class Services extends dev.tinelix.jabwave.api.base.models.Services {

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
                String node = item.getNode();
                if(node == null) {
                    node = item.getEntityID().toString();
                }
                NetworkService netService = new
                        dev.tinelix.jabwave.api.xmpp.models.NetworkService(
                                client, jid, 0, title, node, isConference
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
