package com.example.practika;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BasketManager {
    private static final String TAG = "BASKET_DEBUG";
    private static final String API_BASE = "http://2.nntc.nnov.ru:8900/api";
    private final OkHttpClient client = new OkHttpClient();
    private final Context context;
    private Basket currentBasket;
    private String basketId;
    private BasketCallback callback;

    //  Очередь действий для выполнения после загрузки корзины
    private final List<Runnable> pendingActions = new ArrayList<>();
    private boolean isLoading = false;

    public interface BasketCallback {
        void onSuccess(Basket basket);
        void onError(String error);
    }

    public BasketManager(Context context) {
        this.context = context;
        this.currentBasket = new Basket();
        Log.d(TAG, "=== BasketManager initialized ===");
    }

    public void setCallback(BasketCallback callback) {
        this.callback = callback;
    }

    private String getToken() {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("user_token", "");
        Log.d(TAG, "getToken: " + (token.isEmpty() ? "EMPTY" : token.substring(0, Math.min(20, token.length())) + "..."));
        return token;
    }

    private String getUserId() {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("user_id", "");
        Log.d(TAG, "getUserId: " + (userId.isEmpty() ? "EMPTY" : userId));
        return userId;
    }
    
    private void executePendingActions() {
        if (!pendingActions.isEmpty()) {
            Log.d(TAG, "executePendingActions: Executing " + pendingActions.size() + " pending action(s)");
            for (Runnable action : pendingActions) {
                action.run();
            }
            pendingActions.clear();
        }
    }

    // Загрузка корзины с сервера
    public void loadBasket() {
        Log.d(TAG, "=== loadBasket START ===");
        String token = getToken();
        String userId = getUserId();

        if (userId.isEmpty()) {
            Log.e(TAG, "loadBasket: User not authorized!");
            if (callback != null) {
                ((Activity) context).runOnUiThread(() -> callback.onError("Пользователь не авторизован"));
            }
            return;
        }

        Log.d(TAG, "GET /collections/basket/records");
        Request request = new Request.Builder()
                .url(API_BASE + "/collections/basket/records")
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "loadBasket: Network failure: " + e.getMessage(), e);
                isLoading = false; //
                if (callback != null) {
                    ((Activity) context).runOnUiThread(() -> callback.onError("Ошибка сети: " + e.getMessage()));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "loadBasket: HTTP " + response.code());
                Log.d(TAG, "loadBasket: Response: " + responseData);

                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(responseData);
                        JSONArray items = json.optJSONArray("items");
                        Log.d(TAG, "loadBasket: Items count: " + (items != null ? items.length() : 0));

                        if (items != null && items.length() > 0) {
                            JSONObject basketJson = items.getJSONObject(0);
                            currentBasket = new Basket(basketJson);
                            basketId = currentBasket.getId();
                            Log.d(TAG, "loadBasket: Basket found! ID: " + basketId);
                            Log.d(TAG, "loadBasket: Basket items count: " + currentBasket.getItems().size());
                            
                            executePendingActions();
                        } else {
                            Log.d(TAG, "loadBasket: No basket found, creating new...");
                            createBasket();
                            return;
                        }

                        if (callback != null) {
                            ((Activity) context).runOnUiThread(() -> callback.onSuccess(currentBasket));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "loadBasket: Parse error: " + e.getMessage(), e);
                        if (callback != null) {
                            ((Activity) context).runOnUiThread(() -> callback.onError("Ошибка парсинга: " + e.getMessage()));
                        }
                    }
                } else if (response.code() == 404) {
                    Log.d(TAG, "loadBasket: 404 - Creating new basket");
                    createBasket();
                } else {
                    Log.e(TAG, "loadBasket: HTTP error " + response.code());
                    if (callback != null) {
                        ((Activity) context).runOnUiThread(() -> callback.onError("Ошибка: " + response.code()));
                    }
                }
                isLoading = false;
            }
        });
    }

    // Создание новой корзины
    private void createBasket() {
        Log.d(TAG, "=== createBasket START ===");
        String token = getToken();
        String userId = getUserId();

        JSONObject json = new JSONObject();
        try {
            json.put("user_id", userId);
            json.put("items", new JSONArray());
            json.put("count", 0);
        } catch (Exception e) {
            Log.e(TAG, "createBasket: JSON creation error: " + e.getMessage(), e);
        }

        String jsonString = json.toString();
        Log.d(TAG, "POST /collections/basket/records");
        Log.d(TAG, "Request JSON: " + jsonString);

        RequestBody body = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_BASE + "/collections/basket/records")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "createBasket: Network failure: " + e.getMessage(), e);
                isLoading = false; //  Сбрасываем флаг
                if (callback != null) {
                    ((Activity) context).runOnUiThread(() -> callback.onError("Ошибка создания: " + e.getMessage()));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "createBasket: HTTP " + response.code());
                Log.d(TAG, "createBasket: Response: " + responseData);

                if (response.isSuccessful()) {
                    try {
                        currentBasket = new Basket(new JSONObject(responseData));
                        basketId = currentBasket.getId();
                        Log.d(TAG, "createBasket: SUCCESS! Basket ID: " + basketId);

                        //  Выполняем отложенные действия
                        executePendingActions();

                        if (callback != null) {
                            ((Activity) context).runOnUiThread(() -> callback.onSuccess(currentBasket));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "createBasket: Parse error: " + e.getMessage(), e);
                        if (callback != null) {
                            ((Activity) context).runOnUiThread(() -> callback.onError("Ошибка парсинга: " + e.getMessage()));
                        }
                    }
                } else {
                    Log.e(TAG, "createBasket: HTTP error " + response.code());
                    if (callback != null) {
                        ((Activity) context).runOnUiThread(() -> callback.onError("Ошибка: " + response.code()));
                    }
                }
                //  Сбрасываем флаг загрузки
                isLoading = false;
            }
        });
    }

    // Добавление товара в корзину
    public void addToCart(Product product, int quantity) {
        Log.d(TAG, "=== addToCart START ===");
        Log.d(TAG, "addToCart: Product ID: " + product.getId());
        Log.d(TAG, "addToCart: Product Title: " + product.getTitle());
        Log.d(TAG, "addToCart: Product Price: " + product.getPrice());
        Log.d(TAG, "addToCart: Quantity: " + quantity);
        Log.d(TAG, "addToCart: Current basketId: " + (basketId != null ? basketId : "NULL"));

        if (basketId == null || basketId.isEmpty()) {
            Log.d(TAG, "addToCart: No basket ID, loading basket first...");

            //  Добавляем действие в очередь
            pendingActions.add(() -> {
                Log.d(TAG, "addToCart: Executing pending action after load");
                BasketItem newItem = new BasketItem(
                        product.getId(),
                        product.getTitle(),
                        product.getPrice(),
                        quantity
                );
                currentBasket.addItem(newItem);
                Log.d(TAG, "addToCart: Item added to local basket");
                updateBasketOnServer();
            });

            if (!isLoading) {
                isLoading = true;
                loadBasket();
            } else {
                Log.d(TAG, "addToCart: Basket loading already in progress, action queued");
            }
            return;
        }

        BasketItem newItem = new BasketItem(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                quantity
        );

        Log.d(TAG, "addToCart: Adding item to local basket...");
        currentBasket.addItem(newItem);
        Log.d(TAG, "addToCart: Local basket items count: " + currentBasket.getItems().size());
        Log.d(TAG, "addToCart: Local basket total: " + currentBasket.getTotalAmount());

        updateBasketOnServer();
    }

    // Обновление количества товара
    public void updateQuantity(String productId, int quantity) {
        Log.d(TAG, "=== updateQuantity ===");
        Log.d(TAG, "updateQuantity: Product ID: " + productId);
        Log.d(TAG, "updateQuantity: New quantity: " + quantity);

        currentBasket.updateQuantity(productId, quantity);
        updateBasketOnServer();
    }

    // Удаление товара из корзины
    public void removeFromCart(String productId) {
        Log.d(TAG, "=== removeFromCart ===");
        Log.d(TAG, "removeFromCart: Product ID: " + productId);

        currentBasket.removeItem(productId);
        updateBasketOnServer();
    }

    // Обновление корзины на сервере
    private void updateBasketOnServer() {
        Log.d(TAG, "=== updateBasketOnServer START ===");
        Log.d(TAG, "updateBasketOnServer: basketId: " + (basketId != null ? basketId : "NULL"));

        if (basketId == null || basketId.isEmpty()) {
            Log.w(TAG, "updateBasketOnServer: No basket ID, loading basket...");
            loadBasket();
            return;
        }

        String token = getToken();
        JSONObject basketJson = currentBasket.toJson();
        String jsonString = basketJson.toString();

        Log.d(TAG, "PATCH /collections/basket/records/" + basketId);
        Log.d(TAG, "Request JSON: " + jsonString);

        RequestBody body = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_BASE + "/collections/basket/records/" + basketId)
                .patch(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "updateBasketOnServer: Network failure: " + e.getMessage(), e);
                if (callback != null) {
                    ((Activity) context).runOnUiThread(() -> callback.onError("Ошибка обновления: " + e.getMessage()));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "updateBasketOnServer: HTTP " + response.code());
                Log.d(TAG, "updateBasketOnServer: Response: " + responseData);

                if (response.isSuccessful()) {
                    try {
                        currentBasket = new Basket(new JSONObject(responseData));
                        basketId = currentBasket.getId();
                        Log.d(TAG, "updateBasketOnServer: SUCCESS!");
                        Log.d(TAG, "updateBasketOnServer: Basket items: " + currentBasket.getItems().size());
                        Log.d(TAG, "updateBasketOnServer: Basket total: " + currentBasket.getTotalAmount());

                        if (callback != null) {
                            ((Activity) context).runOnUiThread(() -> callback.onSuccess(currentBasket));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "updateBasketOnServer: Parse error: " + e.getMessage(), e);
                        if (callback != null) {
                            ((Activity) context).runOnUiThread(() -> callback.onError("Ошибка парсинга: " + e.getMessage()));
                        }
                    }
                } else {
                    Log.e(TAG, "updateBasketOnServer: HTTP error " + response.code());
                    if (callback != null) {
                        ((Activity) context).runOnUiThread(() -> callback.onError("Ошибка: " + response.code()));
                    }
                }
            }
        });
    }

    // Удаление корзины
    public void deleteBasket() {
        Log.d(TAG, "=== deleteBasket START ===");
        if (basketId == null || basketId.isEmpty()) {
            Log.w(TAG, "deleteBasket: No basket ID");
            return;
        }

        String token = getToken();
        Log.d(TAG, "DELETE /collections/basket/records/" + basketId);

        Request request = new Request.Builder()
                .url(API_BASE + "/collections/basket/records/" + basketId)
                .delete()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "deleteBasket: Network failure: " + e.getMessage(), e);
                if (callback != null) {
                    ((Activity) context).runOnUiThread(() -> callback.onError("Ошибка удаления: " + e.getMessage()));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "deleteBasket: HTTP " + response.code());
                if (response.code() == 204 || response.isSuccessful()) {
                    Log.d(TAG, "deleteBasket: SUCCESS");
                    currentBasket = new Basket();
                    basketId = null;
                    if (callback != null) {
                        ((Activity) context).runOnUiThread(() -> callback.onSuccess(currentBasket));
                    }
                } else {
                    Log.e(TAG, "deleteBasket: HTTP error " + response.code());
                    if (callback != null) {
                        ((Activity) context).runOnUiThread(() -> callback.onError("Ошибка: " + response.code()));
                    }
                }
            }
        });
    }

    public Basket getCurrentBasket() {
        return currentBasket;
    }

    public String getBasketId() {
        return basketId;
    }
}