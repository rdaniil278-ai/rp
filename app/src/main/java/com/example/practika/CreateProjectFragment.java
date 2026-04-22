package com.example.practika;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

public class CreateProjectFragment extends Fragment {
    private static final String API_BASE = "http://2.nntc.nnov.ru:8900/api";
    private final OkHttpClient client = new OkHttpClient();

    // === Поля формы ===
    private EditText editProjectName, editDateStart, editDateEnd, editSize, editDescription;
    private TextView spinnerType;
    private LinearLayout dropdownType;
    private ImageView arrowType;
    private boolean isTypeDropdownOpen = false;
    private String selectedType = "";

    // === Для режима просмотра/редактирования ===
    private String projectId = null; // если не null → это просмотр существующего проекта
    private boolean isEditMode = false; // пока всегда false, т.к. бекенд не поддерживает PATCH

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Получаем projectId, если передали (просмотр существующего)
        if (getArguments() != null) {
            projectId = getArguments().getString("projectId");
            isEditMode = false; // бекенд не поддерживает редактирование, поэтому просто просмотр
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_project, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupDropdowns(view);
        setupConfirmButton(view);

        // Если это просмотр существующего проекта — загружаем с сервера
        if (projectId != null && isAdded()) {
            loadProjectFromServer();
        }
    }

    private void initViews(View view) {
        editProjectName = view.findViewById(R.id.editProjectName);
        editDateStart = view.findViewById(R.id.editDateStart);
        editDateEnd = view.findViewById(R.id.editDateEnd);
        editSize = view.findViewById(R.id.editTextSize);
        editDescription = view.findViewById(R.id.editSource);
        spinnerType = view.findViewById(R.id.spinnerType);
        dropdownType = view.findViewById(R.id.dropdownType);
        arrowType = view.findViewById(R.id.arrowType);
    }

    private void setupDropdowns(View view) {
        spinnerType.setOnClickListener(v -> toggleDropdown(
                dropdownType, arrowType, () -> isTypeDropdownOpen, open -> isTypeDropdownOpen = open));
        view.findViewById(R.id.typePersonal).setOnClickListener(v -> selectType("Личный"));
        view.findViewById(R.id.typeCommercial).setOnClickListener(v -> selectType("Коммерческий"));
        view.findViewById(R.id.typeEducational).setOnClickListener(v -> selectType("Учебный"));
    }

    private void toggleDropdown(LinearLayout dropdown, ImageView arrow,
                                java.util.function.Supplier<Boolean> isOpenGetter,
                                java.util.function.Consumer<Boolean> isOpenSetter) {
        if (isOpenGetter.get()) {
            collapseDropdown(dropdown, arrow);
            isOpenSetter.accept(false);
        } else {
            closeAllDropdownsExcept(dropdown);
            expandDropdown(dropdown, arrow);
            isOpenSetter.accept(true);
        }
    }

