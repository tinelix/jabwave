package dev.tinelix.jabwave.ui.list.sections;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.helper.widget.Flow;
import androidx.recyclerview.widget.RecyclerView;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.net.base.api.entities.Chat;
import dev.tinelix.jabwave.net.base.api.entities.Message;
import dev.tinelix.jabwave.ui.list.adapters.MessagesAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class MessagesSection extends Section {
    private final MessagesAdapter adapter;
    private final Context ctx;
    private final ArrayList<Message> messages;
    private boolean isOpen = true;

    public MessagesSection(Context ctx, ArrayList<Message> messages, MessagesAdapter adapter) {
        super(SectionParameters.builder()
                .headerResourceId(R.layout.list_item_message_date)
                .itemResourceId(R.layout.list_item_message)
                .build());
        this.ctx = ctx;
        this.messages = messages;
        this.adapter = adapter;
    }

    @Override
    public int getContentItemsTotal() {
        return isOpen ? messages.size() : 0;
    }


    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        MessageViewHolder holder = new MessageViewHolder(view);
        holder.setIsRecyclable(false);
        return holder;
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new MessageGroupViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((MessageViewHolder) holder).bind(position);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        ((MessageGroupViewHolder) holder).bind();
    }

    public ArrayList<Message> searchMessagesByText(String text) {
        ArrayList<Message> searchResults = new ArrayList<>();
        for (Message message: messages) {
            if(message.text.contains(text)) {
                searchResults.add(message);
            }
        }
        return searchResults;
    }

    public int searchMessageById(long id) {
        for (int i = 0; i < messages.size(); i++) {
            if(messages.get(i).id == id) {
                return i;
            }
        }
        return -1;
    }

    public void updateMessage(int index, Message message) {
        messages.set(index, message);
    }

    public Message getMessage(int position) {
        return messages.get(position);
    }

    public void setMessage(int position, Message message) {
        messages.set(position, message);
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final CardView msg_card;
        private final TextView msg_text;
        private final TextView msg_timestamp;
        private final Flow flow_layout;

        public MessageViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;

            msg_text = view.findViewById(R.id.msg_text);
            flow_layout = view.findViewById(R.id.flow_layout);
            msg_timestamp = view.findViewById(R.id.msg_timestamp);
            msg_card = view.findViewById(R.id.msg_card);
        }

        public Resources getResources() {
            return view.getResources();
        }

        public void bind(int position) {
            Message msg = messages.get(position);
            msg_text.setText(msg.text);
            msg_timestamp.setText(msg.formatTimestamp());
            if(!msg.isIncoming()) {
                ((FrameLayout.LayoutParams) msg_card.getLayoutParams()).gravity = Gravity.RIGHT;
                msg_text.setTextColor(ctx.getResources().getColor(R.color.outMessageTextColor));
            } else {
                msg_card.setCardBackgroundColor(ctx.getResources().getColor(R.color.inMessageColor));
                msg_text.setTextColor(ctx.getResources().getColor(R.color.inMessageTextColor));
                msg_timestamp.setTextColor(ctx.getResources().getColor(R.color.inMsgTimestampColor));
            }
        }

        private void loadPhotoCache(Chat chat) {

        }
    }

    class MessageGroupViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final TextView date_header;

        public MessageGroupViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.date_header = view.findViewById(R.id.msg_date);
        }

        public Resources getResources() {
            return view.getResources();
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        public void bind() {
            if(messages.size() > 0) {
                date_header.setText(messages.get(0).formatTimestamp("d MMMM yyyy"));
            } else {
                date_header.setText(getResources().getString(R.string.no_messages));
            }
        }
    }

}
