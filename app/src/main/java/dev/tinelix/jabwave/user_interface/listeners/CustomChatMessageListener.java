package dev.tinelix.jabwave.user_interface.listeners;

import android.util.Log;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

import dev.tinelix.jabwave.user_interface.activities.AppActivity;

/*
* MyChatMessageListener directs the incoming messages to the appropriate container.
* In this case, messages are contained in the ChatList
* */
public class CustomChatMessageListener implements ChatMessageListener {

    protected static final String TAG = "CCML";

    @SuppressWarnings("deprecation")
    @Override
    public void processMessage(Chat chat, Message message) {
        final String mChatSender = String.valueOf(message.getFrom());
        final String mChatMessage = message.getBody();

        Log.e(TAG, mChatSender + ": " + mChatMessage);

        AppActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //AppActivity.getInstance().updateChatList(message.getFrom(), message.getBody());
            }
        });
    }
}
