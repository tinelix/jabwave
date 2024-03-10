package dev.tinelix.jabwave.net.xmpp.api.entities;

import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jxmpp.jid.impl.JidCreate;

import java.util.ArrayList;
import java.util.List;

import dev.tinelix.jabwave.net.base.api.BaseClient;
import dev.tinelix.jabwave.net.base.api.entities.ServiceEntity;
import dev.tinelix.jabwave.net.xmpp.api.XMPPClient;

public class NetworkService extends dev.tinelix.jabwave.net.base.api.models.NetworkService {

    private XMPPClient client;

    public NetworkService(Object id, int type, String title, String node, boolean isConference) {
        super(id, type, title, node, isConference);
    }

    @Override
    public ArrayList<ServiceEntity> getEntities(BaseClient client) {
        try {
            isUpdating = true;
            entities = new ArrayList<>();
            this.client = (XMPPClient) client;
            ServiceDiscoveryManager sdm =
                    ServiceDiscoveryManager.getInstanceFor(((XMPPClient) client).getConnection());
            List<DiscoverItems.Item> items =
                    sdm.discoverItems(JidCreate.bareFrom((String) id)).getItems();
            for (DiscoverItems.Item item: items) {
                if(entities.size() < 200) {
                    DiscoverInfo info = sdm.discoverInfo(item.getEntityID());
                    String title = "No name";
                    if (info.getIdentities().size() > 0) {
                        title = info.getIdentities().get(0).getName();
                    }
                    ServiceEntity entity = new ServiceEntity(item.getEntityID().toString(), title);
                    entities.add(entity);
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isUpdating = false;
        return entities;
    }
}
