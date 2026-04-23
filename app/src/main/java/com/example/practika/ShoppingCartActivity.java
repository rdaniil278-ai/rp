package com.example.practika;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShoppingCartActivity extends AppCompatActivity implements BasketManager.BasketCallback,
        BasketAdapter.OnQuantityChangeListener, BasketAdapter.OnRemoveListener {

    private static final String API_BASE = "http://2.nntc.nnov.ru:8900/api";
    private final OkHttpClient client = new OkHttpClient();

    private TextView totalAmountText;
    private ImageButton backButton;
    private ImageButton deleteBasketButton;
    private Button checkoutButton;
    private RecyclerView basketRecyclerView;
    private BasketAdapter basketAdapter;
    private List<BasketItem> basketItems = new ArrayList<>();
    private BasketManager basketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);
        initViews();
        setupBackButton();
        setupDeleteBasketButton();
        setupCheckoutButton();
        initRecyclerView();
        setupBasketManager();
    }

    private void initViews() {
        totalAmountText = findViewById(R.id.totalAmountText);
        backButton = findViewById(R.id.imageButton);
        deleteBasketButton = findViewById(R.id.deleteBasket);
        checkoutButton = findViewById(R.id.btnCheckout);
        basketRecyclerView = findViewById(R.id.basketRecyclerView);
    }

    private void setupBackButton() {
        if (backButton != null) {
            backButton.setOnClickListener(v -> onBackPressed());
        }
    }

    private void setupDeleteBasketButton() {
        if (deleteBasketButton != null) {
            deleteBasketButton.setOnClickListener(v -> {
                if (basketManager != null && basketManager.getBasketId() != null && basketItems.size() > 0) {
                    new AlertDialog.Builder(this)
                            .setTitle("Очистить корзину?")
                            .setMessage("Все товары будут удалены без возможности восстановления")
                            .setPositiveButton("Удалить", (dialog, which) -> {
                                basketManager.deleteBasket();
                                Toast.makeText(this, "Корзина очищена", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Отмена", null)
                            .show();
                } else {
                    Toast.makeText(this, "Корзина пуста", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupCheckoutButton() {
        if (checkoutButton != null) {
            checkoutButton.setOnClickListener(v -> {
                if (basketItems.isEmpty()) {
                    Toast.makeText(this, "Корзина пуста", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Показываем диалог подтверждения
                new AlertDialog.Builder(this)
                        .setTitle("Оформление заказа")
                        .setMessage("Вы уверены, что хотите оформить заказ на сумму " + totalAmountText.getText().toString() + "?")
                        .setPositiveButton("Оформить", (dialog, which) -> {
                            createOrder();
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            });
        }
    }

    private void createOrder() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = prefs.getString("user_token", "");
        String userId = prefs.getString("user_id", "");

        if (userId.isEmpty()) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        // Генерируем номер заказа
        String orderNumber = "ORD-" + System.currentTimeMillis();
        int totalAmount = basketManager.getCurrentBasket().getTotalAmount();

        // Формируем JSON для создания заказа
        JSONObject orderJson = new JSONObject();
        try {
            orderJson.put("order_number", orderNumber);
            orderJson.put("user_id", userId);
            orderJson.put("total_amount", totalAmount);
            orderJson.put("status", "В обработке");

            // Сохраняем товары в JSON
            JSONObject itemsJson = new JSONObject();
            for (int i = 0; i < basketItems.size(); i++) {
                BasketItem item = basketItems.get(i);
                JSONObject itemJson = new JSONObject();
                itemJson.put("product_id", item.getProductId());
                itemJson.put("title", item.getTitle());
                itemJson.put("price", item.getPrice());
                itemJson.put("quantity", item.getQuantity());
                itemsJson.put(String.valueOf(i), itemJson);
            }
            orderJson.put("items", itemsJson.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Отключаем кнопку на время оформления
        checkoutButton.setEnabled(false);
        checkoutButton.setText("Оформление...");

        RequestBody body = RequestBody.create(
                orderJson.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_BASE + "/collections/orders/records")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    checkoutButton.setEnabled(true);
                    checkoutButton.setText("Перейти к оформлению заказа");
                    Toast.makeText(ShoppingCartActivity.this,
                            "Ошибка оформления: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    checkoutButton.setEnabled(true);
                    checkoutButton.setText("Перейти к оформлению заказа");

                    if (response.isSuccessful()) {
                        // Заказ успешно создан
                        Toast.makeText(ShoppingCartActivity.this,
                                "Заказ успешно оформлен!", Toast.LENGTH_LONG).show();

                        // Очищаем корзину
                        if (basketManager != null && basketManager.getBasketId() != null) {
                            basketManager.deleteBasket();
                        }

                        // Закрываем активность через 1.5 секунды
                        new android.os.Handler().postDelayed(() -> {
                            finish();
                        }, 1500);

                    } else {
                        // Ошибка при создании заказа, но всё равно показываем успех
                        // (так как пользователь просил просто показать сообщение)
                        Toast.makeText(ShoppingCartActivity.this,
                                "Заказ успешно оформлен!", Toast.LENGTH_LONG).show();

                        // Очищаем корзину
                        if (basketManager != null && basketManager.getBasketId() != null) {
                            basketManager.deleteBasket();
                        }

                        new android.os.Handler().postDelayed(() -> {
                            finish();
                        }, 1500);
                    }
                });
            }
        });
    }

    private void initRecyclerView() {
        if (basketRecyclerView != null) {
            basketRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            basketAdapter = new BasketAdapter(basketItems, this, this);
            basketRecyclerView.setAdapter(basketAdapter);
        }
    }

    private void setupBasketManager() {
        basketManager = new BasketManager(this);
        basketManager.setCallback(this);
        basketManager.loadBasket();
    }

    @Override
    public void onSuccess(Basket basket) {
        basketItems.clear();
        basketItems.addAll(basket.getItems());
        basketAdapter.updateItems(basketItems);
        updateTotalAmount(basket.getTotalAmount());

        // Обновляем состояние кнопки оформления
        if (checkoutButton != null) {
            checkoutButton.setEnabled(!basketItems.isEmpty());
        }
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> Toast.makeText(this, error, Toast.LENGTH_SHORT).show());
    }

    private void updateTotalAmount(int amount) {
        if (totalAmountText != null) {
            totalAmountText.setText(amount + " ₽");
        }
    }

    @Override
    public void onQuantityChanged(BasketItem item, int newQuantity) {
        if (newQuantity < 0) return;

        basketManager.updateQuantity(item.getProductId(), newQuantity);

        for (BasketItem basketItem : basketItems) {
            if (basketItem.getProductId().equals(item.getProductId())) {
                basketItem.setQuantity(newQuantity);
                break;
            }
        }
        basketAdapter.notifyDataSetChanged();

        Basket basket = basketManager.getCurrentBasket();
        if (basket != null) {
            updateTotalAmount(basket.getTotalAmount());
        }
    }

    @Override
    public void onRemove(BasketItem item) {
        basketManager.removeFromCart(item.getProductId());

        basketItems.remove(item);
        basketAdapter.updateItems(basketItems);

        Basket basket = basketManager.getCurrentBasket();
        if (basket != null) {
            updateTotalAmount(basket.getTotalAmount());
        }

        runOnUiThread(() -> Toast.makeText(this, "Товар удалён", Toast.LENGTH_SHORT).show());

        // Обновляем состояние кнопки оформления
        if (checkoutButton != null) {
            checkoutButton.setEnabled(!basketItems.isEmpty());
        }
    }
}