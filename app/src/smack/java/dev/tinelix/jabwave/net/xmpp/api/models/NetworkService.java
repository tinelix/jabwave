package dev.tinelix.jabwave.net.xmpp.api.models;

import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jxmpp.jid.impl.JidCreate;

import java.util.ArrayList;
import java.util.List;

import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.entities.ServiceEntity;
import dev.tinelix.jabwave.net.xmpp.api.XMPPClient;

public class NetworkService extends dev.tinelix.jabwave.api.base.models.NetworkService {

    private final ServiceDiscoveryManager sdm;
    private XMPPClient client;

    public NetworkService(XMPPClient client, Object id, int type, String title, String node, boolean isConference) {
        super(id, type, title, node, isConference);
        sdm = ServiceDiscoveryManager.getInstanceFor(client.getConnection());
    }

    @Override
    public ArrayList<ServiceEntity> getEntities(BaseClient client) {
        try {
            isUpdating = true;
            entities = new ArrayList<>();
            this.client = (XMPPClient) client;
            List<DiscoverItems.Item> items =
                    sdm.discoverItems(JidCreate.bareFrom((String) id)).getItems();
            for (DiscoverItems.Item item: items) {
                int type = 0;
                if(entities.size() < 200) {
                    DiscoverInfo info = sdm.discoverInfo(item.getEntityID());
                    String title = "No name";
                    if (info.getIdentities().size() > 0) {
                        title = info.getIdentities().get(0).getName();
                    }
                    for(DiscoverInfo.Feature feat : info.getFeatures()) {
                        if (feat.getVar().equals("http://jabber.org/protocol/muc")) {
                            type = 1;
                            break;
                        } else if(feat.getVar().equals("http://jabber.org/protocol/pubsub")) {
                            type = 2;
                        }
                    }
                    ServiceEntity entity = new ServiceEntity(item.getEntityID().toString(), type, title);
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

    public ArrayList<ServiceEntity> getFullEntities(BaseClient client) {
        try {
            isUpdating = true;
            entities = new ArrayList<>();
            this.client = (XMPPClient) client;
            List<DiscoverItems.Item> items =
                    sdm.discoverItems(JidCreate.bareFrom((String) id)).getItems();
            for (DiscoverItems.Item item: items) {
                int type = 0;
                if(entities.size() < 200) {
                    DiscoverInfo info = sdm.discoverInfo(item.getEntityID());
                    String title = "No name";
                    if (info.getIdentities().size() > 0) {
                        title = info.getIdentities().get(0).getName();
                    }
                    for(DiscoverInfo.Feature feat : info.getFeatures()) {
                        if (feat.getVar().equals("http://jabber.org/protocol/muc")) {
                            type = 1;
                            break;
                        } else if(feat.getVar().equals("http://jabber.org/protocol/pubsub")) {
                            type = 2;
                        }
                    }
                    ServiceEntity entity = new ServiceEntity(item.getEntityID().toString(), type, title);
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

    @Override
    public ServiceEntity getEntityInfo(Object id) {
        try {
            int type = 0;
            DiscoverInfo info = sdm.discoverInfo(JidCreate.bareFrom((String) id));
            String title = "No name";
            if (info.getIdentities().size() > 0) {
                title = info.getIdentities().get(0).getName();
            }
            for (DiscoverInfo.Feature feat : info.getFeatures()) {
                if (feat.getVar().equals("http://jabber.org/protocol/muc")) {
                    type = 1;
                    break;
                } else if (feat.getVar().equals("http://jabber.org/protocol/pubsub")) {
                    type = 2;
                }
            }
            return new ServiceEntity(id, type, title);
        } catch (Exception e) {
            return null;
        }
    }
}
