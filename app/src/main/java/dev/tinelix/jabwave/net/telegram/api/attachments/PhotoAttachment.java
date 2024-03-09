package dev.tinelix.jabwave.net.telegram.api.attachments;

import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.net.base.api.BaseClient;
import dev.tinelix.jabwave.net.base.api.listeners.OnClientAPIResultListener;

public class PhotoAttachment
        extends dev.tinelix.jabwave.net.base.api.attachments.PhotoAttachment {

    private TdApi.File file;

    public PhotoAttachment(long id, boolean isSecret, boolean isSpoiler) {
        super(id, isSecret, isSpoiler);
    }

    public PhotoAttachment(TdApi.MessagePhoto photo) {
        super(photo.photo.sizes[photo.photo.sizes.length - 1].photo.id, photo.isSecret, false);
        file = photo.photo.sizes[photo.photo.sizes.length - 1].photo;
        updateState(file.local.isDownloadingCompleted ? 1 : 0);
        size = new int[] {
                photo.photo.sizes[photo.photo.sizes.length - 1].width,
                photo.photo.sizes[photo.photo.sizes.length - 1].height
        };
    }

    @Override
    public void downloadPhoto(BaseClient client, OnClientAPIResultListener listener) {
        client.send(new TdApi.DownloadFile
                        ((int) id, 1, 0, 1, true),
                new OnClientAPIResultListener() {
                    @SuppressWarnings("ResultOfMethodCallIgnored")
                    @Override
                    public boolean onSuccess(HashMap<String, Object> map) {
                        if (map.get("result") instanceof TdApi.File) {
                            TdApi.File file = (TdApi.File) map.get("result");
                            assert file != null;
                            if (id == file.id) {
                                try {
                                    array = new byte[file.local.downloadedSize];
                                    if (file.local.isDownloadingCompleted) {
                                        FileInputStream fis = new FileInputStream(file.local.path);
                                        fis.read(array);
                                        state = 1;
                                        Log.d(JabwaveApp.TELEGRAM_SERV_TAG,
                                                String.format("Downloaded file #%s.", file.id)
                                        );
                                    } else {
                                        Log.e(JabwaveApp.TELEGRAM_SERV_TAG,
                                                String.format("Unable to download file #%s.", file.id)
                                        );
                                    }
                                    listener.onSuccess(map);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        return false;
                    }

                    @Override
                    public boolean onFail(HashMap<String, Object> map, Throwable t) {
                        return false;
                    }
                }
        );
    }

    @Override
    public void updateState(int state) {
        super.updateState(state);
    }
}
