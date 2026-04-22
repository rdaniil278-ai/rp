package com.example.practika;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class HomeFragment extends Fragment {
    private static final String API_BASE = "http://2.nntc.nnov.ru:8900/api";
    private final OkHttpClient client = new OkHttpClient();

    private RecyclerView productsRecyclerView, newsRecyclerView;
    private ProductAdapter productAdapter;
    private NewsAdapter newsAdapter;
    private List<Product> productList = new ArrayList<>();
    private List<NewsItem> newsList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerViews(view);
        loadProducts();
        loadNews();
    }

    private void initRecyclerViews(View view) {
        // Товары
        productsRecyclerView = view.findViewById(R.id.productsRecyclerView);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        productAdapter = new ProductAdapter(productList, this::showProductBottomSheet);
        productsRecyclerView.setAdapter(productAdapter);

        // Новости/Акции
        newsRecyclerView = view.findViewById(R.id.newsRecyclerView);
        newsRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        newsAdapter = new NewsAdapter(newsList);
        newsRecyclerView.setAdapter(newsAdapter);
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

    private void loadNews() {
        Request request = new Request.Builder()
                .url(API_BASE + "/collections/promotions_and_news/records")
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Ошибка загрузки новостей", Toast.LENGTH_SHORT).show()
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
                                newsList.clear();
                                for (int i = 0; i < items.length(); i++) {
                                    newsList.add(new NewsItem(items.getJSONObject(i)));
                                }
                                newsAdapter.updateNews(newsList);
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
}