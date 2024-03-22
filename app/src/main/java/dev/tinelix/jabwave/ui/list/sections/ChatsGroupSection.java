package dev.tinelix.jabwave.ui.list.sections;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import dev.tinelix.jabwave.Global;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.api.base.entities.SuperChat;
import dev.tinelix.jabwave.core.activities.MessengerActivity;
import dev.tinelix.jabwave.api.base.entities.Chat;
import dev.tinelix.jabwave.ui.list.adapters.ChatsAdapter;
import dev.tinelix.jabwave.api.base.models.ChatGroup;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class ChatsGroupSection extends Section {
    private final ChatsAdapter adapter;
    private final Context ctx;
    private ArrayList<dev.tinelix.jabwave.api.base.entities.Chat> contacts_exp;
    private final ArrayList<dev.tinelix.jabwave.api.base.entities.Chat> chats;
    private ChatGroup header;
    private boolean isOpen = true;

    public ChatsGroupSection(Context ctx, ChatGroup header, ArrayList<dev.tinelix.jabwave.api.base.entities.Chat> chats, ChatsAdapter adapter) {
        super(SectionParameters.builder()
                .headerResourceId(R.layout.list_item_contacts_group)
                .itemResourceId(R.layout.list_item_contacts)
                .build());
        this.ctx = ctx;
        this.chats = chats;
        this.contacts_exp = new ArrayList<>();
        this.contacts_exp.addAll(chats);
        this.adapter = adapter;
        this.header = header;
    }

    @Override
    public int getContentItemsTotal() {
        return isOpen ? chats.size() : 0;
    }

    private int getOnlineCount() {
        int online_count = 0;
        for (dev.tinelix.jabwave.api.base.entities.Chat chat: chats) {
            if(chat.status > 0) {
                online_count++;
            }
        }
        return online_count;
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

    public dev.tinelix.jabwave.api.base.entities.Chat searchEntityByJid(String jid) {
        for (dev.tinelix.jabwave.api.base.entities.Chat chat: chats) {
            if(chat.id.equals(jid)) {
                return chat;
            }
        }
        return null;
    }

    public int searchEntityIndexByJid(String jid) {
        for (int i = 0; i < chats.size(); i++) {
            if(chats.get(i).id.equals(jid)) {
                return i;
            }
        }
        return -1;
    }

    public void updateEntity(int index, Chat entity) {
        chats.set(index, entity);
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
            dev.tinelix.jabwave.api.base.entities.Chat chat = contacts_exp.get(position);
            ((TextView) view.findViewById(R.id.contact_name))
                    .setText(chat.title);
            if (chat.status == 0) {
                contact_status.setVisibility(View.GONE);
            } else if(chat.status > 0) {
                contact_status.setText(
                        getResources().getStringArray(R.array.default_xmpp_statuses)[chat.status]
                );
            }

            loadPhotoCache(chat);
            view.setOnClickListener(v -> {
                Intent intent = new Intent(ctx, MessengerActivity.class);
                intent.putExtra("network_type", chat.network_type);
                if(chat.network_type == 0) {
                    intent.putExtra("chat_id", (String) chat.id);
                } else {
                    intent.putExtra("chat_id", (long) chat.id);
                }
                intent.putExtra("chat_type", chat.type);
                intent.putExtra("chat_title", chat.title);
                ctx.startActivity(intent);
            });
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        private void loadPhotoCache(dev.tinelix.jabwave.api.base.entities.Chat chat) {
            int placeholder_resid;
            chat.type = chat instanceof SuperChat ? 2 : chat.type;
            placeholder_resid = switch (chat.type) {
                case 3 -> R.drawable.ic_campaign_accent;
                case 2 -> R.drawable.ic_group_accent;
                case 1 -> R.drawable.ic_secret_chat_accent;
                default -> R.drawable.ic_person_accent;
            };
            Drawable drawable = ContextCompat.getDrawable(ctx, placeholder_resid);
            contact_avatar.setImageDrawable(drawable);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                assert drawable != null;
                drawable.setColorFilter(
                        Global.getColorAttribute(ctx, com.google.android.material.R.attr.colorAccent),
                        android.graphics.PorterDuff.Mode.SRC_IN
                );
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
            groupname.setText(header.title);
            groupname.setOnClickListener(v -> toggleGroupList());
            Drawable arrow = getResources().getDrawable(
                    isOpen ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_right
            );
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                arrow.setBounds(0, 0, 90, 90);
            } else {
                arrow.setBounds(0, 0, 60, 60);
            }
            groupname.setCompoundDrawables(arrow, null, null, null);
            if(header.withOnlineCount) {
                members_counter.setText(String.format("%s / %s", getOnlineCount(), chats.size()));
            } else if(chats.size() > 1) {
                members_counter.setText(String.format("%s", chats.size()));
            } else {
                members_counter.setVisibility(View.GONE);
            }
            //((TextView) view.findViewById(R.id.members)).setText(getSectionItemsTotal());
        }

        @SuppressLint({"UseCompatLoadingForDrawables", "NotifyDataSetChanged"})
        private void toggleGroupList() {
            isOpen = !isOpen;
            if(!isOpen) {
                contacts_exp.clear();
            } else {
                contacts_exp.addAll(chats);
            }
            Drawable arrow = getResources().getDrawable(
                    isOpen ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_right
            );
            arrow.setBounds(0, 0, 90, 90);
            groupname.setCompoundDrawables(arrow, null, null, null);
            adapter.notifyDataSetChanged();
        }
    }

}
