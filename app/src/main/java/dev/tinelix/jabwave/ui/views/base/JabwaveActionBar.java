package dev.tinelix.jabwave.ui.views.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import dev.tinelix.jabwave.R;

public class JabwaveActionBar extends Toolbar {

    public JabwaveActionBar(@NonNull Context context) {
        super(context);
    }

    public JabwaveActionBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public JabwaveActionBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("ResourceAsColor")
    public void setNavigationIconTint(@ColorRes int color_res, boolean fromResources) {
        try {
            if (getNavigationIcon() != null) {
                // Setting correct tint for navigation icon
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getNavigationIcon()
                            .setTint(
                                    fromResources ? getResources().getColor(color_res) : color_res
                            );
                } else {
                    getNavigationIcon()
                            .setColorFilter(
                                    fromResources ? getResources().getColor(color_res) : color_res,
                                    PorterDuff.Mode.SRC_IN
                            );
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void setTitle(int resId) {
        super.setTitle(resId);
        new Handler().postDelayed(() -> {
            TextView title_tv = findViewById(R.id.ab_title);
            title_tv.setText(resId);
        }, 20);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        new Handler().postDelayed(() -> {
            TextView title_tv = findViewById(R.id.ab_title);
            if(title == null) {
                title_tv.setText(R.string.app_name);
            } else {
                title_tv.setText(title);
            }
        }, 20);
    }

    @Override
    public void setSubtitle(int resId) {
        super.setSubtitle(resId);
        TextView subtitle_tv = findViewById(R.id.ab_subtitle);
        subtitle_tv.setText(resId);
        subtitle_tv.setVisibility(VISIBLE);
    }

    @Override
    public void setSubtitle(CharSequence subtitle) {
        super.setSubtitle(subtitle);
        TextView subtitle_tv = findViewById(R.id.ab_subtitle);
        if(subtitle == null) {
            subtitle_tv.setVisibility(GONE);
        } else {
            subtitle_tv.setText(subtitle);
            subtitle_tv.setVisibility(VISIBLE);
        }
    }

    public void setProfilePhotoVisibility(boolean isVisible) {
        ShapeableImageView imageView = findViewById(R.id.ab_photo);
        imageView.setVisibility(isVisible ? VISIBLE : GONE);
    }

    public ShapeableImageView getProfilePhotoView() {
        return findViewById(R.id.ab_photo);
    }
}
