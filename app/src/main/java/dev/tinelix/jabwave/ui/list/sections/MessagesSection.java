package dev.tinelix.jabwave.ui.list.sections;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.activities.MessengerActivity;
import dev.tinelix.jabwave.net.base.api.entities.Chat;
import dev.tinelix.jabwave.net.base.api.entities.Message;
import dev.tinelix.jabwave.net.base.api.models.ChatGroup;
import dev.tinelix.jabwave.net.base.api.models.MessageDateGroup;
import dev.tinelix.jabwave.net.xmpp.api.entities.Contact;
import dev.tinelix.jabwave.ui.list.adapters.MessagesAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class MessagesSection extends Section {
    private final MessagesAdapter adapter;
    private final Context ctx;
    private final ArrayList<Message> messages;
    private MessageDateGroup header;
    private boolean isOpen = true;

    public MessagesSection(Context ctx, MessageDateGroup header, ArrayList<Message> messages, MessagesAdapter adapter) {
        super(SectionParameters.builder()
                .headerResourceId(R.layout.list_item_contacts_group)
                .itemResourceId(R.layout.list_item_contacts)
                .build());
        this.ctx = ctx;
        this.messages = messages;
        this.adapter = adapter;
        this.header = header;
    }

    @Override
    public int getContentItemsTotal() {
        return isOpen ? messages.size() : 0;
    }


    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new EntityViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new EntityGroupViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((EntityViewHolder) holder).bind(position);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        ((EntityGroupViewHolder) holder).bind();
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

    class EntityViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final ShapeableImageView contact_avatar;
        private final TextView contact_screenname;
        private final TextView contact_status;

        public EntityViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;

            contact_screenname = view.findViewById(R.id.contact_name);
            contact_status = view.findViewById(R.id.contact_status);
            contact_avatar = view.findViewById(R.id.contact_avatar);
        }

        public Resources getResources() {
            return view.getResources();
        }

        public void bind(int position) {
        }

        private void loadPhotoCache(Chat chat) {
            if(chat.network_type == 0 && chat instanceof Contact) {
                Contact contact = ((Contact) chat);
                if (contact.getVCard() != null && contact.getVCard().getAvatar() != null) {
                    Glide.with(ctx)
                            .load(contact.getVCard().getAvatar())
                            .apply(new RequestOptions()
                                    .override(400, 400)
                                    .placeholder(R.drawable.ic_person_accent)
                                    .error(R.drawable.ic_person_accent))
                            .into(contact_avatar);
                }
            } else if(chat instanceof dev.tinelix.jabwave.net.telegram.api.entities.Chat){
                if(chat.photo != null) {
                    Glide.with(ctx)
                            .load(chat.photo)
                            .apply(new RequestOptions()
                                    .override(400, 400)
                                    .placeholder(R.drawable.ic_person_accent)
                                    .error(R.drawable.ic_person_accent))
                            .into(contact_avatar);
                }
            }
        }
    }

    class EntityGroupViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final TextView groupname;
        private final TextView members_counter;

        public EntityGroupViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.groupname = view.findViewById(R.id.groupname);
            this.members_counter = view.findViewById(R.id.members_count);
        }

        public Resources getResources() {
            return view.getResources();
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        public void bind() {
        }
    }

}
