package dev.tinelix.jabwave.core.fragments.app;

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
import dev.tinelix.jabwave.core.activities.base.AppActivity;
import dev.tinelix.jabwave.api.base.entities.Chat;
import dev.tinelix.jabwave.api.base.models.NetworkService;
import dev.tinelix.jabwave.api.base.models.ChatGroup;
import dev.tinelix.jabwave.api.base.models.Services;
import dev.tinelix.jabwave.ui.list.adapters.NetworkServicesAdapter;
import dev.tinelix.jabwave.ui.list.sections.NetworkServiceSection;

public class NetworkServicesFragment extends Fragment {
    private JabwaveApp app;
    private ArrayList<ChatGroup> groups;
    private ArrayList<Chat> contacts;
    public NetworkServicesAdapter servicesAdapter;
    private LinearLayoutManager llm;
    private View view;
    private ArrayList<NetworkService> servicesList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = ((JabwaveApp) Objects.requireNonNull(getContext()).getApplicationContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_entity, null);
        loadServices();
        if(getActivity() instanceof AppActivity) {
            AppActivity activity = ((AppActivity) getActivity());
            activity.getSupportActionBar().setTitle(
                    getResources().getString(R.string.services)
            );
        }
        return view;
    }

    public void loadServices() {
        if(getActivity() instanceof AppActivity) {
            AppActivity activity = (AppActivity) getActivity();
            Services services = activity.service.getNetworkServices();
            if(services.getServices() == null || services.getServices().size() == 0) {
                servicesList = services.discoverServices();
            } else {
                servicesList = services.getServices();
            }
            createServicesAdapter();
        }
    }

    private void createServicesAdapter() {
        servicesAdapter = new NetworkServicesAdapter();
        if(getActivity() instanceof AppActivity) {
            AppActivity activity = (AppActivity) getActivity();
            for (NetworkService netService : servicesList) {
                servicesAdapter.addSection(
                        new NetworkServiceSection(getContext(), netService, servicesAdapter, activity.service)
                );
            }
        }
        llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        RecyclerView contactsView = view.findViewById(R.id.entityview);
        contactsView.setLayoutManager(llm);
        contactsView.setAdapter(servicesAdapter);
        contactsView.setVisibility(View.VISIBLE);
        Objects.requireNonNull(getActivity()).findViewById(R.id.progress).setVisibility(View.GONE);
    }

    public void refreshAdapter() {
        if(getActivity() instanceof AppActivity) {
            AppActivity activity = (AppActivity) getActivity();
            contacts = activity.service.getChats().getList();
            servicesAdapter.notifyDataSetChanged();
        }
    }
}
