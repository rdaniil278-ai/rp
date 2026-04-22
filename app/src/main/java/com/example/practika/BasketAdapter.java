package com.example.practika;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BasketAdapter extends RecyclerView.Adapter<BasketAdapter.BasketViewHolder> {
    private List<BasketItem> items;
    private OnQuantityChangeListener listener;
    private OnRemoveListener removeListener;

    public interface OnQuantityChangeListener {
        void onQuantityChanged(BasketItem item, int newQuantity);
    }

    public interface OnRemoveListener {
        void onRemove(BasketItem item);
    }

    public BasketAdapter(List<BasketItem> items, OnQuantityChangeListener listener, OnRemoveListener removeListener) {
        this.items = items;
        this.listener = listener;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public BasketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new BasketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BasketViewHolder holder, int position) {
        BasketItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void updateItems(List<BasketItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    class BasketViewHolder extends RecyclerView.ViewHolder {
        TextView tvCartName, tvCartPrice, tvCartQuantity;
        ImageButton btnMinus, btnPlus, btnCartRemove;
        CardView cardView;

        BasketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCartName = itemView.findViewById(R.id.tvCartName);
            tvCartPrice = itemView.findViewById(R.id.tvCartPrice);
            tvCartQuantity = itemView.findViewById(R.id.tvCartQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnCartRemove = itemView.findViewById(R.id.btnCartRemove);
        }

        void bind(BasketItem item) {
            tvCartName.setText(item.getTitle());
            tvCartPrice.setText(item.getFormattedPrice());
            tvCartQuantity.setText(item.getQuantity() + " шт");

            btnPlus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuantityChanged(item, item.getQuantity() + 1);
                }
            });

            btnMinus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuantityChanged(item, item.getQuantity() - 1);
                }
            });

            btnCartRemove.setOnClickListener(v -> {
                if (removeListener != null) {
                    removeListener.onRemove(item);
                }
            });
        }
    }
}