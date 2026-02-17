package dev.tinelix.jabwave.api.xmpp.models;

import org.jivesoftware.smack.packet.Presence;

import java.util.List;

import dev.tinelix.jabwave.api.xmpp.entities.Chat;

public class Presences {
    public List<Presence> presences;
    private Chat chat;

    public Presences(Chat chat, List<Presence> presences) {
        this.chat = chat;
        this.presences = presences;
    }

    public Presence getHighestPriorityPresence() {
        int highest_priority = -127;
        for (Presence presence: presences) {
            if(presence.getPriority() >= highest_priority) {
                return presence;
            }
        }
        return null;
    }

    public int getStatusEnum(Presence presence) {
        if(presence.getType() == Presence.Type.available) {
            switch (presence.getMode()) {
                case away:          // Away
                    return 2;
                case xa:            // Extended Away (Not available)
                    return 3;
                case dnd:           // Do not disturb
                    return 4;
                default:            // Default value (also for 'chat')
                    return 1;
            }
        } else {
            return 0;               // Offline
        }
    }
}
