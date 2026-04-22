package com.example.practika;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.util.Log;

public class CatalogFragment extends Fragment {
    private static final String API_BASE = "http://2.nntc.nnov.ru:8900/api";
    private final OkHttpClient client = new OkHttpClient();

    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();

    private TextView cartPriceText;
    private BasketManager basketManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_catalog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView iconProfile = view.findViewById(R.id.iconProfile);
        iconProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.putExtra("open_profile", true);
            startActivity(intent);
        });

        initRecyclerView(view);
        loadProducts();
        setupCartButton(view);
        setupCartPriceListener();
    }
    private void setupCartPriceListener() {
        cartPriceText = getView().findViewById(R.id.priceText);

        basketManager = new BasketManager(requireContext());
        basketManager.setCallback(new BasketManager.BasketCallback() {
            @Override
            public void onSuccess(Basket basket) {
                if (cartPriceText != null && isAdded()) {
                    cartPriceText.setText(basket.getFormattedTotalAmount());
                }
            }
            @Override
            public void onError(String error) {
                // Можно логировать, но не показывать Toast в фрагменте без runOnUiThread
            }
        });

        // Загружаем корзину при старте фрагмента
        basketManager.loadBasket();
    }
    private void initRecyclerView(View view) {
        productsRecyclerView = view.findViewById(R.id.productsRecyclerView);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        productAdapter = new ProductAdapter(productList, this::showProductBottomSheet);
        productsRecyclerView.setAdapter(productAdapter);
    }

    private void loadProducts() {
        Request request = new Request.Builder()
                .url(API_BASE + "/collections/products/records")
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Ошибка загрузки товаров", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body() != null ? response.body().string() : "";
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject json = new JSONObject(responseData);
                                JSONArray items = json.getJSONArray("items");
                                productList.clear();
                                for (int i = 0; i < items.length(); i++) {
                                    productList.add(new Product(items.getJSONObject(i)));
                                }
                                productAdapter.updateProducts(productList);
                            } catch (Exception e) {
                                Toast.makeText(requireContext(), "Ошибка парсинга", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void showProductBottomSheet(Product product) {
        Log.d("CATALOG_DEBUG", "=== showProductBottomSheet ===");
        Log.d("CATALOG_DEBUG", "Product ID: " + product.getId());
        Log.d("CATALOG_DEBUG", "Product Title: " + product.getTitle());
        Log.d("CATALOG_DEBUG", "Product Price: " + product.getPrice());

        ProductBottomSheetFragment bottomSheet = ProductBottomSheetFragment.newInstance(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getFormattedPrice(),
                product.getApproximateCost(),
                product.getPrice()
        );
        bottomSheet.show(getChildFragmentManager(), "ProductBottomSheet");
    }

    private void refreshCartPrice() {
        TextView priceText = getView().findViewById(R.id.priceText);
        if (priceText == null || !isAdded()) return;

        BasketManager tempManager = new BasketManager(requireContext());
        tempManager.setCallback(new BasketManager.BasketCallback() {
            @Override
            public void onSuccess(Basket basket) {
                if (isAdded() && priceText != null) {
                    priceText.setText(basket.getFormattedTotalAmount());
                }
            }
            @Override
            public void onError(String error) {
                // Можно залогировать, но не показывать Toast
            }
        });
        tempManager.loadBasket();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCartPrice();
    }

    private void setupCartButton(View view) {
        View btnAddToCart = view.findViewById(R.id.btnAddToCart);
        if (btnAddToCart != null) {
            btnAddToCart.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), ShoppingCartActivity.class);
                startActivity(intent);
            });
        }
    }
}