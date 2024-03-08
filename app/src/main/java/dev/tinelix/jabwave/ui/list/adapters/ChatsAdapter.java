package dev.tinelix.jabwave.ui.list.adapters;

import android.annotation.SuppressLint;

import dev.tinelix.jabwave.net.xmpp.api.entities.Chat;
import dev.tinelix.jabwave.ui.list.sections.ChatsGroupSection;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class ChatsAdapter extends SectionedRecyclerViewAdapter {
    public Chat searchByJid(String jid) {
        Chat entity = null;
        for(int i = 0; i < getSectionCount(); i++) {
            if(getSection(i) instanceof ChatsGroupSection) {
                ChatsGroupSection section = (ChatsGroupSection) getSection(i);
                entity = (Chat) section.searchEntityByJid(jid);
                if(entity != null) {
                    return entity;
                }
            }
        }
        return null;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setByJid(String jid, Chat entity) {
        int entity_index;
        for(int i = 0; i < getSectionCount(); i++) {
            if(getSection(i) instanceof ChatsGroupSection) {
                ChatsGroupSection section = (ChatsGroupSection) getSection(i);
                entity_index = section.searchEntityIndexByJid(jid);
                if(entity_index >= 0)
                    section.updateEntity(entity_index, entity);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getSectionItemViewType(int position) {
        return position;
    }

}
