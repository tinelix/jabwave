package dev.tinelix.jabwave.net.base.api.entities;

public class Service {
    public final Object id;
    public final String title;
    public final String node;
    public final int type;

    public Service(Object id, int type, String title, String node) {
        this.title = title;
        this.id = id;
        this.node = node;
        this.type = type;
    }

}
