package com.example.practika;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ProductBottomSheetFragment extends BottomSheetDialogFragment {
    private static final String TAG = "BOTTOMSHEET_DEBUG";
    private String productName;
    private String productDescription;
    private String productPrice;
    private String productApproximateCost;
    private String productId;
    private int productPriceValue;

    public static ProductBottomSheetFragment newInstance(String id, String name, String description, String price, String approximateCost, int priceValue) {
        ProductBottomSheetFragment fragment = new ProductBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putString("name", name);
        args.putString("description", description);
        args.putString("price", price);
        args.putString("approximateCost", approximateCost);
        args.putInt("priceValue", priceValue);
        fragment.setArguments(args);
        return fragment;
    }

    public static ProductBottomSheetFragment newInstance(String name, String description, String price, String approximateCost) {
        return newInstance("", name, description, price, approximateCost, 0);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString("id", "");
            productName = getArguments().getString("name");
            productDescription = getArguments().getString("description");
            productPrice = getArguments().getString("price");
            productApproximateCost = getArguments().getString("approximateCost", "");
            productPriceValue = getArguments().getInt("priceValue", 0);

            Log.d(TAG, "Product data loaded:");
            Log.d(TAG, "  ID: " + productId);
            Log.d(TAG, "  Name: " + productName);
            Log.d(TAG, "  Price: " + productPriceValue);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvConsumption = view.findViewById(R.id.tvConsumption);
        TextView tvConsumptionLabel = view.findViewById(R.id.tvConsumptionLabel);
        Button btnAddToCart = view.findViewById(R.id.btnAddToCart);
        ImageView btnClose = view.findViewById(R.id.btnClose);

        if (tvTitle != null) tvTitle.setText(productName != null ? productName : "");
        if (tvDescription != null) tvDescription.setText(productDescription != null ? productDescription : "");

        if (tvConsumption != null && tvConsumptionLabel != null) {
            if (productApproximateCost != null && !productApproximateCost.isEmpty()) {
                tvConsumption.setText(productApproximateCost + " г");
                tvConsumption.setVisibility(View.VISIBLE);
                tvConsumptionLabel.setVisibility(View.VISIBLE);
            } else {
                tvConsumption.setVisibility(View.GONE);
                tvConsumptionLabel.setVisibility(View.GONE);
            }
        }

        if (btnAddToCart != null && productPrice != null) {
            btnAddToCart.setText("Добавить за " + productPrice);
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }

        if (btnAddToCart != null) {
            btnAddToCart.setOnClickListener(v -> {
                Log.d(TAG, "=== Add to Cart button clicked ===");
                Log.d(TAG, "Product ID: " + productId);
                Log.d(TAG, "Product Name: " + productName);
                Log.d(TAG, "Product Price: " + productPriceValue);

                if (getContext() != null) {
                    if (productId == null || productId.isEmpty()) {
                        Log.e(TAG, "ERROR: Product ID is empty!");
                        Toast.makeText(getContext(), "Ошибка: нет ID товара", Toast.LENGTH_SHORT).show();
                        dismiss();
                        return;
                    }

                    //  Создаём BasketManager и добавляем товар
                    BasketManager basketManager = new BasketManager(getContext());

                    // Создаём продукт через сеттеры (проще и надёжнее)
                    Product product = new Product();
                    product.setId(productId);
                    product.setTitle(productName);
                    product.setPrice(productPriceValue);

                    Log.d(TAG, "Calling basketManager.addToCart()...");
                    basketManager.addToCart(product, 1);

                    Toast.makeText(getContext(), "Добавлено в корзину", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "ERROR: Context is null!");
                }
                dismiss();
            });
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.7);
                bottomSheet.setLayoutParams(layoutParams);
            }
        });
        return dialog;
    }
}