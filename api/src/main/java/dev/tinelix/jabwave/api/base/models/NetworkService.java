package dev.tinelix.jabwave.api.base.models;

import java.util.ArrayList;

import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.entities.ServiceEntity;

public class NetworkService {
    public final Object id;
    public final String title;
    public final String node;
    public final int type;
    protected ArrayList<ServiceEntity> entities;
    protected boolean isConference;
    protected boolean isUpdating;

    public NetworkService(Object id, int type, String title, String node, boolean isConference) {
        this.title = title;
        this.id = id;
        this.node = node;
        this.type = type;
        entities = new ArrayList<>();
    }

    public boolean isConference() {
        return isConference;
    }

    public boolean isUpdating() {
        return isUpdating;
    }

    public void setEntities(ArrayList<ServiceEntity> chats) {
        this.entities = entities;
    }

    public ArrayList<ServiceEntity> getEntities() {
        return this.entities;
    }

    public ArrayList<ServiceEntity> getEntities(BaseClient client) {
        return this.entities;
    }
}
