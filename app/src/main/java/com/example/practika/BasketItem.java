package com.example.practika;

import org.json.JSONObject;

public class BasketItem {
    private String productId;
    private String title;
    private int price;
    private int quantity;

    public BasketItem() {}

    public BasketItem(JSONObject json) {
        try {
            this.productId = json.optString("product_id", "");
            this.title = json.optString("title", "");
            this.price = json.optInt("price", 0);
            this.quantity = json.optInt("quantity", 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BasketItem(String productId, String title, int price, int quantity) {
        this.productId = productId;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getFormattedPrice() {
        return price + " ₽";
    }

    public int getTotalPrice() {
        return price * quantity;
    }

    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("product_id", productId);
            json.put("title", title);
            json.put("price", price);
            json.put("quantity", quantity);
            return json;
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }
}