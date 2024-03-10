package dev.tinelix.jabwave.net.telegram.api.models;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.net.base.api.BaseClient;
import dev.tinelix.jabwave.net.base.api.entities.Service;

public class NetworkServices extends dev.tinelix.jabwave.net.base.api.models.NetworkServices {


    public NetworkServices(BaseClient client) {
        super(client);
    }

    @Override
    public ArrayList<Service> discoverServices() {
        services = new ArrayList<>();
        // Well, Telegram not providing XMPP-like services. Therefore, you need to add it manually.
        Service service = new Service(0, 0, "Telegram News", "telegram");
        services.add(service);
        service = new Service(0, 0, "Trending Stickers", "TrendingStickers");
        services.add(service);
        return services;
    }

    @Override
    public ArrayList<Service> getServices() {
        return super.getServices();
    }
}