    private void expandDropdown(LinearLayout dropdown, ImageView arrow) {
        arrow.animate().rotation(180f).setDuration(300).start();
        dropdown.setVisibility(View.VISIBLE);
        Animation slideDown = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down);
        dropdown.startAnimation(slideDown);
    }

    private void collapseDropdown(LinearLayout dropdown, ImageView arrow) {
        arrow.animate().rotation(0f).setDuration(300).start();
        Animation slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up);
        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) { dropdown.setVisibility(View.GONE); }
            @Override public void onAnimationRepeat(Animation animation) {}
        });
        dropdown.startAnimation(slideUp);
    }

    private void closeAllDropdownsExcept(LinearLayout except) {
        if (dropdownType != except && isTypeDropdownOpen) {
            collapseDropdown(dropdownType, arrowType);
            isTypeDropdownOpen = false;
        }
    }

    private void selectType(String type) {
        spinnerType.setText(type);
        spinnerType.setTextColor(getResources().getColor(android.R.color.black, requireContext().getTheme()));
        selectedType = type;
        collapseDropdown(dropdownType, arrowType);
        isTypeDropdownOpen = false;
    }

    private void fillFieldsFromProject() {
        if (!isAdded() || getArguments() == null) return;

        String title = getArguments().getString("title", "");
        String type = getArguments().getString("type", "");
        String dateStart = getArguments().getString("dateStart", "");
        String dateEnd = getArguments().getString("dateEnd", "");
        String size = getArguments().getString("size", "");
        String description = getArguments().getString("description", "");

        if (editProjectName != null) {
            editProjectName.setText(title);
            // Явно устанавливаем черный цвет текста
            editProjectName.setTextColor(getResources().getColor(android.R.color.black, requireContext().getTheme()));
        }
        if (spinnerType != null) {
            spinnerType.setText(type);
            spinnerType.setTextColor(getResources().getColor(android.R.color.black, requireContext().getTheme()));
            selectedType = type;
        }
        if (editDateStart != null) {
            editDateStart.setText(dateStart);
            editDateStart.setTextColor(getResources().getColor(android.R.color.black, requireContext().getTheme()));
        }
        if (editDateEnd != null) {
            editDateEnd.setText(dateEnd);
            editDateEnd.setTextColor(getResources().getColor(android.R.color.black, requireContext().getTheme()));
        }
        if (editSize != null) {
            editSize.setText(size);
            editSize.setTextColor(getResources().getColor(android.R.color.black, requireContext().getTheme()));
        }
        if (editDescription != null) {
            editDescription.setText(description);
            editDescription.setTextColor(getResources().getColor(android.R.color.black, requireContext().getTheme()));
        }

        // Делаем поля только для чтения, если это просмотр
        if (!isEditMode) {
            if (editProjectName != null) editProjectName.setEnabled(false);
            if (editDateStart != null) editDateStart.setEnabled(false);
            if (editDateEnd != null) editDateEnd.setEnabled(false);
            if (editSize != null) editSize.setEnabled(false);
            if (editDescription != null) editDescription.setEnabled(false);
            if (spinnerType != null) spinnerType.setClickable(false);
        }
    }

    private void setupConfirmButton(View view) {
        Button btnConfirm = view.findViewById(R.id.btnConfirm);
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                if (projectId != null) {
                    //  Это просмотр существующего проекта → просто закрываем окно
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    //  Это создание нового проекта → отправляем на сервер
                    createProject();
                }
            });
        }
    }

    private void createProject() {
        String title = editProjectName.getText().toString().trim();
        String dateStart = editDateStart.getText().toString().trim();
        String dateEnd = editDateEnd.getText().toString().trim();
        String size = editSize.getText().toString().trim();
        String description = editDescription.getText().toString().trim();

        android.util.Log.d("CREATE_PROJECT", "=== createProject START ===");
        android.util.Log.d("CREATE_PROJECT", "title: " + title);
        android.util.Log.d("CREATE_PROJECT", "dateStart: " + dateStart);
        android.util.Log.d("CREATE_PROJECT", "dateEnd: " + dateEnd);
        android.util.Log.d("CREATE_PROJECT", "size: " + size);
        android.util.Log.d("CREATE_PROJECT", "description: " + description);
        android.util.Log.d("CREATE_PROJECT", "selectedType: " + selectedType);

        if (title.isEmpty()) {
            editProjectName.setError("Введите название");
            editProjectName.requestFocus();
            android.util.Log.d("CREATE_PROJECT", "Validation failed: title is empty");
            return;
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", requireActivity().MODE_PRIVATE);
        String token = prefs.getString("user_token", "");
        String userId = prefs.getString("user_id", "");

        android.util.Log.d("CREATE_PROJECT", "userId: " + userId);
        android.util.Log.d("CREATE_PROJECT", "token: " + (token.isEmpty() ? "EMPTY" : token.substring(0, Math.min(20, token.length())) + "..."));

        if (userId.isEmpty()) {
            Toast.makeText(requireContext(), "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            android.util.Log.e("CREATE_PROJECT", "userId is empty!");
            return;
        }

        // Формируем JSON для POST
        JSONObject json = new JSONObject();
        try {
            json.put("title", title);
            json.put("type", selectedType.isEmpty() ? "" : selectedType);
            json.put("date_start", dateStart.isEmpty() ? "" : dateStart);
            json.put("date_end", dateEnd.isEmpty() ? "" : dateEnd);
            json.put("size", size.isEmpty() ? "" : size);
            json.put("description_source", description.isEmpty() ? "" : description);
            json.put("user_id", userId);
        } catch (Exception e) {
            android.util.Log.e("CREATE_PROJECT", "JSON creation error: " + e.getMessage(), e);
            e.printStackTrace();
        }

        String jsonString = json.toString();
        android.util.Log.d("CREATE_PROJECT", "Request JSON: " + jsonString);

        RequestBody body = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));

        Request.Builder requestBuilder = new Request.Builder()
                .url(API_BASE + "/collections/projects/records")
                .post(body)
                .addHeader("Content-Type", "application/json");

        if (!token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
            android.util.Log.d("CREATE_PROJECT", "Authorization header added");
        }

        Request request = requestBuilder.build();
        android.util.Log.d("CREATE_PROJECT", "Request URL: " + request.url());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                android.util.Log.e("CREATE_PROJECT", "onFailure: " + e.getMessage(), e);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Ошибка сети: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body() != null ? response.body().string() : "";
                android.util.Log.d("CREATE_PROJECT", "HTTP Code: " + response.code());
                android.util.Log.d("CREATE_PROJECT", "Response Body: " + responseData);

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            android.util.Log.d("CREATE_PROJECT", "Project created successfully!");
                            Toast.makeText(requireContext(), "Проект создан!", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                            Fragment currentFragment = requireActivity().getSupportFragmentManager()
                                    .findFragmentById(R.id.fragmentContainer);
                            if (currentFragment instanceof ProjectsFragment) {
                                ((ProjectsFragment) currentFragment).refreshProjects();
                            }
                        } else {
                            android.util.Log.e("CREATE_PROJECT", "HTTP error: " + response.code());
                            try {
                                JSONObject errorJson = new JSONObject(responseData);
                                String message = errorJson.optString("message", "Ошибка создания");
                                android.util.Log.e("CREATE_PROJECT", "Error message: " + message);
                                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                android.util.Log.e("CREATE_PROJECT", "Error parsing error response: " + e.getMessage());
                                Toast.makeText(requireContext(), "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    public void closeAllDropdowns() {
        if (isTypeDropdownOpen) {
            collapseDropdown(dropdownType, arrowType);
            isTypeDropdownOpen = false;
        }
    }

    //  Статический метод для создания фрагмента просмотра проекта
    public static CreateProjectFragment newInstanceForView(String projectId, String title, String type,
                                                           String dateStart, String dateEnd,
                                                           String size, String description) {
        CreateProjectFragment fragment = new CreateProjectFragment();
        Bundle args = new Bundle();
        args.putString("projectId", projectId);
        args.putString("title", title);
        args.putString("type", type);
        args.putString("dateStart", dateStart);
        args.putString("dateEnd", dateEnd);
        args.putString("size", size);
        args.putString("description", description);
        fragment.setArguments(args);
        return fragment;
    }
    private void loadProjectFromServer() {
        if (projectId == null || !isAdded()) return;

        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", requireActivity().MODE_PRIVATE);
        String token = prefs.getString("user_token", "");

        android.util.Log.d("PROJECT_VIEW", "=== loadProjectFromServer START ===");
        android.util.Log.d("PROJECT_VIEW", "projectId: " + projectId);
        android.util.Log.d("PROJECT_VIEW", "token: " + (token.isEmpty() ? "EMPTY" : token.substring(0, Math.min(20, token.length())) + "..."));

        Request.Builder requestBuilder = new Request.Builder()
                .url(API_BASE + "/collections/projects/records/" + projectId)
                .addHeader("Accept", "application/json");

        if (!token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        Request request = requestBuilder.build();
        android.util.Log.d("PROJECT_VIEW", "Request URL: " + request.url());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                android.util.Log.e("PROJECT_VIEW", "onFailure: " + e.getMessage(), e);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Ошибка загрузки проекта", Toast.LENGTH_SHORT).show();
                        // Если не удалось загрузить, используем данные из аргументов
                        fillFieldsFromProject();
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body() != null ? response.body().string() : "";
                android.util.Log.d("PROJECT_VIEW", "HTTP Code: " + response.code());
                android.util.Log.d("PROJECT_VIEW", "Response Body: " + responseData);

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject json = new JSONObject(responseData);
                                fillFieldsFromJson(json);
                                android.util.Log.d("PROJECT_VIEW", "Project loaded successfully!");
                            } catch (JSONException e) {
                                android.util.Log.e("PROJECT_VIEW", "JSON parse error: " + e.getMessage(), e);
                                fillFieldsFromProject();
                            }
                        } else {
                            android.util.Log.e("PROJECT_VIEW", "HTTP error: " + response.code());
                            fillFieldsFromProject();
                        }
                    });
                }
            }
        });
    }

    private void fillFieldsFromJson(JSONObject json) {
        if (!isAdded()) return;

        String title = json.optString("title", "");
        String type = json.optString("type", "");
        String dateStart = json.optString("date_start", "");
        String dateEnd = json.optString("date_end", "");
        String size = json.optString("size", "");
        String description = json.optString("description_source", "");

        android.util.Log.d("PROJECT_VIEW", "Filling fields from JSON:");
        android.util.Log.d("PROJECT_VIEW", "title: " + title);
        android.util.Log.d("PROJECT_VIEW", "type: " + type);
        android.util.Log.d("PROJECT_VIEW", "dateStart: " + dateStart);
        android.util.Log.d("PROJECT_VIEW", "dateEnd: " + dateEnd);
        android.util.Log.d("PROJECT_VIEW", "size: " + size);
        android.util.Log.d("PROJECT_VIEW", "description: " + description);

        fillFields(title, type, dateStart, dateEnd, size, description);
    }

    private void fillFields(String title, String type, String dateStart, String dateEnd, String size, String description) {
        if (editProjectName != null) {
            editProjectName.setText(title);
            // Явно устанавливаем черный цвет текста
            editProjectName.setTextColor(getResources().getColor(android.R.color.black, requireContext().getTheme()));
        }
        if (spinnerType != null) {
            spinnerType.setText(type);
            spinnerType.setTextColor(getResources().getColor(android.R.color.black, requireContext().getTheme()));
            selectedType = type;
        }
        if (editDateStart != null) {
            editDateStart.setText(dateStart);
            editDateStart.setTextColor(getResources().getColor(android.R.color.black, requireContext().getTheme()));
        }
        if (editDateEnd != null) {
            editDateEnd.setText(dateEnd);
            editDateEnd.setTextColor(getResources().getColor(android.R.color.black, requireContext().getTheme()));
        }
        if (editSize != null) {
            editSize.setText(size);
            editSize.setTextColor(getResources().getColor(android.R.color.black, requireContext().getTheme()));
        }
        if (editDescription != null) {
            editDescription.setText(description);
            editDescription.setTextColor(getResources().getColor(android.R.color.black, requireContext().getTheme()));
        }

        // Делаем поля только для чтения, если это просмотр
        if (!isEditMode) {
            if (editProjectName != null) editProjectName.setEnabled(false);
            if (editDateStart != null) editDateStart.setEnabled(false);
            if (editDateEnd != null) editDateEnd.setEnabled(false);
            if (editSize != null) editSize.setEnabled(false);
            if (editDescription != null) editDescription.setEnabled(false);
            if (spinnerType != null) spinnerType.setClickable(false);
        }
    }

}