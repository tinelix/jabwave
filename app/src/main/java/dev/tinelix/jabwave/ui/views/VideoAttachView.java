package dev.tinelix.jabwave.ui.views;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import dev.tinelix.jabwave.BuildConfig;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.api.base.services.ClientService;
import dev.tinelix.jabwave.api.base.attachments.VideoAttachment;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;

public class VideoAttachView extends FrameLayout {

    private View view;
    private ImageView thumbnail_view;

    public VideoAttachView(@NonNull Context context) {
        super(context);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_video_attach, null);
        this.addView(view);
        thumbnail_view = view.findViewById(R.id.thumbnail);
    }

    public VideoAttachView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_video_attach, null);
        this.addView(view);
        thumbnail_view = view.findViewById(R.id.thumbnail);
    }

    public void loadAttachment(VideoAttachment video, ClientService service) {
        if(video.getThumbnail() != null) {
            thumbnail_view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(getContext())
                    .load(video.getThumbnail())
                    .apply(new RequestOptions()
                            .override(600, 600)
                    ).into(thumbnail_view);
        }
        findViewById(R.id.play_button).setOnClickListener(v -> {
            if(video.state == 2) {
                findViewById(R.id.play_button).setVisibility(VISIBLE);
                findViewById(R.id.progress_card).setVisibility(GONE);
                startVideoPlayer(video.getLocalPath());
            } else if(video.state == 0) {
                findViewById(R.id.play_button).setVisibility(GONE);
                findViewById(R.id.progress_card).setVisibility(VISIBLE);
                video.downloadVideo(service.getClient(), new OnClientAPIResultListener() {
                    @Override
                    public boolean onSuccess(HashMap<String, Object> map) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            findViewById(R.id.play_button).setVisibility(VISIBLE);
                            findViewById(R.id.progress_card).setVisibility(GONE);
                            startVideoPlayer(video.getLocalPath());
                        });
                        return false;
                    }

                    @Override
                    public boolean onFail(HashMap<String, Object> map, Throwable t) {
                        return false;
                    }
                });
            }
        });
    }

    private void startVideoPlayer(String path) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        if(path.length() > 0) {
            intent.setData(
                    FileProvider.getUriForFile(
                            getContext(), BuildConfig.APPLICATION_ID + ".provider", new File(path)
                    )
            );
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            getContext().startActivity(intent);
        }
        Log.d(JabwaveApp.APP_TAG, String.format("Starting video player by %s...", path));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
