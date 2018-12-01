package com.example.s1616573.coinz;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    // https://stackoverflow.com/questions/40584424/simple-android-recyclerview-example

    private List<Coin> coins;
    private LayoutInflater inflater;
    private ItemClickListener clickListener;
    private SparseBooleanArray storeChecked = new SparseBooleanArray();

    // data is passed into the constructor
    RecyclerViewAdapter(Context context, List<Coin> coins) {
        this.inflater = LayoutInflater.from(context);
        this.coins = coins;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Coin coin = coins.get(position);
        holder.textCurrency.setText(String.format("Currency: %s", coin.getCurrency()));
        holder.textValue.setText(String.format("Value: %s", coin.getValue()));
        holder.textGoldValue.setText(String.format("Gold: %s", coin.getGoldValue()));
        if(storeChecked.get(position, true)){
            holder.itemView.setSelected(false);
        }else{
            holder.itemView.setSelected(true);
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return coins.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textCurrency;
        TextView textValue;
        TextView textGoldValue;


        ViewHolder(View itemView) {
            super(itemView);
            textCurrency = itemView.findViewById(R.id.text_currency);
            textValue = itemView.findViewById(R.id.text_value);
            textGoldValue = itemView.findViewById(R.id.text_gold_value);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                clickListener.onItemClick(view, getAdapterPosition());
               if (storeChecked.get(getAdapterPosition(), true)) {
                   storeChecked.put(getAdapterPosition(), false);
               } else {
                   storeChecked.put(getAdapterPosition(), true);
               }
                notifyDataSetChanged();
            }
        }
    }

    // convenience method for getting data at click position
    Coin getItem(int pos) {
        return coins.get(pos);
    }

    void removeItems(Set<Integer> pos) {
        List<Integer> posSorted = new ArrayList<>(pos);
        posSorted.sort(Collections.reverseOrder());
        for (int i : posSorted) {
            coins.remove(i);
            notifyItemRemoved(i);
        }
        storeChecked.clear();
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}