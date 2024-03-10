package dev.tinelix.jabwave.net.base.api.models;

import java.util.ArrayList;

import dev.tinelix.jabwave.net.base.api.BaseClient;
import dev.tinelix.jabwave.net.base.api.entities.Service;

public class NetworkServices {
    protected final BaseClient client;
    protected ArrayList<Service> services;

    public NetworkServices(BaseClient client) {
        this.client = client;
    }

    public ArrayList<Service> discoverServices() {
        return null;
    }

    public ArrayList<Service> getServices() {
        return services;
    }
}
