package dev.tinelix.jabwave.ui.list.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import dev.tinelix.jabwave.Global;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.services.base.ClientService;
import dev.tinelix.jabwave.api.base.entities.Chat;
import dev.tinelix.jabwave.api.base.entities.Message;
import dev.tinelix.jabwave.ui.views.AttachFlowLayout;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.Holder> {
    private final Context ctx;
    private final ArrayList<Message> messages;
    private final Chat chat;
    private final ClientService service;

    public MessagesAdapter(Context context, ArrayList<Message> messages, Chat chat,
                           ClientService service) {
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
        this.service = service;
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
        private final LinearLayout flow_container;
        private final Space flow_container_space;
        private TextView msg_text;
        private final TextView msg_timestamp;
        private final AttachFlowLayout attach_view;
        private final View view;

        public Holder(@NonNull View view) {
            super(view);
            this.view = view;
            msg_text = view.findViewById(R.id.msg_text);
            attach_view = view.findViewById(R.id.flow_layout);
            flow_container = view.findViewById(R.id.flow_container);
            flow_container_space = view.findViewById(R.id.container_space);
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
                flow_container_space.setVisibility(View.GONE);
                if(chat.type == 3) {
                    msg_card.setCardBackgroundColor(
                            Global.getColorAttribute(ctx, R.attr.inMsgBubbleBackgroundColor)
                    );
                    msg_text.setMaxWidth((int) (256 * ctx.getResources().getDisplayMetrics().scaledDensity));
                    msg_text.setTextColor(Global.getColorAttribute(ctx, R.attr.inMessageTextColor));
                    msg_timestamp.setTextColor(ctx.getResources().getColor(R.color.inMsgTimestampColor));
                } else if(chat.type == 0 && msg.isIncoming()) {
                    msg_card.setCardBackgroundColor(
                            Global.getColorAttribute(ctx, R.attr.inMsgBubbleBackgroundColor)
                    );
                    msg_text.setTextColor(Global.getColorAttribute(ctx, R.attr.inMessageTextColor));
                    msg_timestamp.setTextColor(ctx.getResources().getColor(R.color.inMsgTimestampColor));
                } else if (!msg.isIncoming()) {
                    ((FrameLayout.LayoutParams) msg_card.getLayoutParams()).gravity = Gravity.END;
                    msg_text.setTextColor(
                            Global.getColorAttribute(ctx, R.attr.outMessageTextColor)
                    );
                    msg_card.setCardBackgroundColor(
                            Global.getColorAttribute(ctx, R.attr.outMsgBubbleBackgroundColor)
                    );
                } else {
                    if(msg.getAttachments() != null && msg.getAttachments().size() > 0) {
                        flow_container_space.setVisibility(View.VISIBLE);
                    }
                    msg_card.setCardBackgroundColor(
                            Global.getColorAttribute(ctx, R.attr.inMsgBubbleBackgroundColor)
                    );
                    msg_text.setTextColor(Global.getColorAttribute(ctx, R.attr.inMessageTextColor));
                    msg_timestamp.setTextColor(ctx.getResources().getColor(R.color.inMsgTimestampColor));
                    if(msg.getSender() != null) {
                        if(msg.getSender().first_name != null && msg.getSender().last_name != null) {
                            msg_author.setText(
                                    String.format("%s %s",
                                            msg.getSender().first_name,
                                            msg.getSender().last_name
                                    )
                            );
                        } else {
                            msg_author.setText(msg.getSender().name);
                        }
                    } else {
                        msg_author.setText(chat.title);
                    }
                    msg_author.setVisibility(View.VISIBLE);
                    msg_author.setTextColor(ctx.getResources().getColor(R.color.authorInMessageColor));
                }
            }

            msg_text.setText(msg.text);

            if(msg.getAttachments() != null && msg.getAttachments().size() > 0) {
                attach_view.removeAllViews();
                attach_view.loadAttachments(msg.getAttachments(), service);
                flow_container.setVisibility(View.VISIBLE);
                if(msg.text.length() == 0) {
                    msg_text.setVisibility(View.GONE);
                    ((LinearLayout) view.findViewById(R.id.message_text_area))
                            .setOrientation(LinearLayout.VERTICAL);
                    ((LinearLayout.LayoutParams) msg_timestamp.getLayoutParams())
                            .setMarginStart(0);
                    msg_timestamp.setGravity(Gravity.END);
                }
                msg_card.getLayoutParams().width = (int) (220 * ctx.getResources().getDisplayMetrics().scaledDensity);
            }
            if(msg.text.length() > 20 || msg.text.split("\n").length > 1) {
                ((LinearLayout) view.findViewById(R.id.message_text_area))
                        .setOrientation(LinearLayout.VERTICAL);
                ((LinearLayout.LayoutParams) msg_timestamp.getLayoutParams())
                        .setMarginStart(0);
                msg_timestamp.setGravity(Gravity.END);
            }
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
