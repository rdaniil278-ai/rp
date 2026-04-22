package com.example.practika;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> products;
    private OnAddButtonClickListener listener;

    public interface OnAddButtonClickListener {
        void onAddClick(Product product);
    }

    public ProductAdapter(List<Product> products, OnAddButtonClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textCategory, textPrice;
        Button btnAdd;
        CardView cardView;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textProductTitle);
            textCategory = itemView.findViewById(R.id.textProductCategory);
            textPrice = itemView.findViewById(R.id.ProductPrice);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            cardView = itemView.findViewById(R.id.cardProduct);
        }

        void bind(Product product) {
            textTitle.setText(product.getTitle());
            textCategory.setText(product.getType());
            textPrice.setText(product.getFormattedPrice());
            btnAdd.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddClick(product);
                }
            });
        }
    }
}