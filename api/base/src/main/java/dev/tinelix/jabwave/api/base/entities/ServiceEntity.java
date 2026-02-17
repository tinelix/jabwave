package dev.tinelix.jabwave.api.base.entities;

/**
 * ServiceEntity - an object that stores information about XMPP-like service.
 */

@SuppressWarnings("ClassCanBeRecord")
public class ServiceEntity {
    public final String title;
    public final Object id;
    public final int type;

    /**
     * Default constructor of ServiceEntity class.
     * @param id Network service ID
     * @param type Entity type
     *             <br>
     *             <br><b>Service Entity Type available values:</b>
     *             <br><code>0</code> - Default
     *             <br><code>1</code> - Group chat
     *             <br><code>2</code> - Channel (or Publish-Subscribe)
     */

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
