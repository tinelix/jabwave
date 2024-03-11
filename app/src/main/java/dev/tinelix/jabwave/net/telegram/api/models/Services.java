package dev.tinelix.jabwave.net.telegram.api.models;

import java.util.ArrayList;

import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.models.NetworkService;

public class Services extends dev.tinelix.jabwave.api.base.models.Services {


    public Services(BaseClient client) {
        super(client);
    }

    @Override
    public ArrayList<NetworkService> discoverServices() {
        netServices = new ArrayList<>();
        // Well, Telegram not providing XMPP-like services. Therefore, you need to add it manually.
        NetworkService netService = new NetworkService(0, 0, "Telegram News", "telegram", false);
        netServices.add(netService);
        netService = new NetworkService(0, 0, "Trending Stickers", "TrendingStickers", false);
        netServices.add(netService);
        return netServices;
    }

    @Override
    public ArrayList<NetworkService> getServices() {
        return super.getServices();
    }
}
