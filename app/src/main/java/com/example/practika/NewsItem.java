package com.example.practika;

import org.json.JSONObject;

public class NewsItem {
    private String id;
    private String newsImage;
    private String collectionId;
    private String collectionName;
    private String created;
    private String updated;

    public NewsItem(JSONObject json) {
        try {
            this.id = json.getString("id");
            this.newsImage = json.optString("newsImage", "");
            this.collectionId = json.optString("collectionId", "");
            this.collectionName = json.optString("collectionName", "");
            this.created = json.optString("created", "");
            this.updated = json.optString("updated", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getId() { return id; }
    public String getNewsImage() { return newsImage; }
    public String getCollectionId() { return collectionId; }

    // URL для загрузки изображения из PocketBase
    public String getImageUrl() {
        if (newsImage != null && !newsImage.isEmpty() && collectionId != null && id != null) {
            return "http://2.nntc.nnov.ru:8900/api/files/" + collectionId + "/" + id + "/" + newsImage;
        }
        return null;
    }
}