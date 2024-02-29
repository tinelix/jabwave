package dev.tinelix.jabwave.core.list.sections;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.list.adapters.ContactsAdapter;
import dev.tinelix.jabwave.xmpp.api.entities.Contact;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class ContactsGroupSection extends Section {
    private final ContactsAdapter adapter;
    private ArrayList<Contact> contacts_exp;
    private final ArrayList<Contact> contacts;
    private Contact header;
    private boolean isOpen = true;

    public ContactsGroupSection(Contact header, ArrayList<Contact> contacts, ContactsAdapter adapter) {
        super(SectionParameters.builder()
                .headerResourceId(R.layout.list_item_contacts_group)
                .itemResourceId(R.layout.list_item_contacts)
                .build());
        this.contacts = contacts;
        this.contacts_exp = new ArrayList<>();
        this.contacts_exp.addAll(contacts);
        this.adapter = adapter;
        this.header = header;
    }

    @Override
    public int getContentItemsTotal() {
        return isOpen ? contacts.size() : 0;
    }

    private int getOnlineCount() {
        int online_count = 0;
        for (Contact contact: contacts) {
            if(contact.status > 0) {
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

    public Contact searchEntityByJid(String jid) {
        for (Contact contact: contacts) {
            if(contact.jid.equals(jid)) {
                return contact;
            }
        }
        return null;
    }

    public int searchEntityIndexByJid(String jid) {
        for (int i = 0; i < contacts.size(); i++) {
            if(contacts.get(i).jid.equals(jid)) {
                return i;
            }
        }
        return -1;
    }

    public void updateEntity(int index, Contact entity) {
        contacts.set(index, entity);
    }

    class EntityViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final TextView roster_screenname;
        private final TextView roster_status;

        public EntityViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            roster_screenname = view.findViewById(R.id.roster_screenname);
            roster_status = view.findViewById(R.id.roster_status);
        }

        public Resources getResources() {
            return view.getResources();
        }

        public void bind(int position) {
            Contact contact = contacts_exp.get(position);
            ((TextView) view.findViewById(R.id.roster_screenname))
                    .setText(contact.title);
            if (contact.status == 0) {
                roster_status.setVisibility(View.GONE);
            } else if(contact.status > 0) {
                roster_status.setText(
                        getResources().getStringArray(R.array.default_xmpp_statuses)[contact.status]
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
            groupname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleGroupList();
                }
            });
            Drawable arrow = getResources().getDrawable(
                    isOpen ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_right
            );
            arrow.setBounds(0, 0, 90, 90);
            groupname.setCompoundDrawables(arrow, null, null, null);
            members_counter.setText(String.format("%s / %s", getOnlineCount(), contacts.size()));
            //((TextView) view.findViewById(R.id.members)).setText(getSectionItemsTotal());
        }

        @SuppressLint({"UseCompatLoadingForDrawables", "NotifyDataSetChanged"})
        private void toggleGroupList() {
            isOpen = !isOpen;
            if(!isOpen) {
                contacts_exp.clear();
            } else {
                contacts_exp.addAll(contacts);
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
