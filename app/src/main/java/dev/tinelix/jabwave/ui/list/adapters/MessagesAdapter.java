package dev.tinelix.jabwave.ui.list.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.helper.widget.Flow;
import androidx.recyclerview.widget.RecyclerView;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.net.base.api.entities.Chat;
import dev.tinelix.jabwave.net.base.api.entities.Message;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.Holder> {
    private final Context ctx;
    private final ArrayList<Message> messages;
    private final Chat chat;

    public MessagesAdapter(Context context, ArrayList<Message> messages, Chat chat) {
        this.ctx = context;
        this.messages = messages;
        this.messages.add(0,
                new Message(true,
                        messages.size() > 0 ?
                                messages.get(0).formatTimestamp("dd MMMM yyyy")
                                :
                                ctx.getResources().getString(R.string.no_messages)
                )
        );
        this.chat = chat;
    }

    @NonNull
    @Override
    public MessagesAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType >= 10000) {
            view = View.inflate(ctx, R.layout.list_item_message, null);
        } else {
            view = View.inflate(ctx, R.layout.list_item_message_date, null);
        }
        RecyclerView.LayoutParams lp =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                );
        view.setLayoutParams(lp);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesAdapter.Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        private final CardView msg_card;
        private final TextView msg_author;
        private TextView msg_text;
        private final TextView msg_timestamp;
        private final Flow flow_layout;
        private final View view;

        public Holder(@NonNull View view) {
            super(view);
            this.view = view;
            msg_text = view.findViewById(R.id.msg_text);
            flow_layout = view.findViewById(R.id.flow_layout);
            msg_timestamp = view.findViewById(R.id.msg_timestamp);
            msg_card = view.findViewById(R.id.msg_card);
            msg_author = view.findViewById(R.id.msg_author);
        }

        public void bind(int position) {
            Message msg = messages.get(position);
            if(msg.isHeader()) {
                msg_text = view.findViewById(R.id.msg_date);
            } else {
                msg_timestamp.setText(msg.formatTimestamp());
                if(chat.type == 0 || chat.type == 3) {
                    msg_card.setCardBackgroundColor(ctx.getResources().getColor(R.color.inMessageColor));
                    msg_text.setTextColor(ctx.getResources().getColor(R.color.inMessageTextColor));
                    msg_timestamp.setTextColor(ctx.getResources().getColor(R.color.inMsgTimestampColor));
                } else if (!msg.isIncoming()) {
                    ((FrameLayout.LayoutParams) msg_card.getLayoutParams()).gravity = Gravity.END;
                    msg_text.setTextColor(ctx.getResources().getColor(R.color.outMessageTextColor));
                } else {
                    msg_card.setCardBackgroundColor(ctx.getResources().getColor(R.color.inMessageColor));
                    msg_text.setTextColor(ctx.getResources().getColor(R.color.inMessageTextColor));
                    msg_timestamp.setTextColor(ctx.getResources().getColor(R.color.inMsgTimestampColor));
                    msg_author.setText(
                            msg.getSender() != null ?
                                    String.format("%s %s",
                                            msg.getSender().first_name,
                                            msg.getSender().last_name
                                    ) : chat.title
                    );
                    msg_author.setVisibility(View.VISIBLE);
                    msg_author.setTextColor(ctx.getResources().getColor(R.color.authorInMessageColor));
                }
            }

            msg_text.setText(msg.text);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(!messages.get(position).isHeader()) {
            return 10000 + position;
        } else {
            return position;
        }
    }
}
