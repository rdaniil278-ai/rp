package com.example.practika;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProfileFragment extends Fragment {

    private static final String TAG = "PROFILE_DEBUG";

    private TextView textName;
    private TextView textEmail;
    private SwitchMaterial switchNotifications;
    private TextView textLogout;

    private final OkHttpClient client = new OkHttpClient();
    private static final String API_BASE = "http://2.nntc.nnov.ru:8900/api";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupNotificationSwitch();
        setupLogout();
        loadUserData();
    }

    private void initViews(View view) {
        textName = view.findViewById(R.id.textName);
        textEmail = view.findViewById(R.id.textEmail);
        switchNotifications = view.findViewById(R.id.switch_notifications);
        textLogout = view.findViewById(R.id.textLogout); //  Инициализируем здесь
    }

    private void loadUserData() {
        if (requireActivity().isFinishing() || !isAdded()) return;

        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", requireActivity().MODE_PRIVATE);
        String userId = prefs.getString("user_id", "");
        String token = prefs.getString("user_token", "");

        if (userId.isEmpty() || token.isEmpty()) {
            android.util.Log.w(TAG, "loadUserData: No user_id or token");
            textName.setText("Пользователь");
            textEmail.setText("email@example.com");
            return;
        }

        android.util.Log.d(TAG, "loadUserData: Fetching userId=[" + userId + "]");

        Request request = new Request.Builder()
                .url(API_BASE + "/collections/users/records/" + userId)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                android.util.Log.e(TAG, "loadUserData: Network error", e);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        textName.setText("Ошибка загрузки");
                        textEmail.setText("Проверьте соединение");
                        Toast.makeText(requireContext(), "Не удалось загрузить профиль", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body() != null ? response.body().string() : "";
                android.util.Log.d(TAG, "loadUserData: HTTP " + response.code() + " | " + responseData);

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject json = new JSONObject(responseData);
                                String firstName = json.optString("first_name", "");
                                String lastName = json.optString("last_name", "");
                                String email = json.optString("email", "email@example.com");

                                String fullName = (firstName + " " + lastName).trim();
                                if (fullName.isEmpty()) fullName = "Пользователь";

                                textName.setText(fullName);
                                textEmail.setText(email);
                                android.util.Log.i(TAG, "Success | Name=[" + fullName + "]");
                            } catch (JSONException e) {
                                android.util.Log.e(TAG, "JSON parse error", e);
                                Toast.makeText(requireContext(), "Ошибка обработки данных", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            android.util.Log.e(TAG, "HTTP error " + response.code());
                            Toast.makeText(requireContext(), "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void setupNotificationSwitch() {
        // Загружаем сохранённое состояние
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", requireActivity().MODE_PRIVATE);
        // изначально включаем по дефолту в shared preferences
        if (!prefs.contains("notifications_enabled")) {
            prefs.edit().putBoolean("notifications_enabled", true).apply();
        }

        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
        switchNotifications.setChecked(notificationsEnabled);

        // Обработчик переключения
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(requireContext(), "Уведомления включены", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(requireContext(), "Уведомления выключены", Toast.LENGTH_SHORT).show();

            }

            // Сохраняем состояние
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
        });
    }

    private void setupLogout() {
        if (textLogout != null) {
            textLogout.setOnClickListener(v -> {
                SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", requireActivity().MODE_PRIVATE);
                prefs.edit().clear().apply();

                Toast.makeText(requireContext(), "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(requireActivity(), AuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            });
        }
    }
}