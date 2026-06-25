package com.example.shoeapp.user.adapter;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.data.entity.ChatMessage;
import com.example.shoeapp.user.MessageLinkifier;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    public interface ChatCallback {
        void onRetryClick(ChatMessage message);
        void onFeedbackClick(ChatMessage message, int rating);
    }

    private final Context context;
    private final ChatCallback callback;
    private final List<ChatMessage> messages = new ArrayList<>();

    public ChatAdapter(Context context, ChatCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void setMessages(List<ChatMessage> newMessages) {
        this.messages.clear();
        if (newMessages != null) {
            this.messages.addAll(newMessages);
        }
        notifyDataSetChanged();
    }

    public void addMessage(ChatMessage message) {
        this.messages.add(message);
        notifyItemInserted(this.messages.size() - 1);
    }

    public void removeLastMessage() {
        if (!this.messages.isEmpty()) {
            int lastIndex = this.messages.size() - 1;
            this.messages.remove(lastIndex);
            notifyItemRemoved(lastIndex);
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        holder.layoutUser.setVisibility(View.GONE);
        holder.layoutBot.setVisibility(View.GONE);
        holder.layoutError.setVisibility(View.GONE);
        holder.layoutLoading.setVisibility(View.GONE);

        if (message.isUser) {
            holder.layoutUser.setVisibility(View.VISIBLE);
            holder.textUserMessage.setText(message.content);
        } else if (message.isError) {
            holder.layoutError.setVisibility(View.VISIBLE);
            holder.textErrorMessage.setText(message.content);
            holder.btnRetry.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onRetryClick(message);
                }
            });
        } else if (message.content == null) {
            holder.layoutLoading.setVisibility(View.VISIBLE);
        } else {
            holder.layoutBot.setVisibility(View.VISIBLE);

            SpannableStringBuilder linkifiedText = MessageLinkifier.linkifyProducts(context, message.content);
            holder.textBotMessage.setText(linkifiedText);
            holder.textBotMessage.setMovementMethod(LinkMovementMethod.getInstance());

            int grayColor = ContextCompat.getColor(context, R.color.text_muted);
            int orangeColor = ContextCompat.getColor(context, R.color.brand_orange);

            holder.btnLike.setColorFilter(message.feedbackRating == 1 ? orangeColor : grayColor);
            holder.btnDislike.setColorFilter(message.feedbackRating == -1 ? orangeColor : grayColor);

            holder.btnLike.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onFeedbackClick(message, message.feedbackRating == 1 ? 0 : 1);
                }
            });

            holder.btnDislike.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onFeedbackClick(message, message.feedbackRating == -1 ? 0 : -1);
                }
            });


        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutUser;
        LinearLayout layoutBot;
        LinearLayout layoutError;
        LinearLayout layoutLoading;
        TextView textUserMessage;
        TextView textBotMessage;
        TextView textErrorMessage;
        TextView btnRetry;
        ImageButton btnLike;
        ImageButton btnDislike;
        TextView textLoadingDots;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutUser = itemView.findViewById(R.id.layout_user);
            layoutBot = itemView.findViewById(R.id.layout_bot);
            layoutError = itemView.findViewById(R.id.layout_error);
            layoutLoading = itemView.findViewById(R.id.layout_loading);
            textUserMessage = itemView.findViewById(R.id.text_user_message);
            textBotMessage = itemView.findViewById(R.id.text_bot_message);
            textErrorMessage = itemView.findViewById(R.id.text_error_message);
            btnRetry = itemView.findViewById(R.id.btn_retry);
            btnLike = itemView.findViewById(R.id.btn_like);
            btnDislike = itemView.findViewById(R.id.btn_dislike);
            textLoadingDots = itemView.findViewById(R.id.text_loading_dots);
        }
    }
}
