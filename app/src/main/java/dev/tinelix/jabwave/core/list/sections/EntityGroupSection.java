package dev.tinelix.jabwave.core.list.sections;

import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.xmpp.api.entities.Contact;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class EntityGroupSection extends Section {
    private ArrayList<Contact> entities;
    private Contact header;

    public EntityGroupSection(Contact header, ArrayList<Contact> entities) {
        super(SectionParameters.builder()
                .headerResourceId(R.layout.maingroup_row)
                .itemResourceId(R.layout.mainchild_row)
                .build());
        this.entities = entities;
        this.header = header;
    }

    @Override
    public int getContentItemsTotal() {
        return entities.size();
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
        for (Contact entity: entities) {
            if(entity.jid.equals(jid)) {
                return entity;
            }
        }
        return null;
    }

    public int searchEntityIndexByJid(String jid) {
        for (int i = 0; i < entities.size(); i++) {
            if(entities.get(i).jid.equals(jid)) {
                return i;
            }
        }
        return -1;
    }

    public void updateEntity(int index, Contact entity) {
        entities.set(index, entity);
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
            Contact entity = entities.get(position);
            ((TextView) view.findViewById(R.id.roster_screenname))
                    .setText(entity.title);
            if(entity.custom_status == null || entity.custom_status.length() == 0) {
                if (entity.status == 0) {
                    roster_status.setVisibility(View.GONE);
                } else if (entity.status > 0) {
                    roster_status.setText(
                            getResources().getStringArray(R.array.default_xmpp_statuses)[entity.status]
                    );
                }
            } else {
                roster_status.setText(entity.custom_status);
            }
        }
    }

    class EntityGroupViewHolder extends RecyclerView.ViewHolder {
        private final View view;

        public EntityGroupViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
        }

        public void bind() {
            ((TextView) view.findViewById(R.id.groupname))
                    .setText(header.title);
            //((TextView) view.findViewById(R.id.members)).setText(getSectionItemsTotal());
        }
    }
}
