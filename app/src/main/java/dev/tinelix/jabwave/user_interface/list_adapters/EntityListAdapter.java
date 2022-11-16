package dev.tinelix.jabwave.user_interface.list_adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.user_interface.list_items.EntityList;

public class EntityListAdapter extends RecyclerView.Adapter<EntityListAdapter.Holder> {

    private final Context ctx;
    private final ArrayList<EntityList> items;

    public EntityListAdapter(Context context, ArrayList<EntityList> els) {
        ctx = context;
        items = els;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public EntityListAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("ViewType", String.format("type: %s", viewType));
        if(viewType == 0) {
            return new Holder(LayoutInflater.from(ctx).inflate(R.layout.maingroup_row, parent, false));
        } else {
            return new Holder(LayoutInflater.from(ctx).inflate(R.layout.mainchild_row, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull EntityListAdapter.Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private final View convertView;
        private TextView titleView;

        public Holder(View view) {
            super(view);
            this.convertView = view;
        }

        public void bind(int position) {
            View view = convertView;
            EntityList el = items.get(position);
            if(EntityListAdapter.this.getItemViewType(position) == 0) {
                titleView = view.findViewById(R.id.groupname);
            } else {
                titleView = view.findViewById(R.id.roster_screenname);
            }
            if(titleView != null) titleView.setText(el.title);
        }
    }
}
