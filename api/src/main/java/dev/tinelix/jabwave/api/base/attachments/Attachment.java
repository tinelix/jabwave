package dev.tinelix.jabwave.api.base.attachments;

public class Attachment {
    private final int type;
    public long id;
    protected boolean isSecret;
    protected boolean isSpoiler;
    public int state; // 0 - Loading, 1 - Loaded

    public Attachment(long id, int type) {
        /*  Attachment Type available values:
            0 - Photo
            1 - Video
            2 - Audio
            3 - Voice
            4 - VideoCircle
            5 - Sticker
            6 - Unsupported Type
         */
        this.type = type;
        this.id = id;
    }

    public Attachment(long id, int type, boolean isSecret, boolean isSpoiler) {
        /*  Attachment Type available values:
            0 - Photo
            1 - Video
            2 - Audio
            3 - Voice
            4 - VideoCircle
            5 - Sticker
            6 - Unsupported Type
         */
        this.type = type;
        this.id = id;
        this.isSecret = isSecret;
        this.isSpoiler = isSpoiler;
    }

    public int getType() {
        return type;
    }

    public boolean isSecret() {
        return isSecret;
    }

    public boolean isSpoiler() {
        return isSpoiler;
    }

    public void updateState(int state) {
        this.state = state;
    }
}
