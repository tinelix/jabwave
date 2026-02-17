package dev.tinelix.jabwave.api.base.attachments;

public class Attachment {

    /** <b>Attachment</b> class used for messages bubbles.**/

    private final int type;
    public long id;
    protected boolean isSecret;
    protected boolean isSpoiler;
    public int state; // 0 - Loading, 1 - Loaded

    /**
     * Default constructor for Attachment class.
     * @param id Attachment ID
     * @param type Attachment Type
     * <br><br>
     * <b>Attachment Type available values:</b>
     * <br><code>0</code> - <i>Photo</i>
     * <br>(must be PhotoAttachment)
     * <br><code>1</code> - <i>Video</i>
     * <br>(must be VideoAttachment)
     * <br><code>2</code> - <i>Audio</i>
     * <br>(must be AudioAttachment)
     * <br><code>3</code> - <i>Voice</i>
     * <br>(must be VoiceAttachment)
     * <br><code>4</code> - <i>VideoCircle</i>
     * <br>(must be VideoCircleAttachment, extended from VoiceAttachment)
     * <br><code>5</code> - <i>Sticker</i>
     * <br>(must be StickerAttachment)
     * <br><code>6</code> - <i>Unsupported type</i>
     */

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
