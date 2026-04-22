package com.example.practika;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CreatePinActivity extends AppCompatActivity {

    private String enteredPin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_create);

        initViews();
        setupNumberButtons();
        setupDeleteButton();
    }

    private void initViews() {
        // Список ID кнопок цифр
        int[] numberButtonIds = {
                R.id.num0, R.id.num1, R.id.num2, R.id.num3,
                R.id.num4, R.id.num5, R.id.num6, R.id.num7,
                R.id.num8, R.id.num9
        };

        // Список ID кружков
        int[] circleIds = {R.id.circle1, R.id.circle2, R.id.circle3, R.id.circle4};
    }

    private void setupNumberButtons() {
        int[] numberButtonIds = {
                R.id.num0, R.id.num1, R.id.num2, R.id.num3,
                R.id.num4, R.id.num5, R.id.num6, R.id.num7,
                R.id.num8, R.id.num9
        };

        for (int id : numberButtonIds) {
            Button button = findViewById(id);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNumberClick((Button) v);
                }
            });
        }
    }

    private void setupDeleteButton() {
        ImageButton deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enteredPin.length() > 0) {
                    enteredPin = enteredPin.substring(0, enteredPin.length() - 1);
                    updateCircles();
                    resetAllButtons();
                }
            }
        });
    }

    private void onNumberClick(Button button) {
        if (enteredPin.length() < 4) {
            String number = button.getText().toString();
            enteredPin += number;

            // Анимация нажатия
            button.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(10)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            // Синий фон
                            button.setBackgroundResource(R.drawable.number_background_pressed);

                            // Анимация отпускания + сброс фона
                            button.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            // СБРОС на обычный фон через
                                            button.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    button.setBackgroundResource(R.drawable.number_background);
                                                }
                                            }, 200);
                                        }
                                    })
                                    .start();
                        }
                    })
                    .start();

            updateCircles();

            if (enteredPin.length() == 4) {
                onPinComplete();
            }
        }
    }


    private void updateCircles() {
        int[] circleIds = {R.id.circle1, R.id.circle2, R.id.circle3, R.id.circle4};

        for (int i = 0; i < circleIds.length; i++) {
            ImageView circle = findViewById(circleIds[i]);
            if (i < enteredPin.length()) {
                // Заполненный кружок
                circle.setImageResource(R.drawable.ic_circle_filled);
            } else {
                // Пустой кружок
                circle.setImageResource(R.drawable.ic_circle);
            }
        }
    }

    private void resetAllButtons() {
        int[] numberButtonIds = {
                R.id.num0, R.id.num1, R.id.num2, R.id.num3,
                R.id.num4, R.id.num5, R.id.num6, R.id.num7,
                R.id.num8, R.id.num9
        };

        for (int id : numberButtonIds) {
            Button button = findViewById(id);
            button.setBackgroundResource(R.drawable.number_background);
        }
    }

//    private void onPinComplete() {
//        Toast.makeText(this, "PIN создан: " + enteredPin, Toast.LENGTH_SHORT).show();
//
//        // Переход на следующую активность
//         Intent intent = new Intent(CreatePasswordActivity.this, CreateCardPatient.class);
//         startActivity(intent);
//         finish();
//    }



    private void onPinComplete() {
        // Сохраняем PIN
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("pin_hash", enteredPin)
                .apply();

        Toast.makeText(this, "PIN сохранён", Toast.LENGTH_SHORT).show();

        // Переход к созданию карты пациента
        Intent intent = new Intent(CreatePinActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
