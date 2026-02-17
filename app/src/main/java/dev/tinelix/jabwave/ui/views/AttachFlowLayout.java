package dev.tinelix.jabwave.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.api.base.services.ClientService;
import dev.tinelix.jabwave.api.base.attachments.Attachment;
import dev.tinelix.jabwave.api.base.attachments.PhotoAttachment;
import dev.tinelix.jabwave.api.base.attachments.VideoAttachment;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;

public class AttachFlowLayout extends FlowLayout {
    private ArrayList<Attachment> attachments;
    private ArrayList<PhotoAttachment> photos;
    private ArrayList<VideoAttachment> videos;

    public AttachFlowLayout(Context context) {
        super(context);
    }

    public AttachFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void loadAttachments(ArrayList<Attachment> attachments, ClientService service) {
        this.attachments = attachments;
        this.photos = new ArrayList<>();
        this.videos = new ArrayList<>();
        for (int i = 0; i < attachments.size(); i++) {
            Attachment attachment = attachments.get(i);
            switch (attachment.getType()) {
                case 0:
                    if(attachment instanceof PhotoAttachment) {
                        PhotoAttachment photo = (PhotoAttachment) attachment;
                        photos.add(photo);
                    }
                    break;
                case 1:
                    if(attachment instanceof VideoAttachment) {
                        VideoAttachment video = (VideoAttachment) attachment;
                        videos.add(video);
                    }
            }
        }

        loadPhotos(photos, service);
        loadVideos(videos, service);
    }

    private void loadPhotos(ArrayList<PhotoAttachment> photos, ClientService service) {
        int placeholder_resid = R.drawable.ic_photo;
        int error_resid = R.drawable.ic_broken_attach_big_white;
        int dp = (int) (getResources().getDisplayMetrics().scaledDensity);
        if(photos.size() > 1) {
            for (int i = 0; i < photos.size(); i++) {
                PhotoAttachment photo = photos.get(i);
                ImageView iv = new ImageView(getContext());
                iv.setId(View.generateViewId());
                photo.downloadPhoto(service.getClient(), new OnClientAPIResultListener() {
                    @SuppressLint({"CheckResult", "UseCompatLoadingForDrawables"})
                    @Override
                    public boolean onSuccess(HashMap<String, Object> map) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            iv.setId(View.generateViewId());
                            addView(iv);
                            if(photo.state == 1) {
                                Glide.with(getContext())
                                        .load(photo.getContent())
                                        .apply(new RequestOptions()
                                                .override(600, 600)
                                                .placeholder(placeholder_resid)
                                                .error(error_resid)
                                        ).into(iv);
                                iv.setAdjustViewBounds(true);
                                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            } else {
                                iv.setImageDrawable(
                                        getResources().getDrawable(error_resid)
                                );
                            }
                        });
                        return false;
                    }

                    @Override
                    public boolean onFail(HashMap<String, Object> map, Throwable t) {
                        return false;
                    }
                });
                addView(iv);
                ((FlowLayout.LayoutParams) iv.getLayoutParams())
                        .setMargins(
                                0,
                                0,
                                i < photos.size() - 1 ? 2*dp : 0,
                                4*dp
                        );
                iv.setAdjustViewBounds(true);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        } else if(photos.size() == 1) {
            PhotoAttachment photo = photos.get(0);
            ImageView iv = new ImageView(getContext());
            iv.setId(View.generateViewId());
            photo.downloadPhoto(service.getClient(), new OnClientAPIResultListener() {
                @SuppressLint({"CheckResult", "UseCompatLoadingForDrawables"})
                @Override
                public boolean onSuccess(HashMap<String, Object> map) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        int id = View.generateViewId();
                        iv.setId(id);
                        if(findViewById(id) != null) {
                            removeView(iv);
                        }
                        addView(iv);
                        if(photo.state == 2) {
                            Glide.with(getContext())
                                    .load(photo.getContent())
                                    .apply(new RequestOptions()
                                            .override(600, 600)
                                            .placeholder(placeholder_resid)
                                            .error(error_resid)
                                    ).into(iv);
                            iv.setAdjustViewBounds(true);
                        } else {
                            iv.setImageDrawable(
                                    getResources().getDrawable(error_resid)
                            );
                        }
                    });
                    return false;
                }

                @Override
                public boolean onFail(HashMap<String, Object> map, Throwable t) {
                    return false;
                }
            });
            addView(iv);
            rescaleImageView(photo.getSize(), iv);
        }
    }

    private void loadVideos(ArrayList<VideoAttachment> videos, ClientService service) {
        for (int i = 0; i < videos.size(); i++) {
            VideoAttachment video = videos.get(i);
            VideoAttachView vav = new VideoAttachView(getContext());
            int id = View.generateViewId();
            vav.setId(id);
            addView(vav);
            vav.loadAttachment(video, service);
            video.downloadThumbnail(service.getClient(), new OnClientAPIResultListener() {
                @Override
                public boolean onSuccess(HashMap<String, Object> map) {
                    vav.loadAttachment(video, service);
                    return false;
                }

                @Override
                public boolean onFail(HashMap<String, Object> map, Throwable t) {
                    return false;
                }
            });
        }
    }

    private void rescaleImageView(int[] size, ImageView iv) {
        /* Scaling photo to target size
           (code taken from the Conversations XMPP client)

           size[0] -> original photo width
           size[1] -> original photo height
        */
        int dp = (int) (getResources().getDisplayMetrics().scaledDensity);
        float target = 256 * dp;
        float targetH = 300 * dp;
        int scaledW;
        int scaledH;

        if(Math.max(size[1], size[0]) * dp <= target) {
            scaledW = size[0] * dp;
            scaledH = size[1] * dp;
        } else if(Math.max(size[0], size[1]) <= target) {
            scaledW = size[0];
            scaledH = size[1];
        } else if(size[0] <= size[1]){
            scaledW = (int) ((double) size[0] / ((double) size[1] / targetH));
            scaledH = (int) targetH;
        } else {
            scaledW = (int) target;
            scaledH = (int) ((double) size[1] / ((double) size[0] / target));
        }

        iv.getLayoutParams().width = scaledW;
        iv.getLayoutParams().height = scaledH;
    }

    private int getMaxPhotoHeight(ArrayList<PhotoAttachment> photos) {
        List<Integer> heights = new ArrayList<>();
        for(int i = 0; i < photos.size(); i++) {
            PhotoAttachment photo = photos.get(i);
            heights.add(photo.getSize()[1]);
        }
        return Collections.max(heights);
    }

    private int getMaxPhotoWidth(ArrayList<PhotoAttachment> photos) {
        List<Integer> heights = new ArrayList<>();
        for(int i = 0; i < photos.size(); i++) {
            PhotoAttachment photo = photos.get(i);
            heights.add(photo.getSize()[0]);
        }
        return Collections.max(heights);
    }

    private int getMinPhotoHeight(ArrayList<PhotoAttachment> photos) {
        List<Integer> heights = new ArrayList<>();
        for(int i = 0; i < photos.size(); i++) {
            PhotoAttachment photo = photos.get(i);
            heights.add(photo.getSize()[1]);
        }
        return Collections.min(heights);
    }
}
