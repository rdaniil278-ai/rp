package com.example.practika;

import org.json.JSONObject;

public class Product {
    private String id;
    private String collectionId;
    private String collectionName;
    private String created;
    private String updated;
    private String title;
    private int price;
    private String typeCloses;
    private String type;
    private String description;
    private String approximateCost;

    //  Дефолтный конструктор (добавлен)
    public Product() {
        this.id = "";
        this.title = "";
        this.price = 0;
        this.type = "";
        this.description = "";
        this.approximateCost = "";
    }

    public Product(JSONObject json) {
        try {
            this.id = json.getString("id");
            this.collectionId = json.optString("collectionId", "");
            this.collectionName = json.optString("collectionName", "");
            this.created = json.optString("created", "");
            this.updated = json.optString("updated", "");
            this.title = json.optString("title", "");
            this.price = json.optInt("price", 0);
            this.typeCloses = json.optString("typeCloses", "");
            this.type = json.optString("type", "");
            this.description = json.optString("description", "");
            this.approximateCost = json.optString("approximate_cost", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCollectionId() { return collectionId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public String getType() { return type; }
    public String getTypeCloses() { return typeCloses; }
    public String getDescription() { return description; }
    public String getApproximateCost() { return approximateCost; }

    public String getFormattedPrice() {
        return price + " ₽";
    }
}