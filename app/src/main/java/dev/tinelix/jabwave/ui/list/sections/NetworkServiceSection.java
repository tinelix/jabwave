package dev.tinelix.jabwave.ui.list.sections;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.services.base.ClientService;
import dev.tinelix.jabwave.api.base.entities.ServiceEntity;
import dev.tinelix.jabwave.api.base.models.NetworkService;
import dev.tinelix.jabwave.ui.list.adapters.NetworkServicesAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class NetworkServiceSection extends Section {
    private final NetworkServicesAdapter adapter;
    private final Context ctx;
    private final ArrayList<ServiceEntity> entities;
    private final ClientService service;
    private NetworkService net_service;
    private boolean isOpen = true;

    public NetworkServiceSection(Context ctx, NetworkService net_service,
                                 NetworkServicesAdapter adapter,
                                 ClientService service) {
        super(SectionParameters.builder()
                .headerResourceId(R.layout.list_item_service)
                .itemResourceId(R.layout.list_item_contacts)
                .build());
        this.ctx = ctx;
        this.entities = new ArrayList<>();
        this.entities.addAll(net_service.getEntities());
        this.adapter = adapter;
        this.net_service = net_service;
        this.service = service;
    }

    @Override
    public int getContentItemsTotal() {
        return isOpen ? entities.size() : 0;
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new EntityViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new EntityGroupViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((EntityViewHolder) holder).bind(position);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        ((EntityGroupViewHolder) holder).bind();
    }

    public ServiceEntity searchEntityByJid(String jid) {
        for (ServiceEntity entity : entities) {
            if(entity.id.equals(jid)) {
                return entity;
            }
        }
        return null;
    }

    public int searchEntityIndexByJid(String jid) {
        for (int i = 0; i < entities.size(); i++) {
            if(entities.get(i).id.equals(jid)) {
                return i;
            }
        }
        return -1;
    }

    public void updateEntity(int index, ServiceEntity entity) {
        entities.set(index, entity);
    }

    class EntityViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final ShapeableImageView contact_avatar;
        private final TextView contact_screenname;
        private final TextView contact_status;

        public EntityViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;

            contact_screenname = view.findViewById(R.id.contact_name);
            contact_status = view.findViewById(R.id.contact_status);
            contact_avatar = view.findViewById(R.id.contact_avatar);
        }

        public Resources getResources() {
            return view.getResources();
        }

        public void bind(int position) {
            ServiceEntity entity = entities.get(position);
            ((TextView) view.findViewById(R.id.contact_name))
                    .setText(entity.title);
            if (entity.id instanceof String) {
                contact_status.setText((String) entity.id);
            } else if (entity.id instanceof Long || entity.id instanceof Integer) {
                contact_status.setText(String.format("%s", entity.id));
            } else {
                contact_status.setVisibility(View.GONE);
            }
            int placeholder_resid;
            switch (service.type) {
                case 2:
                    placeholder_resid = R.drawable.ic_campaign_accent;
                    break;
                case 1:
                    placeholder_resid = R.drawable.ic_group_accent;
                    break;
                default:
                    placeholder_resid = R.drawable.ic_misc_services_accent;
                    break;
            }
            contact_avatar.setImageDrawable(
                    ContextCompat.getDrawable(ctx, placeholder_resid)
            );
        }
    }

    class EntityGroupViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final TextView service_title;
        private final TextView entites_counter;

        public EntityGroupViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.service_title = view.findViewById(R.id.service_name);
            this.entites_counter = view.findViewById(R.id.entities_count);
        }

        public Resources getResources() {
            return view.getResources();
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        public void bind() {
            service_title.setText(String.format("%s (%s)", net_service.title, net_service.node));
            service_title.setOnClickListener(v -> toggleGroupList());
            isOpen = entities.size() != 0;
            Drawable arrow = getResources().getDrawable(
                    isOpen ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_right
            );
            arrow.setBounds(0, 0, 90, 90);
            service_title.setCompoundDrawables(arrow, null, null, null);
            entites_counter.setText(String.format("%s", net_service.getEntities().size()));
            //((TextView) view.findViewById(R.id.members)).setText(getSectionItemsTotal());
        }

        @SuppressLint({"UseCompatLoadingForDrawables", "NotifyDataSetChanged"})
        private void toggleGroupList() {
            isOpen = !isOpen;
            if(!isOpen) {
                entities.clear();
            } else {
                if(net_service.getEntities().size() > 0) {
                    entities.addAll(net_service.getEntities());
                    adapter.notifyDataSetChanged();
                } else {
                    view.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                    new Thread(() -> {
                        net_service.getEntities(service.getClient());
                        new Handler(Looper.getMainLooper()).post(() -> {
                            view.findViewById(R.id.progress).setVisibility(View.GONE);
                            entities.addAll(net_service.getEntities());
                            adapter.notifyDataSetChanged();
                        });
                    }).start();
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            new Handler(Looper.getMainLooper()).post(
                                    () -> entites_counter.setText(
                                            String.format("%s", net_service.getEntities().size())
                                    )
                            );
                        }
                    }, 0, 200);
                }
            }
            Drawable arrow = getResources().getDrawable(
                    isOpen ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_right
            );
            arrow.setBounds(0, 0, 90, 90);
            service_title.setCompoundDrawables(arrow, null, null, null);
            adapter.notifyDataSetChanged();
        }
    }

}
