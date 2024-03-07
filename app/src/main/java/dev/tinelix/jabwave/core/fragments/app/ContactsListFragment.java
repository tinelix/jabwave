package dev.tinelix.jabwave.core.fragments.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dev.tinelix.jabwave.Global;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.activities.AppActivity;
import dev.tinelix.jabwave.core.services.base.ClientService;
import dev.tinelix.jabwave.net.base.api.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.net.base.api.models.Chats;
import dev.tinelix.jabwave.ui.list.adapters.ChatsAdapter;
import dev.tinelix.jabwave.net.base.api.entities.Chat;
import dev.tinelix.jabwave.net.base.api.models.ChatGroup;
import dev.tinelix.jabwave.ui.list.sections.ChatsGroupSection;
import dev.tinelix.jabwave.net.telegram.api.TDLibClient;
import dev.tinelix.jabwave.ui.enums.HandlerMessages;

public class ContactsListFragment extends Fragment {
    private JabwaveApp app;
    private ArrayList<ChatGroup> groups;
    private ArrayList<Chat> contacts;
    public ChatsAdapter chatsAdapter;
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
        if(getActivity() instanceof AppActivity) {
            AppActivity activity = (AppActivity) getActivity();
            Chats chats = activity.service.getChats();
            if (activity.service.isAsyncAPIs()) {
                chats.loadChats(new OnClientAPIResultListener() {
                    @Override
                    public boolean onSuccess(HashMap<String, Object> map) {
                        contacts = activity.service.getChats().getList();
                        groups = activity.service.getChats().getGroupsList();
                        Global.triggerReceiverIntent(Objects.requireNonNull(getActivity()), HandlerMessages.CHATS_LOADED);
                        return true;
                    }

                    @Override
                    public boolean onFail(HashMap<String, Object> map, Throwable t) {
                        return false;
                    }
                });
            } else {
                // not yet optimized
                contacts = chats.getList();
                groups = chats.getGroupsList();
                createContactsAdapter(activity.service);
            }
        }
    }

    public void loadLocalContacts() {
        if(getActivity() instanceof AppActivity) {
            AppActivity activity = (AppActivity) getActivity();
            contacts = activity.service.getChats().getList();
            if (app.getCurrentNetworkType().equals("telegram")) {
                groups = new ArrayList<>();
            } else {
                groups = activity.service.getChats().getGroupsList();
            }
            createContactsAdapter(activity.service);
        }
    }

    private void createContactsAdapter(ClientService service) {
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
            ChatGroup group;
            if(app.getCurrentNetworkType().equals("telegram")) {
                ChatGroup account_group = new ChatGroup(getResources().getString(R.string.saved_messages), 1);
                if(contacts.size() > 0) {
                    if(contacts.get(0).id.equals(service.getAccount().id)) {
                        ArrayList<Chat> account_only = new ArrayList<>();
                        Chat chat = contacts.get(0);
                        account_only.add(chat);
                        contacts.remove(chat);
                        ChatsGroupSection accountGroupSection =
                                new ChatsGroupSection(getActivity(), account_group, account_only, chatsAdapter);
                        chatsAdapter.addSection(accountGroupSection);
                    }
                }
                group = new ChatGroup(getResources().getString(R.string.general_category), 1);
            } else {
                group = new ChatGroup(getResources().getString(R.string.general_category), 0);
            }
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
