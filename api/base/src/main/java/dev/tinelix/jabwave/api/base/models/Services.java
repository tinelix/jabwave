package dev.tinelix.jabwave.api.base.models;

import java.util.ArrayList;

import dev.tinelix.jabwave.api.base.BaseClient;

public class Services {
    protected final BaseClient client;
    protected ArrayList<NetworkService> netServices;

    public Services(BaseClient client) {
        this.client = client;
    }

    public ArrayList<NetworkService> discoverServices() {
        return null;
    }

    public ArrayList<NetworkService> getServices() {
        return netServices;
    }
}
