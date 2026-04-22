package com.example.practika;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashActivity extends AppCompatActivity {

    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_PIN_HASH = "pin_hash";

    // Время показа заставки в миллисекундах (2000 = 2 секунды)
    private static final int SPLASH_DELAY = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean pinExists = prefs.contains(KEY_PIN_HASH);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        // Настройка отступов под системные панели (если нужно)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Запускаем таймер перехода
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Переход на экран авторизации
//            Intent intent = new Intent(SplashActivity.this, AuthActivity.class);
//            startActivity(intent);
            if (pinExists) {
                // Уже есть PIN - ввод PIN
                startActivity(new Intent(this, EnterPinActivity.class));
            } else {
                // Первый запуск - регистрация
                startActivity(new Intent(this, AuthActivity.class));
            }
            finish();
        }, SPLASH_DELAY);
    }
}