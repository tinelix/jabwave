package dev.tinelix.jabwave.net.xmpp.api.models;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jxmpp.jid.EntityBareJid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.tinelix.jabwave.net.base.api.models.Chats;
import dev.tinelix.jabwave.net.xmpp.api.XMPPClient;
import dev.tinelix.jabwave.net.xmpp.api.entities.Contact;
import dev.tinelix.jabwave.net.base.api.entities.Chat;
import dev.tinelix.jabwave.net.base.api.models.ChatGroup;

public class Roster extends Chats {

    private final XMPPConnection conn;
    private final org.jivesoftware.smack.roster.Roster roster;
    private ArrayList<Chat> contacts;

    public Roster(XMPPClient client) {
        super(client);
        this.conn = client.getConnection();
        this.roster = org.jivesoftware.smack.roster.Roster.getInstanceFor(conn);
        try { Thread.sleep(2000); } catch (InterruptedException ignored) { }
    }

    @Override
    public ArrayList<Chat> getList() {
        contacts = new ArrayList<>();
        Collection <RosterEntry> entries = roster.getEntries();
        Collection<RosterGroup> groups = roster.getGroups();
        for (RosterEntry entry : entries) {
            Contact entity = new Contact("");
            entity.jid = entry.getJid().toString();
            List<Presence> presences = roster.getAllPresences(entry.getJid());
            String custom_status = "";
            int status = 0;
            if(presences.size() > 0) {
                Presences presencesModel = new Presences(entity, presences);
                Presence hpPresence = presencesModel.getHighestPriorityPresence();
                if(hpPresence != null) {
                    custom_status = hpPresence.getStatus();
                    status = presencesModel.getStatusEnum(hpPresence);
                }
            }
            if(entry.getName() != null) {
                if(custom_status != null && custom_status.length() > 0)
                    entity = new Contact(
                            entry.getName(),
                            entry.getJid().toString(),
                            new ArrayList<>(),
                            custom_status,
                            status
                    );
                else
                    entity = new Contact(
                            entry.getName(),
                            entry.getJid().toString(),
                            new ArrayList<>(),
                            status
                    );
            } else {
                if(custom_status != null && custom_status.length() > 0)
                    entity = new Contact(
                            entry.getJid().toString(),
                            entry.getJid().toString(),
                            new ArrayList<>(),
                            custom_status,
                            status
                    );
                else
                    entity = new Contact(
                            entry.getJid().toString(),
                            entry.getJid().toString(),
                            new ArrayList<>(),
                            status
                    );
            }

            for (RosterGroup group : groups) {
                if(entry.getGroups().contains(group)) {
                    entity.groups.add(group.getName());
                }
            }
            Contact finalEntity = entity;
            new Thread(() -> {
                // Non-async loadVCard function blocking UI thread.
                try {
                    finalEntity.setVCard(
                            VCardManager
                                    .getInstanceFor(conn)
                                    .loadVCard((EntityBareJid) entry.getJid().asBareJid())
                    );
                } catch (SmackException.NoResponseException |
                        XMPPException.XMPPErrorException |
                        SmackException.NotConnectedException |
                        InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            contacts.add(entity);
        }

        return contacts;
    }

    @Override
    public ArrayList<ChatGroup> getGroupsList() {
        ArrayList<ChatGroup> groups_list = new ArrayList<>();
        Collection<RosterGroup> groups = roster.getGroups();
        for (RosterGroup group: groups) {
            groups_list.add(new ChatGroup(group.getName(), true, 0));
        }
        return groups_list;
    }

    @Override
    public Chat getChatById(Object id) {
        for (Chat chat : contacts) {
            if(chat.id.equals(id)) {
                return chat;
            }
        }
        return null;
    }
}
