package dev.tinelix.jabwave.core.ui.fragments.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.list.adapters.ContactsAdapter;
import dev.tinelix.jabwave.core.list.sections.ContactsGroupSection;
import dev.tinelix.jabwave.xmpp.api.entities.Contact;

public class ContactsListFragment extends Fragment {
    private JabwaveApp app;
    private ArrayList<Contact> groups;
    private ArrayList<Contact> contacts;
    private ContactsAdapter contactsAdapter;
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
        contacts = app.xmpp.getContacts();
        groups = app.xmpp.getChatGroups();
        createContactsAdapter();
    }

    private void createContactsAdapter() {
        contactsAdapter = new ContactsAdapter();
        ContactsGroupSection entityGroupSection;
        if(groups.size() > 0) {
            for (Contact group : groups) {
                ArrayList<Contact> groupContacts = new ArrayList<>();
                for (Contact contact : contacts) {
                    if (contact.groups.contains(group.title)) {
                        groupContacts.add(contact);
                    }
                }
                entityGroupSection = new ContactsGroupSection(getActivity(), group, groupContacts, contactsAdapter);
                contactsAdapter.addSection(entityGroupSection);
            }
        } else {
            Contact group = new Contact(getResources().getString(R.string.general_category));
            entityGroupSection = new ContactsGroupSection(getActivity(), group, contacts, contactsAdapter);
            contactsAdapter.addSection(entityGroupSection);
        }

        llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        RecyclerView contactsView = view.findViewById(R.id.entityview);
        contactsView.setLayoutManager(llm);
        contactsView.setAdapter(contactsAdapter);
        contactsView.setVisibility(View.VISIBLE);
        Objects.requireNonNull(getActivity()).findViewById(R.id.progress).setVisibility(View.GONE);
    }
}
