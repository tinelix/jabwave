package dev.tinelix.jabwave.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

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
        view.getLayoutParams().width = LayoutParams.MATCH_PARENT;
        view.getLayoutParams().height = LayoutParams.MATCH_PARENT;
    }

    public MessageEditor(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_message_editor, null);
        this.addView(view);
        view.getLayoutParams().width = LayoutParams.MATCH_PARENT;
        view.getLayoutParams().height = LayoutParams.MATCH_PARENT;
    }

    public MessageEditor(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_message_editor, null);
        this.addView(view);
        view.getLayoutParams().width = LayoutParams.MATCH_PARENT;
        view.getLayoutParams().height = LayoutParams.MATCH_PARENT;
    }

    public void setSendButtonListener(OnClickListener onClickListener) {
        findViewById(R.id.send_button).setOnClickListener(onClickListener);
    }

    public EditText getEditorArea() {
        return findViewById(R.id.message_edit);
    }
}
