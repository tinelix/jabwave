package dev.tinelix.jabwave.net.base.api.attachments;

import dev.tinelix.jabwave.net.base.api.BaseClient;
import dev.tinelix.jabwave.net.base.api.listeners.OnClientAPIResultListener;

public class VideoAttachment extends Attachment {

    protected int duration;
    protected int[] size;
    protected long thumbnail_id;
    protected byte[] thumbnail;
    protected String local_path;

    public VideoAttachment(long id) {
        super(id, 1);
    }

    public void downloadVideo(BaseClient client) {

    }

    public void downloadVideo(BaseClient client, OnClientAPIResultListener listener) {

    }

    public void downloadThumbnail(BaseClient client, OnClientAPIResultListener listener) {

    }

    public int getDuration() {
        return 0;
    }

    public int[] getSize() {
        return size;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public String getLocalPath() {
        return local_path;
    }
}
