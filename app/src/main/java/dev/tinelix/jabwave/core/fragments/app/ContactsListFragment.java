package dev.tinelix.jabwave.core.fragments.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dev.tinelix.jabwave.Global;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.ui.list.adapters.ChatsAdapter;
import dev.tinelix.jabwave.ui.list.items.base.Chat;
import dev.tinelix.jabwave.ui.list.items.base.ChatGroup;
import dev.tinelix.jabwave.ui.list.sections.ChatsGroupSection;
import dev.tinelix.jabwave.net.telegram.api.TDLibClient;
import dev.tinelix.jabwave.net.telegram.api.models.Chats;
import dev.tinelix.jabwave.ui.enums.HandlerMessages;

public class ContactsListFragment extends Fragment {
    private JabwaveApp app;
    private ArrayList<ChatGroup> groups;
    private ArrayList<Chat> contacts;
    private ChatsAdapter chatsAdapter;
    private LinearLayoutManager llm;
    private View view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = ((JabwaveApp) Objects.requireNonNull(getContext()).getApplicationContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_contacts, null);
        return view;
    }

    public void loadContacts() {
        if(app.getCurrentNetworkType().equals("telegram")) {
            app.telegram.chats = new Chats(app.telegram.getClient());
            app.telegram.chats.loadChats(new TDLibClient.ApiHandler() {
                @Override
                public void onSuccess(TdApi.Function function, TdApi.Object object) {
                    contacts = app.telegram.chats.chats;
                    groups = new ArrayList<>();
                    Global.triggerReceiverIntent(getActivity(), HandlerMessages.CHATS_LOADED);
                }

                @Override
                public void onFail(TdApi.Function function, Throwable throwable) {

                }
            });
        } else {
            contacts = app.xmpp.getRoster().getContacts();
            groups = app.xmpp.getRoster().getGroups();
        }
    }

    public void loadLocalContacts() {
        contacts = app.telegram.chats.chats;
        groups = new ArrayList<>();
        createContactsAdapter();
    }

    private void createContactsAdapter() {
        chatsAdapter = new ChatsAdapter();
        if(groups == null) {
            groups = new ArrayList<>();
        }
        ChatsGroupSection entityGroupSection;
        if(groups.size() > 0) {
            for (ChatGroup group : groups) {
                ArrayList<Chat> groupChats = new ArrayList<>();
                for (Chat chat : contacts) {
                    if (chat.groups.contains(group.title)) {
                        groupChats.add(chat);
                    }
                }
                entityGroupSection = new ChatsGroupSection(getActivity(), group, groupChats, chatsAdapter);
                chatsAdapter.addSection(entityGroupSection);
            }
        } else {
            ChatGroup group = new ChatGroup(getResources().getString(R.string.general_category), 1);
            entityGroupSection = new ChatsGroupSection(getActivity(), group, contacts, chatsAdapter);
            chatsAdapter.addSection(entityGroupSection);
        }
        llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        RecyclerView contactsView = view.findViewById(R.id.entityview);
        contactsView.setLayoutManager(llm);
        contactsView.setAdapter(chatsAdapter);
        contactsView.setVisibility(View.VISIBLE);
        Objects.requireNonNull(getActivity()).findViewById(R.id.progress).setVisibility(View.GONE);
    }
}
