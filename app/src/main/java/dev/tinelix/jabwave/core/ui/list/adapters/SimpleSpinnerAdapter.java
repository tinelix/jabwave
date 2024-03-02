package dev.tinelix.jabwave.core.ui.list.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.constraintlayout.widget.ConstraintLayout;
import dev.tinelix.jabwave.Global;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.listeners.OnItemSelectListener;
import dev.tinelix.jabwave.core.ui.list.items.SimpleListItem;

@SuppressWarnings("ResourceType")
public class SimpleSpinnerAdapter extends BaseAdapter {
    private final OnItemSelectListener onSelectListener;
    Context ctx;
    LayoutInflater inflater;
    ArrayList<SimpleListItem> objects;

    public SimpleSpinnerAdapter(Context context, ArrayList<SimpleListItem> items, OnItemSelectListener onSelectListener) {
        ctx = context;
        objects = items;
        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.onSelectListener = onSelectListener;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    SimpleListItem getListItem(int position) {
        return ((SimpleListItem) getItem(position));
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.list_item_simple_spinner, parent, false);
        SimpleListItem item = getListItem(position);
        TextView item_name = ((TextView) view.findViewById(R.id.item_title));
        item_name.setText(item.name);
        item_name.setSingleLine(true);
        ConstraintLayout.LayoutParams lp =
                (ConstraintLayout.LayoutParams) item_name.getLayoutParams();
        lp.leftMargin = (int) (15 * Global.getScaledDp(ctx.getResources()));
        return view;
    }

    @Override
    public View getDropDownView(final int position, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.list_item_simple_spinner, parent, false);
        SimpleListItem item = getListItem(position);
        TextView item_name = ((TextView) view.findViewById(R.id.item_title));
        item_name.setText(item.name);
        item_name.setSingleLine(true);
        view.setOnClickListener(clicked_view -> {
            if(onSelectListener != null) {
                onSelectListener.onItemSelect(objects, position);
            }
        });
        return view;
    }

    public static class ViewHolder {
        public TextView item_name;
    }

}

