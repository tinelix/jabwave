package dev.tinelix.jabwave.ui.list.adapters;

import android.annotation.SuppressLint;

import java.util.ArrayList;

import dev.tinelix.jabwave.net.base.api.entities.Message;
import dev.tinelix.jabwave.net.xmpp.api.entities.Contact;
import dev.tinelix.jabwave.ui.list.sections.ChatsGroupSection;
import dev.tinelix.jabwave.ui.list.sections.MessagesSection;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class MessagesAdapter extends SectionedRecyclerViewAdapter {
    private ArrayList<Message> all_messages;

    public Message searchByText(String text, int begin) {
        if(all_messages == null)
            all_messages = new ArrayList<>();
        for(int i = 0; i < getSectionCount(); i++) {
            ArrayList<Message> messages;
            if(getSection(i) instanceof MessagesSection) {
                MessagesSection section = (MessagesSection) getSection(i);
                messages = section.searchMessagesByText(text);
                all_messages.addAll(messages);
            }
        }
        if(all_messages.size() > begin) {
            return all_messages.get(begin);
        } else {
            throw new ArrayIndexOutOfBoundsException("Offset is higher than the length of the array");
        }
    }

    public Message searchByObject(Message message) {
        for(int i = 0; i < getSectionCount(); i++) {
            Message _message;
            if(getSection(i) instanceof MessagesSection) {
                MessagesSection section = (MessagesSection) getSection(i);
                return section.getMessage(section.searchMessageById(message.id));
            }
        }
        return null;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setByText(String text, Message message) {
        int message_index;
        for(int i = 0; i < getSectionCount(); i++) {
            if(getSection(i) instanceof MessagesSection) {
                MessagesSection section = (MessagesSection) getSection(i);
                message_index = section.searchMessageById(message.id);
                if(message_index >= 0)
                    section.setMessage(message_index, message);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getSectionItemViewType(int position) {
        return position;
    }
}
