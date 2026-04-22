package com.example.practika;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthActivity extends AppCompatActivity {

    private EditText passwordField;
    private EditText emailField;
    private Button buttonNext;
    private TextView textReg;

    private boolean isEmailValid = false;
    private boolean isPasswordNotEmpty = false;
    private boolean hasShownEmailError = false; //  Флаг: показана ли уже ошибка email

    private final OkHttpClient client = new OkHttpClient();
    private static final String API_BASE = "http://2.nntc.nnov.ru:8900/api";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        initViews();
        setupTextWatchers();
        setupEmailFocusListener(); //  Новый обработчик фокуса для email
        setupEyeIcon();
        setupClickListeners();
        updateNextButtonState();
    }

    private void initViews() {
        passwordField = findViewById(R.id.passwordField);
        emailField = findViewById(R.id.passField);
        buttonNext = findViewById(R.id.buttonNext);
        textReg = findViewById(R.id.textReg);
        buttonNext.setEnabled(false);
    }

    private void setupTextWatchers() {
        TextWatcher emailWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                isEmailValid = !TextUtils.isEmpty(email) &&
                        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();

                // Сбрасываем флаг ошибки, если пользователь начал исправлять email
                if (hasShownEmailError && !TextUtils.isEmpty(s)) {
                    hasShownEmailError = false;
                    emailField.setError(null);
                }

                updateNextButtonState();
            }
        };

        TextWatcher passWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString();
                if (TextUtils.isEmpty(password)) {
                    passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                } else {
                    passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_close, 0);
                }
                isPasswordNotEmpty = !TextUtils.isEmpty(password);
                passwordField.setError(null);
                updateNextButtonState();
            }
        };

        emailField.addTextChangedListener(emailWatcher);
        passwordField.addTextChangedListener(passWatcher);
    }


    private void setupEmailFocusListener() {
        passwordField.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Пользователь кликнул на поле пароля — проверяем email
                String email = emailField.getText().toString().trim();
                if (!TextUtils.isEmpty(email) && !isEmailValid && !hasShownEmailError) {
                    emailField.setError("Введите корректный email");
                    emailField.requestFocus();
                    hasShownEmailError = true;
                }
            }
        });
    }

    private void setupEyeIcon() {
        passwordField.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                var drawables = passwordField.getCompoundDrawables();
                if (drawables[2] != null) {
                    int iconLeft = passwordField.getRight() - passwordField.getPaddingRight() - drawables[2].getBounds().width();
                    int iconRight = passwordField.getRight() - passwordField.getPaddingRight();
                    if (event.getRawX() >= iconLeft && event.getRawX() <= iconRight) {
                        togglePasswordVisibility();
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private void togglePasswordVisibility() {
        int inputType = passwordField.getInputType();
        boolean isVisible = (inputType & android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;

        if (isVisible) {
            passwordField.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_close, 0);
        } else {
            passwordField.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                    android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0);
        }
        passwordField.setSelection(passwordField.getText().length());
    }

    private void setupClickListeners() {
        textReg.setOnClickListener(v -> {
            Intent intent = new Intent(AuthActivity.this, CreateProfileActivity.class);
            startActivity(intent);
        });

        buttonNext.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString();

            authenticateUser(email, password);
        });
    }

    private void authenticateUser(String email, String password) {
        buttonNext.setEnabled(false);

        String json = "{\"identity\":\"" + escapeJson(email) + "\",\"password\":\"" + escapeJson(password) + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_BASE + "/collections/users/auth-with-password")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(AuthActivity.this, "Ошибка сети: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    android.util.Log.e("AUTH_ERROR", "Network failure: " + e.getMessage(), e);
                    buttonNext.setEnabled(true);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body() != null ? response.body().string() : "";
                android.util.Log.d("AUTH_RESPONSE", "Code: " + response.code());
                android.util.Log.d("AUTH_RESPONSE", "Body: " + responseData);

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonResp = new JSONObject(responseData);
                            String token = jsonResp.getString("token");
                            JSONObject record = jsonResp.getJSONObject("record");
                            String userId = record.getString("id");

                            saveAuthData(token, userId, email);

                            Toast.makeText(AuthActivity.this, "Успешный вход!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(AuthActivity.this, CreatePinActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                        } catch (JSONException e) {
                            Toast.makeText(AuthActivity.this, "Ошибка парсинга ответа", Toast.LENGTH_SHORT).show();
                            android.util.Log.e("AUTH_ERROR", "HTTP " + response.code() + ": " + responseData);
                            e.printStackTrace();
                        }
                    } else {
                        //  Обработка ошибки авторизации от сервера
                        if (response.code() == 400 || response.code() == 401) {
                            // PocketBase возвращает 400/401 при неверных данных
                            Toast.makeText(AuthActivity.this, "Неверный email или пароль", Toast.LENGTH_LONG).show();

                            //  Подсвечиваем оба поля, чтобы пользователь понял, что проверять
                            emailField.setError(null); // Сбрасываем, чтобы не мешало
                            passwordField.setError("Неверный пароль");
                            passwordField.requestFocus();
                        } else {
                            Toast.makeText(AuthActivity.this, "Ошибка: " + response.code(), Toast.LENGTH_LONG).show();
                        }
                        android.util.Log.e("AUTH_ERROR", "HTTP " + response.code() + ": " + responseData);
                    }
                    buttonNext.setEnabled(true);
                });
            }
        });
    }

    private void saveAuthData(String token, String userId, String email) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("user_token", token)
                .putString("user_id", userId)
                .putString("user_email", email)
                .putBoolean("is_logged_in", true)
                .apply();
    }

    private void updateNextButtonState() {
        if (isEmailValid && isPasswordNotEmpty) {
            buttonNext.setEnabled(true);
            buttonNext.setBackgroundResource(R.drawable.shape_rounded_btn_enable);
        } else {
            buttonNext.setEnabled(false);
            buttonNext.setBackgroundResource(R.drawable.shape_rounded_btn_disable);
        }
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}