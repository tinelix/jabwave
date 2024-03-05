package dev.tinelix.jabwave.net.base.api.entities;

public class Message {
    public long id;
    public Object author_id;
    public String text;
    private boolean isIncoming;

    public Message(long id, Object author_id, String text, boolean isIncoming) {
        this.id = id;
        this.author_id = author_id;
        this.text = text;
        this.isIncoming = isIncoming;
    }

    public boolean isIncoming() {
        return isIncoming;
    }
}
