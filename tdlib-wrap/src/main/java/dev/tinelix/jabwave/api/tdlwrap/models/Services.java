package dev.tinelix.jabwave.api.tdlwrap.models;

import java.util.ArrayList;

import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.entities.ServiceEntity;
import dev.tinelix.jabwave.api.base.models.NetworkService;

public class Services extends dev.tinelix.jabwave.api.base.models.Services {


    public Services(BaseClient client) {
        super(client);
    }

    @Override
    public ArrayList<NetworkService> discoverServices() {
        netServices = new ArrayList<>();
        // Well, Telegram not providing XMPP-like services. Therefore, you need to add it manually.
        NetworkService netService = new NetworkService(
                0, 0, "Telegram", "telegram.org", false
        );
        ArrayList<ServiceEntity> entities = netService.getEntities();
        entities.add(new ServiceEntity(0, "Telegram News"));
        entities.add(new ServiceEntity(1, "Trending Stickers"));
        entities.add(new ServiceEntity(2, "Premium Bot"));
        entities.add(new ServiceEntity(3, "BotFather"));
        entities.add(new ServiceEntity(4, "Stickers"));

        netServices.add(netService);
        return netServices;
    }

    @Override
    public ArrayList<NetworkService> getServices() {
        return super.getServices();
    }
}
