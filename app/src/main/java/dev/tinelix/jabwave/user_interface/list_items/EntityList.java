package dev.tinelix.jabwave.user_interface.list_items;

public class EntityList {
    public int type;
    public String title;
    public EntityList(int type, String title) {
        // Types: 0 - header, 1 - child
        this.type = type;
        this.title = title;
    }
}
