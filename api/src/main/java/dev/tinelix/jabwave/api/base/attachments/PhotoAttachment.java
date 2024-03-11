package dev.tinelix.jabwave.api.base.attachments;

import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;

public class PhotoAttachment extends Attachment {
    protected byte[] array;
    protected int[] size;

    public PhotoAttachment(long id) {
        super(id, 0);
    }

    public PhotoAttachment(long id, boolean isSecret, boolean isSpoiler) {
        super(id, 0, isSecret, isSpoiler);
    }

    public void downloadPhoto(BaseClient client) {

    }

    public void downloadPhoto(BaseClient client, OnClientAPIResultListener listener) {

    }

    public void setContent(byte[] array) {
        this.array = array;
    }

    public byte[] getContent() {
        return array;
    }

    public int[] getSize() {
        return size;
    }

    @Override
    public void updateState(int state) {
        super.updateState(state);
    }
}
