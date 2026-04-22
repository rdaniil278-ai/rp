package com.example.practika;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class Basket {
    private String id;
    private String collectionId;
    private String collectionName;
    private String created;
    private String updated;
    private String userId;
    private List<BasketItem> items;
    private int count;

    public Basket() {
        this.items = new ArrayList<>();
    }

    public Basket(JSONObject json) {
        this.items = new ArrayList<>();
        try {
            this.id = json.optString("id", "");
            this.collectionId = json.optString("collectionId", "");
            this.collectionName = json.optString("collectionName", "");
            this.created = json.optString("created", "");
            this.updated = json.optString("updated", "");
            this.userId = json.optString("user_id", "");

            if (json.has("items") && !json.isNull("items")) {
                JSONArray itemsArray = json.getJSONArray("items");
                for (int i = 0; i < itemsArray.length(); i++) {
                    items.add(new BasketItem(itemsArray.getJSONObject(i)));
                }
            }

            this.count = json.optInt("count", 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCollectionId() { return collectionId; }
    public void setCollectionId(String collectionId) { this.collectionId = collectionId; }

    public String getCollectionName() { return collectionName; }
    public void setCollectionName(String collectionName) { this.collectionName = collectionName; }

    public String getCreated() { return created; }
    public void setCreated(String created) { this.created = created; }

    public String getUpdated() { return updated; }
    public void setUpdated(String updated) { this.updated = updated; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<BasketItem> getItems() { return items; }
    public void setItems(List<BasketItem> items) { this.items = items; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public int getTotalAmount() {
        int total = 0;
        for (BasketItem item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public String getFormattedTotalAmount() {
        return getTotalAmount() + " ₽";
    }

    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("user_id", userId);
            json.put("count", count);

            JSONArray itemsArray = new JSONArray();
            for (BasketItem item : items) {
                itemsArray.put(item.toJson());
            }
            json.put("items", itemsArray);

            return json;
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public void addItem(BasketItem newItem) {
        for (BasketItem existingItem : items) {
            if (existingItem.getProductId().equals(newItem.getProductId())) {
                existingItem.setQuantity(existingItem.getQuantity() + newItem.getQuantity());
                count++;
                return;
            }
        }
        items.add(newItem);
        count++;
    }

    public void updateQuantity(String productId, int newQuantity) {
        for (BasketItem item : items) {
            if (item.getProductId().equals(productId)) {
                if (newQuantity <= 0) {
                    items.remove(item);
                    count--;
                } else {
                    item.setQuantity(newQuantity);
                }
                return;
            }
        }
    }

    public void removeItem(String productId) {
        for (BasketItem item : items) {
            if (item.getProductId().equals(productId)) {
                items.remove(item);
                count--;
                return;
            }
        }
    }
}