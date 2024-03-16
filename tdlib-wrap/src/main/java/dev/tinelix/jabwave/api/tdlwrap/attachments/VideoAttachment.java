package dev.tinelix.jabwave.api.tdlwrap.attachments;

import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.FileInputStream;
import java.util.HashMap;

import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.api.tdlwrap.TDLibClient;

public class VideoAttachment extends dev.tinelix.jabwave.api.base.attachments.VideoAttachment {
    private TdApi.Video video;

    public VideoAttachment(TdApi.Video video) {
        super(video.video.id);
        this.video = video;
        this.id = video.video.id;
        this.duration = video.duration;
        this.size = new int[] {
                video.width,
                video.height
        };
        if (video.thumbnail != null) {
            this.thumbnail_id = video.thumbnail.file.id;
        }
    }

    @Override
    public void downloadVideo(BaseClient client, OnClientAPIResultListener listener) {
        client.send(new TdApi.DownloadFile
                        ((int) id, 1, 0, 41943040, false),
                new OnClientAPIResultListener() {
                    @Override
                    public boolean onSuccess(HashMap<String, Object> map) {
                        if (map.get("result") instanceof TdApi.File) {
                            TdApi.File file = (TdApi.File) map.get("result");
                            assert file != null;
                            if (id == file.id) {
                                try {
                                    if (file.local.isDownloadingCompleted) {
                                        VideoAttachment.this.local_path = file.local.path;
                                        state = 2;
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
    }

    @Override
    public void downloadThumbnail(BaseClient client, OnClientAPIResultListener listener) {
        client.send(new TdApi.DownloadFile
                        ((int) thumbnail_id, 1, 0, 2097152, true),
                new OnClientAPIResultListener() {
                    @SuppressWarnings("ResultOfMethodCallIgnored")
                    @Override
                    public boolean onSuccess(HashMap<String, Object> map) {
                        if (map.get("result") instanceof TdApi.File) {
                            TdApi.File file = (TdApi.File) map.get("result");
                            assert file != null;
                            if (id == file.id) {
                                try {
                                    thumbnail = new byte[file.local.downloadedSize];
                                    if (file.local.isDownloadingCompleted) {
                                        FileInputStream fis = new FileInputStream(file.local.path);
                                        fis.read(thumbnail);
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
    }
}
