package com.example.practika;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
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

public class HomeFragment extends Fragment {
    private static final String API_BASE = "http://2.nntc.nnov.ru:8900/api";
    private final OkHttpClient client = new OkHttpClient();

    private RecyclerView productsRecyclerView, newsRecyclerView;
    private ProductAdapter productAdapter;
    private NewsAdapter newsAdapter;
    private List<Product> productList = new ArrayList<>();
    private List<Product> filteredProductList = new ArrayList<>();
    private List<NewsItem> newsList = new ArrayList<>();

    private EditText searchEditText;
    private TextView filterAll, filterWomen, filterMen;
    private String currentFilter = "Все";
    private String currentSearchQuery = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupSearch();
        setupFilters();
        initRecyclerViews(view);
        loadProducts();
        loadNews();
    }

    private void initViews(View view) {
        searchEditText = view.findViewById(R.id.editText);
        filterAll = view.findViewById(R.id.filterAll);
        filterWomen = view.findViewById(R.id.filterWomen);
        filterMen = view.findViewById(R.id.filterMen);
    }

    private void setupSearch() {
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentSearchQuery = searchEditText.getText().toString().toLowerCase();
                applyFilters();
                return true;
            }
            return false;
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        filterAll.setOnClickListener(v -> {
            currentFilter = "Все";
            updateFilterUI(filterAll);
            applyFilters();
        });

        filterWomen.setOnClickListener(v -> {
            currentFilter = "Женщинам";
            updateFilterUI(filterWomen);
            applyFilters();
        });

        filterMen.setOnClickListener(v -> {
            currentFilter = "Мужчинам";
            updateFilterUI(filterMen);
            applyFilters();
        });
    }

    private void updateFilterUI(TextView activeFilter) {
        // Сброс всех
        filterAll.setBackgroundResource(R.drawable.shape_rounded_back_disable);
        filterWomen.setBackgroundResource(R.drawable.shape_rounded_back_disable);
        filterMen.setBackgroundResource(R.drawable.shape_rounded_back_disable);
        filterAll.setTextColor(getResources().getColor(R.color.filter_inactive));
        filterWomen.setTextColor(getResources().getColor(R.color.filter_inactive));
        filterMen.setTextColor(getResources().getColor(R.color.filter_inactive));

        // Активация выбранного
        activeFilter.setBackgroundResource(R.drawable.shape_rounded_btn_enable);
        activeFilter.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void applyFilters() {
        filteredProductList.clear();

        for (Product product : productList) {
            boolean categoryMatch = true;
            if (!currentFilter.equals("Все")) {
                String productType = product.getType();
                categoryMatch = productType != null && productType.equals(currentFilter);
            }

            boolean searchMatch = true;
            if (!currentSearchQuery.isEmpty()) {
                String title = product.getTitle() != null ? product.getTitle().toLowerCase() : "";
                String description = product.getDescription() != null ? product.getDescription().toLowerCase() : "";
                searchMatch = title.contains(currentSearchQuery) || description.contains(currentSearchQuery);
            }

            if (categoryMatch && searchMatch) {
                filteredProductList.add(product);
            }
        }

        productAdapter.updateProducts(filteredProductList);
    }

    private void initRecyclerViews(View view) {
        productsRecyclerView = view.findViewById(R.id.productsRecyclerView);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        productAdapter = new ProductAdapter(filteredProductList, this::showProductBottomSheet);
        productsRecyclerView.setAdapter(productAdapter);

        newsRecyclerView = view.findViewById(R.id.newsRecyclerView);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
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
                                applyFilters();
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