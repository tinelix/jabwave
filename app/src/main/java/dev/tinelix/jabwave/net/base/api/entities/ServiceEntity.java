package dev.tinelix.jabwave.net.base.api.entities;

public class ServiceEntity {
    public final String title;
    public final Object id;

    public ServiceEntity(Object id, String title) {
        this.id = id;
        this.title = title;
    }
}
