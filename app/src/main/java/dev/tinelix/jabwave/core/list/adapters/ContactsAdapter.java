package dev.tinelix.jabwave.core.list.adapters;

import android.annotation.SuppressLint;

import dev.tinelix.jabwave.xmpp.api.entities.Contact;
import dev.tinelix.jabwave.core.list.sections.ContactsGroupSection;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class ContactsAdapter extends SectionedRecyclerViewAdapter {
    public Contact searchByJid(String jid) {
        Contact entity = null;
        for(int i = 0; i < getSectionCount(); i++) {
            if(getSection(i) instanceof ContactsGroupSection) {
                ContactsGroupSection section = (ContactsGroupSection) getSection(i);
                entity = section.searchEntityByJid(jid);
                if(entity != null) {
                    return entity;
                }
            }
        }
        return null;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setByJid(String jid, Contact entity) {
        int entity_index;
        for(int i = 0; i < getSectionCount(); i++) {
            if(getSection(i) instanceof ContactsGroupSection) {
                ContactsGroupSection section = (ContactsGroupSection) getSection(i);
                entity_index = section.searchEntityIndexByJid(jid);
                if(entity_index >= 0)
                    section.updateEntity(entity_index, entity);
            }
        }
        notifyDataSetChanged();
    }
}
