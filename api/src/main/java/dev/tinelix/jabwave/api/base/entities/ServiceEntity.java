package dev.tinelix.jabwave.api.base.entities;

public class ServiceEntity {
    public final String title;
    public final Object id;
    public final int type;

    public ServiceEntity(Object id, int type, String title) {
        /*  Service Entity Type available values:
            0 - Default
            1 - Group chat
            2 - Channel (or Publish-Subscribe)
         */
        this.type = type;
        this.id = id;
        this.title = title;
    }
}
