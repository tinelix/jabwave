package dev.tinelix.jabwave.net.base.api.entities;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import dev.tinelix.jabwave.net.base.api.attachments.Attachment;

public class Message {
    public long id;
    public Object member_id;
    public Object chat_id;
    public String text;
    private boolean isIncoming;
    private Date timestamp;
    private boolean isHeader;
    private ChatSender sender;
    protected ArrayList<Attachment> attachments;

    public Message(long id, Object chat_id, Object member_id, String text, Date timestamp, boolean isIncoming) {
        this.id = id;
        this.chat_id = chat_id;
        this.member_id = member_id;
        this.timestamp = timestamp;
        this.text = text;
        this.isIncoming = isIncoming;
        attachments = new ArrayList<>();
    }

    public Message(boolean isHeader, String header_title) {
        this.isHeader = isHeader;
        this.text = header_title;
    }

    @SuppressLint("SimpleDateFormat")
    public String formatTimestamp() {
        Date timestamp_1 = new Date(timestamp.getTime() * 1000);
        return new SimpleDateFormat("HH:mm").format(timestamp_1);
    }

    @SuppressLint("SimpleDateFormat")
    public String formatTimestamp(String pattern) {
        Date timestamp_1 = new Date(timestamp.getTime() * 1000);
        return new SimpleDateFormat(pattern).format(timestamp_1);
    }

    public boolean isIncoming() {
        return isIncoming;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public ChatSender getSender() {
        return sender;
    }

    public void setSender(ChatSender sender) {
        this.sender = sender;
    }

    public ArrayList<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(ArrayList<Attachment> attachments) {
        this.attachments = attachments;
    }

    public Attachment searchAttachment(int file_id) {
        for(Attachment attachment : attachments) {
            if(attachment.id == file_id) {
                return attachment;
            }
        }
        return null;
    }

    public int getAttachmentIndex(int file_id) {
        for(int i = 0; i < attachments.size(); i++) {
            Attachment attachment = attachments.get(i);
            if(attachment.id == file_id) {
                return i;
            }
        }
        return -1;
    }
}
