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
        entities.add(new ServiceEntity("@telegram", 2, "Telegram News"));
        entities.add(new ServiceEntity("@TrendingStickers", 2, "Trending Stickers"));
        entities.add(new ServiceEntity("@PremiumBot", 0, "Premium Bot"));
        entities.add(new ServiceEntity("@BotFather", 0, "BotFather"));
        entities.add(new ServiceEntity("@Stickers", 0, "Stickers"));

        netServices.add(netService);
        return netServices;
    }

    @Override
    public ArrayList<NetworkService> getServices() {
        return super.getServices();
    }
}
