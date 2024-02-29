package dev.tinelix.jabwave.xmpp.api.entities;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.EntityBareJid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.tinelix.jabwave.xmpp.api.models.Presences;

public class Roster {

    private final XMPPConnection conn;
    private final org.jivesoftware.smack.roster.Roster roster;

    public Roster(XMPPConnection conn) {
        this.roster = org.jivesoftware.smack.roster.Roster.getInstanceFor(conn);
        this.conn = conn;
        try { Thread.sleep(2000); } catch (InterruptedException ignored) { }
    }

    public ArrayList<Contact> getContacts() {
        ArrayList<Contact> contacts = new ArrayList<>();
        Collection<RosterEntry> entries = roster.getEntries();
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
            try {
                entity.setVCard(
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
            contacts.add(entity);
        }

        return contacts;
    }

    public ArrayList<Contact> getGroups() {
        ArrayList<Contact> groups_list = new ArrayList<>();
        Collection<RosterGroup> groups = roster.getGroups();
        for (RosterGroup group: groups) {
            groups_list.add(new ContactsGroup(group.getName()));
        }
        return groups_list;
    }
}
