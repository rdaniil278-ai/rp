package com.example.practika;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCartActivity extends AppCompatActivity implements BasketManager.BasketCallback, BasketAdapter.OnQuantityChangeListener, BasketAdapter.OnRemoveListener {
    private TextView totalAmountText;
    private ImageButton backButton;
    private ImageButton deleteBasketButton; //  Добавляем кнопку удаления корзины
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
        setupDeleteBasketButton(); //  Настраиваем кнопку удаления
        initRecyclerView();
        setupBasketManager();
    }

    private void initViews() {
        totalAmountText = findViewById(R.id.totalAmountText);
        backButton = findViewById(R.id.imageButton);
        deleteBasketButton = findViewById(R.id.deleteBasket); //  Находим кнопку по ID
        basketRecyclerView = findViewById(R.id.basketRecyclerView);
    }

    private void setupBackButton() {
        if (backButton != null) {
            backButton.setOnClickListener(v -> onBackPressed());
        }
    }

    //  Новый метод для настройки кнопки удаления корзины
    private void setupDeleteBasketButton() {
        if (deleteBasketButton != null) {
            deleteBasketButton.setOnClickListener(v -> {
                if (basketManager != null && basketManager.getBasketId() != null) {
                    // Показываем подтверждение
                    new androidx.appcompat.app.AlertDialog.Builder(this)
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

        // Обновляем локально для быстрого отклика
        for (BasketItem basketItem : basketItems) {
            if (basketItem.getProductId().equals(item.getProductId())) {
                basketItem.setQuantity(newQuantity);
                break;
            }
        }
        basketAdapter.notifyDataSetChanged();

        // Пересчитываем сумму — безопасно через getCurrentBasket()
        Basket basket = basketManager.getCurrentBasket();
        if (basket != null) {
            updateTotalAmount(basket.getTotalAmount());
        }
    }

    @Override
    public void onRemove(BasketItem item) {
        basketManager.removeFromCart(item.getProductId());

        // Обновляем локально
        basketItems.remove(item);
        basketAdapter.updateItems(basketItems);

        // Пересчитываем сумму
        Basket basket = basketManager.getCurrentBasket();
        if (basket != null) {
            updateTotalAmount(basket.getTotalAmount());
        }

        runOnUiThread(() -> Toast.makeText(this, "Товар удалён", Toast.LENGTH_SHORT).show());
    }
}