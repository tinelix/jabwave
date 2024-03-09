package dev.tinelix.jabwave.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.services.base.ClientService;
import dev.tinelix.jabwave.net.base.api.attachments.Attachment;
import dev.tinelix.jabwave.net.base.api.attachments.PhotoAttachment;
import dev.tinelix.jabwave.net.base.api.listeners.OnClientAPIResultListener;

public class AttachView extends FlowLayout {
    private ArrayList<Attachment> attachments;
    private ArrayList<PhotoAttachment> photos;

    public AttachView(Context context) {
        super(context);
    }

    public AttachView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void loadAttachments(ArrayList<Attachment> attachments, ClientService service) {
        this.attachments = attachments;
        this.photos = new ArrayList<>();
        for (int i = 0; i < attachments.size(); i++) {
            Attachment attachment = attachments.get(i);
            switch (attachment.getType()) {
                case 0:
                    if(attachment instanceof PhotoAttachment) {
                        PhotoAttachment photo = (PhotoAttachment) attachment;
                        photos.add(photo);
                    }
                    break;
            }
        }

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
        } else {
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
            rescaleImageView(photo.getSize(), iv);
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
