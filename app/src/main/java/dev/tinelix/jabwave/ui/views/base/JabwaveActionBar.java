package dev.tinelix.jabwave.ui.views.base;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.ColorRes;
import androidx.annotation.IdRes;
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

    public void setNavigationIconTint(@ColorRes int color_res) {
        if(getNavigationIcon() != null) {
            // Setting correct tint for navigation icon
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getNavigationIcon()
                        .setTint(getResources().getColor(color_res));
            } else {
                getNavigationIcon()
                        .setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            }
        }
    }
}
