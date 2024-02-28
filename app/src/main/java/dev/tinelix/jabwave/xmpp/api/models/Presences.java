package dev.tinelix.jabwave.xmpp.api.models;

import android.util.Log;

import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;
import java.util.List;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.xmpp.api.entities.Contact;

public class Presences {
    public List<Presence> presences;
    private Contact contact;

    public Presences(Contact contact, List<Presence> presences) {
        this.contact = contact;
        this.presences = presences;
    }

    public Presence getHighestPriorityPresence() {
        int highest_priority = -127;
        for (Presence presence: presences) {
            if(presence.getType() == Presence.Type.unavailable &&
                    presence.getPriority() >= highest_priority) {
                return presence;
            }
        }
        return null;
    }

    public int getStatusEnum(Presence presence) {
        Log.d(JabwaveApp.XMPP_SERV_TAG,
                String.format(
                        "JID: %s | Subscription Type: %s",
                        contact.jid,
                        presence.getType().toString()
                )
        );
        switch (presence.getMode()) {
            case available:     // Available
                return 1;
            case away:          // Away
                return 2;
            case xa:            // Extended Away (Not available)
                return 3;
            case dnd:           // Do not disturb
                return 4;
            default:            // Default value (also for 'chat')
                return 0;
        }
    }
}
