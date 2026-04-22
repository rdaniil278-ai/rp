package com.example.practika;

import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ProjectItem {
    private String id;
    private String title;
    private String created;
    private String type;
    private String dateStart;
    private String dateEnd;
    private String size;
    private String descriptionSource;
    private String technicalDrawing;

    public ProjectItem(JSONObject json) {
        try {
            this.id = json.getString("id");
            this.title = json.optString("title", "Без названия");
            this.created = json.optString("created", "");
            this.type = json.optString("type", "");
            this.dateStart = json.optString("date_start", "");
            this.dateEnd = json.optString("date_end", "");
            this.size = json.optString("size", "");
            this.descriptionSource = json.optString("description_source", "");
            this.technicalDrawing = json.optString("technical_drawing", "");

            android.util.Log.d("PROJECT_ITEM", "Created project: id=" + id +
                    ", title=" + title +
                    ", created=" + created +
                    ", rawJson=" + json.toString());
        } catch (Exception e) {
            android.util.Log.e("PROJECT_ITEM", "Error parsing: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCreated() { return created; }
    public String getType() { return type; }
    public String getDateStart() { return dateStart; }
    public String getDateEnd() { return dateEnd; }
    public String getSize() { return size; }
    public String getDescriptionSource() { return descriptionSource; }
    public String getTechnicalDrawing() { return technicalDrawing; }

    public String getDaysPassed() {
        if (created == null || created.isEmpty()) {
            return "Дата не указана";
        }
        try {
            // Пробуем парсить с пробелом (формат от сервера)
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'", Locale.getDefault());
            sdf1.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date createdDate = sdf1.parse(created);

            if (createdDate == null) return "Дата не указана";

            long now = System.currentTimeMillis();
            long diffMillis = now - createdDate.getTime();
            long days = diffMillis / (1000 * 60 * 60 * 24);

            android.util.Log.d("PROJECT_ITEM", "getDaysPassed: created=" + created +
                    ", createdDate=" + createdDate +
                    ", now=" + new Date(now) +
                    ", diffDays=" + days);

            if (days < 0) return "Создан сегодня";
            if (days == 0) return "Создан сегодня";
            if (days == 1) return "Прошёл 1 день";
            if (days >= 2 && days <= 4) return "Прошло " + days + " дня";
            return "Прошло " + days + " дней";
        } catch (ParseException e) {
            android.util.Log.e("PROJECT_ITEM", "Parse error: " + e.getMessage() + " for date: " + created);
            return "Дата не указана";
        }
    }
}