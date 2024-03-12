package dev.tinelix.jabwave.net.xmpp.api.models;

import android.util.Log;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.api.base.entities.ServiceEntity;
import dev.tinelix.jabwave.api.base.listeners.OnClientUpdateListener;
import dev.tinelix.jabwave.api.base.models.Chats;
import dev.tinelix.jabwave.api.base.models.NetworkService;
import dev.tinelix.jabwave.core.services.XMPPService;
import dev.tinelix.jabwave.net.xmpp.api.XMPPClient;
import dev.tinelix.jabwave.net.xmpp.api.entities.Chat;
import dev.tinelix.jabwave.api.base.models.ChatGroup;

public class Roster extends Chats {

    private final XMPPConnection conn;
    private final org.jivesoftware.smack.roster.Roster roster;
    private final OnClientUpdateListener listener;
    private ArrayList<dev.tinelix.jabwave.api.base.entities.Chat> chats;
    private XMPPService service;

    public Roster(XMPPService service, XMPPClient client, OnClientUpdateListener listener) {
        super(client);
        this.conn = client.getConnection();
        this.roster = org.jivesoftware.smack.roster.Roster.getInstanceFor(conn);
        this.listener = listener;
        this.service = service;
        try { Thread.sleep(2000); } catch (InterruptedException ignored) { }
        roster.addRosterListener(new RosterListener() {
            @Override
            public void entriesAdded(Collection<Jid> addresses) {

            }

            @Override
            public void entriesUpdated(Collection<Jid> addresses) {

            }

            @Override
            public void entriesDeleted(Collection<Jid> addresses) {

            }

            public void presenceChanged(Presence presence) {
                if(chats != null) {
                    Log.d(JabwaveApp.XMPP_SERV_TAG, String.format("Updated presence from %s", presence.getFrom()));
                    Chat chat = (Chat) getChatById(presence.getFrom().toString().split("/")[0]);
                    Presences presences = new Presences(chat, new ArrayList<>());
                    chat.status = presences.getStatusEnum(presence);
                    chats.set(getChatIndex(chat), chat);
                    listener.onUpdate(new HashMap<>());
                }
            }
        });
    }

    @Override
    public ArrayList<dev.tinelix.jabwave.api.base.entities.Chat> getList() {
        chats = new ArrayList<>();
        Collection <RosterEntry> entries = roster.getEntries();
        Collection<RosterGroup> groups = roster.getGroups();
        for (RosterEntry entry : entries) {
            Chat entity = new Chat("");
            entity.jid = entry.getJid().toString();
            List<Presence> presences = roster.getAllPresences(entry.getJid());
            String custom_status = "";
            int status = 0;
            int type;
            boolean isMuc = isMuc(entry.getJid().asEntityBareJidOrThrow());
            type = isMuc ? 2 : 0;
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
                    entity = new Chat(
                            entry.getJid().toString(),
                            entry.getName(),
                            type,
                            new ArrayList<>(),
                            custom_status,
                            status
                    );
                else
                    entity = new Chat(
                            entry.getJid().toString(),
                            entry.getName(),
                            type,
                            new ArrayList<>(),
                            status
                    );
            } else {
                if(custom_status != null && custom_status.length() > 0)
                    entity = new Chat(
                            entry.getJid().toString(),
                            entry.getJid().toString(),
                            new ArrayList<>(),
                            custom_status,
                            status
                    );
                else
                    entity = new Chat(
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
            Chat finalEntity = entity;
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
            chats.add(entity);
        }
        return chats;
    }

    public boolean isMuc(@NonNull final EntityBareJid mucId) {
        Services netServices;
        if(service.getNetworkServices() == null)
            netServices = new Services(client);
        else
            netServices = (Services) service.getNetworkServices();
        if(netServices.getServices() == null || netServices.getServices().size() == 0)
            netServices.discoverServices();
        ArrayList<NetworkService> netServicesList = netServices.getServices();
        for (NetworkService service : netServicesList) {
            boolean equalDomain = service.id.equals(mucId.toString().split("@")[1]);
            boolean isSupportedMuc = service.isConference();
            if(equalDomain && isSupportedMuc) {
                ServiceEntity entity = service.getEntityInfo(mucId.toString());
                boolean isMuc = entity.type == 1;
                return isMuc;
            }
        }
        return false;
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
    public dev.tinelix.jabwave.api.base.entities.Chat getChatById(Object id) {
        for (dev.tinelix.jabwave.api.base.entities.Chat chat : chats) {
            if(chat.id.equals(id)) {
                return chat;
            }
        }
        return null;
    }

    @Override
    public int getChatIndex(dev.tinelix.jabwave.api.base.entities.Chat chat) {
        for(int i = 0; i < chats.size(); i++) {
            if(chats.get(i).equals(chat)) {
                return i;
            }
        }
        return -1;
    }
}
