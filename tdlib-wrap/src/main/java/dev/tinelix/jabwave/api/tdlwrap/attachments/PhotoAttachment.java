package dev.tinelix.jabwave.api.tdlwrap.attachments;

import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.FileInputStream;
import java.util.HashMap;

import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.api.tdlwrap.TDLibClient;

public class PhotoAttachment
        extends dev.tinelix.jabwave.api.base.attachments.PhotoAttachment {

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
        client.send(new TdApi.GetFile((int) id), new OnClientAPIResultListener() {
            @Override
            public boolean onSuccess(HashMap<String, Object> map) {
                if(map.get("result") instanceof TdApi.File) {
                    file = (TdApi.File) map.get("result");
                    assert file != null;
                    if (file.local.downloadedSize != file.size) {
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
                                                        Log.d(TDLibClient.TELEGRAM_SERV_TAG,
                                                                String.format("Downloaded file #%s.", file.id)
                                                        );
                                                    } else {
                                                        Log.e(TDLibClient.TELEGRAM_SERV_TAG,
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
                    } else {
                        if (id == file.id) {
                            try {
                                array = new byte[file.local.downloadedSize];
                                FileInputStream fis = new FileInputStream(file.local.path);
                                fis.read(array);
                                state = 1;
                                Log.d(TDLibClient.TELEGRAM_SERV_TAG,
                                        String.format("Downloaded file #%s.", file.id)
                                );
                                listener.onSuccess(map);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            listener.onSuccess(map);
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean onFail(HashMap<String, Object> map, Throwable t) {
                return false;
            }
        });
    }

    @Override
    public void updateState(int state) {
        super.updateState(state);
    }
}
