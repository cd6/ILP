package com.example.s1616573.coinz;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class BankRecyclerViewAdapter extends RecyclerView.Adapter<BankRecyclerViewAdapter.ViewHolder> {

    private List<Message> messages;
    private LayoutInflater inflater;

    // data is passed into the constructor
    BankRecyclerViewAdapter(Context context, List<Message> messages) {
        this.inflater = LayoutInflater.from(context);
        this.messages = messages;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.bank_recyclerview_row, parent, false);
        return new BankRecyclerViewAdapter.ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Message message = messages.get(position);
        holder.textSender.setText(String.format("From: %s", message.getSender()));
        holder.textGoldSent.setText(String.format("Gold: %s", message.getGold()));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return messages.size();
    }

    // stores and recycles views as they are scrolled off screen
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textSender;
        TextView textGoldSent;

        ViewHolder(View itemView) {
            super(itemView);
            textSender = itemView.findViewById(R.id.text_sender);
            textGoldSent = itemView.findViewById(R.id.text_gold_sent);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
        }
    }
}
