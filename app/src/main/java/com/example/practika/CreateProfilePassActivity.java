package com.example.practika;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class CreateProfilePassActivity extends AppCompatActivity {

    private static final String TAG = "REG_DEBUG";

    private EditText passField;
    private EditText passwordField;
    private Button buttonNext;

    private boolean isPassFieldVisible = false;
    private boolean isConfirmFieldVisible = false;
    private boolean hasShownPasswordError = false;

    private String regName, regPatronymic, regSurname, regBirthday, regGender, regEmail;

    private final OkHttpClient client = new OkHttpClient();
    private static final String API_BASE = "http://2.nntc.nnov.ru:8900/api";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile_pass);

        Intent intent = getIntent();
        regName = intent.getStringExtra("name");
        regPatronymic = intent.getStringExtra("patronymic");
        regSurname = intent.getStringExtra("surname");
        regBirthday = intent.getStringExtra("birthday");
        regGender = intent.getStringExtra("gender");
        regEmail = intent.getStringExtra("email");

        initViews();
        setupTextWatchers();
        setupEyeIcons();
        setupFieldFocusListeners();
        setupSaveButton();
        updateButtonState();
    }

    private void initViews() {
        passField = findViewById(R.id.passField);
        passwordField = findViewById(R.id.passwordField);
        buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setEnabled(false);
        buttonNext.setBackgroundResource(R.drawable.shape_rounded_btn_disable);
    }

    private void setupTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateEyeIcon(passField, isPassFieldVisible);
                if (hasShownPasswordError && !TextUtils.isEmpty(s)) hasShownPasswordError = false;
                updateButtonState();
            }
        };

        TextWatcher confirmWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateEyeIcon(passwordField, isConfirmFieldVisible);
                updateButtonState();
            }
        };

        passField.addTextChangedListener(watcher);
        passwordField.addTextChangedListener(confirmWatcher);
    }

    private void setupFieldFocusListeners() {
        passwordField.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                String password = passField.getText().toString();
                if (!TextUtils.isEmpty(password) && !isValidPassword(password) && !hasShownPasswordError) {
                    String error = getPasswordErrorMessage(password);
                    Toast.makeText(CreateProfilePassActivity.this, error, Toast.LENGTH_LONG).show();
                    hasShownPasswordError = true;
                }
            }
        });
    }

    private void setupEyeIcons() {
        passField.setOnTouchListener(createEyeTouchListener(passField, () -> isPassFieldVisible, v -> isPassFieldVisible = v));
        passwordField.setOnTouchListener(createEyeTouchListener(passwordField, () -> isConfirmFieldVisible, v -> isConfirmFieldVisible = v));
    }

    private View.OnTouchListener createEyeTouchListener(EditText editText, java.util.function.Supplier<Boolean> getState, java.util.function.Consumer<Boolean> setState) {
        return (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                var drawables = editText.getCompoundDrawables();
                if (drawables[2] != null) {
                    int iconLeft = editText.getRight() - editText.getPaddingRight() - drawables[2].getBounds().width();
                    int iconRight = editText.getRight() - editText.getPaddingRight();
                    if (event.getRawX() >= iconLeft && event.getRawX() <= iconRight) {
                        togglePasswordVisibility(editText, R.drawable.ic_eye_close, R.drawable.ic_eye_open, getState.get(), setState);
                        return true;
                    }
                }
            }
            return false;
        };
    }

    private void togglePasswordVisibility(EditText editText, int iconClosed, int iconOpen, boolean currentState, java.util.function.Consumer<Boolean> onStateChange) {
        if (currentState) {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconClosed, 0);
        } else {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconOpen, 0);
        }
        editText.setSelection(editText.getText().length());
        onStateChange.accept(!currentState);
    }

    private void updateEyeIcon(EditText editText, boolean isVisible) {
        String text = editText.getText().toString();
        if (TextUtils.isEmpty(text)) {
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            int icon = isVisible ? R.drawable.ic_eye_open : R.drawable.ic_eye_close;
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0);
        }
    }

    private void updateButtonState() {
        String password = passField.getText().toString();
        String confirmPassword = passwordField.getText().toString();

        if (!TextUtils.isEmpty(password) && !isValidPassword(password)) {
            setButtonDisabled();
            return;
        }
        if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)) {
            if (!password.equals(confirmPassword)) {
                setButtonDisabled();
                return;
            }
            setButtonEnabled();
        } else {
            setButtonDisabled();
        }
    }

    private void setButtonEnabled() {
        buttonNext.setEnabled(true);
        buttonNext.setBackgroundResource(R.drawable.shape_rounded_btn_enable);
    }

    private void setButtonDisabled() {
        buttonNext.setEnabled(false);
        buttonNext.setBackgroundResource(R.drawable.shape_rounded_btn_disable);
    }

    private void setupSaveButton() {
        buttonNext.setOnClickListener(v -> {
            String password = passField.getText().toString().trim();
            String confirmPassword = passwordField.getText().toString().trim();

            if (!isValidPassword(password)) {
                String error = getPasswordErrorMessage(password);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                passField.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
                passwordField.requestFocus();
                return;
            }

            registerUser(regEmail, password);
        });
    }

    private void registerUser(String email, String password) {
        android.util.Log.d(TAG, ">>> registerUser START | email=[" + email + "] password=[" + password + "]");
        buttonNext.setEnabled(false);

        String json = "{"
                + "\"email\":\"" + escapeJson(email) + "\","
                + "\"password\":\"" + escapeJson(password) + "\","
                + "\"passwordConfirm\":\"" + escapeJson(password) + "\""
                + "}";

        android.util.Log.d(TAG, "POST /collections/users/records | JSON: " + json);

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_BASE + "/collections/users/records")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                android.util.Log.e(TAG, "registerUser: NETWORK FAILURE | " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(CreateProfilePassActivity.this, "Ошибка сети: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    buttonNext.setEnabled(true);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body() != null ? response.body().string() : "";
                android.util.Log.d(TAG, "registerUser: HTTP " + response.code() + " | Response: " + responseData);

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonResp = new JSONObject(responseData);
                            String userId = jsonResp.getString("id");
                            android.util.Log.i(TAG, "registerUser SUCCESS | userId=[" + userId + "] | Full: " + jsonResp.toString());
                            authenticateUser(email, password, userId);
                        } catch (JSONException e) {
                            android.util.Log.e(TAG, "registerUser: JSON PARSE ERROR | " + e.getMessage());
                            Toast.makeText(CreateProfilePassActivity.this, "Ошибка парсинга: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            buttonNext.setEnabled(true);
                        }
                    } else {
                        android.util.Log.e(TAG, "registerUser: HTTP ERROR " + response.code() + " | " + responseData);
                        if (responseData.contains("email") && responseData.contains("unique")) {
                            Toast.makeText(CreateProfilePassActivity.this, "Email уже зарегистрирован", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(CreateProfilePassActivity.this, "Ошибка: " + response.code(), Toast.LENGTH_LONG).show();
                        }
                        buttonNext.setEnabled(true);
                    }
                });
            }
        });
    }

    private void authenticateUser(String email, String password, String userId) {
        android.util.Log.d(TAG, ">>> authenticateUser START | email=[" + email + "] password=[" + password + "] userId=[" + userId + "]");

        String json = "{\"identity\":\"" + escapeJson(email) + "\",\"password\":\"" + escapeJson(password) + "\"}";
        android.util.Log.d(TAG, "POST /collections/users/auth-with-password | JSON: " + json);

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_BASE + "/collections/users/auth-with-password")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                android.util.Log.e(TAG, "authenticateUser: NETWORK FAILURE | " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(CreateProfilePassActivity.this, "Ошибка авторизации: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    updateProfile(userId, "");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body() != null ? response.body().string() : "";
                android.util.Log.d(TAG, "authenticateUser: HTTP " + response.code() + " | Response: " + responseData);

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonResp = new JSONObject(responseData);
                            String token = jsonResp.getString("token");
                            android.util.Log.i(TAG, "authenticateUser SUCCESS | token=[" + token + "] | Full: " + jsonResp.toString());
                            // сохранение в sharedpreferences для дальнейшей загрузки через GET запрос в профиле
                            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                            prefs.edit()
                                    .putString("user_token", token)
                                    .putString("user_id", userId)
                                    .putString("user_email", email)
                                    .putBoolean("is_logged_in", true)
                                    .apply();

                            updateProfile(userId, token);
                        } catch (JSONException e) {
                            android.util.Log.e(TAG, "authenticateUser: JSON PARSE ERROR | " + e.getMessage());
                            Toast.makeText(CreateProfilePassActivity.this, "Ошибка токена: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            updateProfile(userId, "");
                        }
                    } else {
                        android.util.Log.e(TAG, "authenticateUser: HTTP ERROR " + response.code() + " | " + responseData);
                        Toast.makeText(CreateProfilePassActivity.this, "Не удалось получить токен", Toast.LENGTH_LONG).show();
                        updateProfile(userId, "");
                    }
                });
            }
        });
    }

    private void updateProfile(String userId, String token) {
        android.util.Log.d(TAG, ">>> updateProfile START | userId=[" + userId + "] token=[" + token + "]");

        String formattedBirthday = formatDateForApi(regBirthday);
        android.util.Log.d(TAG, "Birthday formatted: [" + regBirthday + "] -> [" + formattedBirthday + "]");

        String json = "{"
                + "\"first_name\":\"" + escapeJson(regName) + "\","
                + "\"last_name\":\"" + escapeJson(regSurname) + "\","
                + "\"patronymic\":\"" + escapeJson(regPatronymic) + "\","
                + "\"birthday\":\"" + escapeJson(formattedBirthday) + "\","
                + "\"gender\":\"" + escapeJson(regGender) + "\""
                + "}";

        android.util.Log.d(TAG, "PATCH /collections/users/records/" + userId + " | JSON: " + json);

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request.Builder requestBuilder = new Request.Builder()
                .url(API_BASE + "/collections/users/records/" + userId)
                .patch(body)
                .addHeader("Content-Type", "application/json");

        if (!TextUtils.isEmpty(token)) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        Request request = requestBuilder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                android.util.Log.e(TAG, "updateProfile: NETWORK FAILURE | " + e.getMessage());
                runOnUiThread(() -> {
                    android.util.Log.w(TAG, "updateProfile failed, but proceeding");
                    proceedToNextScreen();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body() != null ? response.body().string() : "";
                android.util.Log.d(TAG, "updateProfile: HTTP " + response.code() + " | Response: " + responseData);

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        android.util.Log.i(TAG, "updateProfile SUCCESS!");
                        Toast.makeText(CreateProfilePassActivity.this, "Профиль создан!", Toast.LENGTH_SHORT).show();
                    } else {
                        android.util.Log.w(TAG, "updateProfile failed with code " + response.code());
                        Toast.makeText(CreateProfilePassActivity.this, "Аккаунт создан!", Toast.LENGTH_SHORT).show();
                    }
                    proceedToNextScreen();
                });
            }
        });
    }

    private void proceedToNextScreen() {
        android.util.Log.i(TAG, ">>> proceedToNextScreen | email=[" + regEmail + "]");

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("is_registered", true)
                .apply();

        Intent intent = new Intent(CreateProfilePassActivity.this, CreatePinActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

        android.util.Log.i(TAG, "=== REGISTRATION FLOW COMPLETE ===");
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecialOrSpace = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (c == ' ' || !Character.isLetterOrDigit(c)) hasSpecialOrSpace = true;
        }
        return hasUpper && hasLower && hasDigit && hasSpecialOrSpace;
    }

    private String getPasswordErrorMessage(String password) {
        if (password == null || password.isEmpty()) return "Введите пароль";
        if (password.length() < 8) return "Минимум 8 символов";
        if (!password.matches(".*[A-Z].*")) return "Добавьте заглавную букву (A-Z)";
        if (!password.matches(".*[a-z].*")) return "Добавьте строчную букву (a-z)";
        if (!password.matches(".*\\d.*")) return "Добавьте цифру (0-9)";
        if (!password.matches(".*[\\s\\W].*")) return "Добавьте спецсимвол или пробел";
        return "";
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private String formatDateForApi(String date) {
        if (date == null || date.isEmpty()) {
            return "";
        }

        date = date.trim();

        if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return date;
        }

        date = date.replace("/", ".").replace("-", ".");

        String[] parts = date.split("\\.");
        if (parts.length == 3) {
            String day = parts[0];
            String month = parts[1];
            String year = parts[2];

            if (year.length() == 2) {
                year = "20" + year;
            }

            return String.format("%s-%02d-%02d",
                    year,
                    Integer.parseInt(month),
                    Integer.parseInt(day));
        }

        return date;
    }
}
