package dev.tinelix.jabwave.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import dev.tinelix.jabwave.R;

public class MessageEditor extends LinearLayoutCompat {

    public MessageEditor(@NonNull Context context) {
        super(context);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_message_editor, null);

        this.addView(view);
    }

    public MessageEditor(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_message_editor, null);

        this.addView(view);
    }

    public MessageEditor(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_message_editor, null);

        this.addView(view);
    }
}
