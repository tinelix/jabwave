package dev.tinelix.jabwave.ui.list.adapters;

import android.annotation.SuppressLint;

import dev.tinelix.jabwave.api.base.entities.Chat;
import dev.tinelix.jabwave.ui.list.sections.ChatsGroupSection;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class NetworkServicesAdapter extends SectionedRecyclerViewAdapter {

    @Override
    public int getSectionItemViewType(int position) {
        return position;
    }

}
