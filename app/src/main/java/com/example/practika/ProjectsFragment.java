package com.example.practika;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProjectsFragment extends Fragment {
    private static final String API_BASE = "http://2.nntc.nnov.ru:8900/api";
    private final OkHttpClient client = new OkHttpClient();

    private RecyclerView projectsRecyclerView;
    private ProjectAdapter projectAdapter;
    private List<ProjectItem> projectList = new ArrayList<>();
    private TextView textNoProjects;
    private ImageView imagePlus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_projects, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupPlusButton();
        initRecyclerView();
        loadProjects();
    }

    private void initViews(View view) {
        textNoProjects = view.findViewById(R.id.textNoProjects);
        imagePlus = view.findViewById(R.id.imagePlus);
    }

    private void setupPlusButton() {
        if (imagePlus != null) {
            imagePlus.setOnClickListener(v -> {
                // Переход на фрагмент создания проекта
                CreateProjectFragment createProjectFragment = new CreateProjectFragment();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, createProjectFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }
    }


    private void initRecyclerView() {
        projectsRecyclerView = getView().findViewById(R.id.projectsRecyclerView);
        projectsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        projectAdapter = new ProjectAdapter(projectList, project -> {
            // Передаем все данные проекта во фрагмент просмотра
            CreateProjectFragment viewFragment = CreateProjectFragment.newInstanceForView(
                    project.getId(),
                    project.getTitle(),
                    project.getType(),
                    project.getDateStart(),
                    project.getDateEnd(),
                    project.getSize(),
                    project.getDescriptionSource()
            );
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, viewFragment)
                    .addToBackStack(null)
                    .commit();
        });

        projectsRecyclerView.setAdapter(projectAdapter);
    }

// ... остальной код без изменений ...

    private void loadProjects() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", requireActivity().MODE_PRIVATE);
        String token = prefs.getString("user_token", "");
        String userId = prefs.getString("user_id", "");

        android.util.Log.d("PROJECTS_DEBUG", "=== loadProjects START ===");
        android.util.Log.d("PROJECTS_DEBUG", "Token: " + (token.isEmpty() ? "EMPTY" : token.substring(0, Math.min(20, token.length())) + "..."));
        android.util.Log.d("PROJECTS_DEBUG", "UserId: " + userId);

        Request.Builder requestBuilder = new Request.Builder()
                .url(API_BASE + "/collections/projects/records?page=1&perPage=50")
                .addHeader("Accept", "application/json");

        if (!token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
            android.util.Log.d("PROJECTS_DEBUG", "Authorization header added");
        }

        Request request = requestBuilder.build();
        android.util.Log.d("PROJECTS_DEBUG", "Request URL: " + request.url());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                android.util.Log.e("PROJECTS_DEBUG", "onFailure: " + e.getMessage(), e);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Ошибка загрузки проектов", Toast.LENGTH_SHORT).show();
                        showEmptyState(true);
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body() != null ? response.body().string() : "";
                android.util.Log.d("PROJECTS_DEBUG", "HTTP Code: " + response.code());
                android.util.Log.d("PROJECTS_DEBUG", "Response Body: " + responseData);

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject json = new JSONObject(responseData);
                                JSONArray items = json.optJSONArray("items");
                                android.util.Log.d("PROJECTS_DEBUG", "Items count: " + (items != null ? items.length() : 0));

                                projectList.clear();
                                if (items != null) {
                                    for (int i = 0; i < items.length(); i++) {
                                        JSONObject item = items.getJSONObject(i);
                                        ProjectItem project = new ProjectItem(item);
                                        projectList.add(project);
                                        android.util.Log.d("PROJECTS_DEBUG", "Project " + i + ": id=" + project.getId() +
                                                ", title=" + project.getTitle() +
                                                ", created=" + project.getCreated() +
                                                ", daysPassed=" + project.getDaysPassed());
                                    }
                                }
                                projectAdapter.updateProjects(projectList);
                                showEmptyState(projectList.isEmpty());
                            } catch (Exception e) {
                                android.util.Log.e("PROJECTS_DEBUG", "Parse error: " + e.getMessage(), e);
                                Toast.makeText(requireContext(), "Ошибка парсинга", Toast.LENGTH_SHORT).show();
                                showEmptyState(true);
                            }
                        } else {
                            android.util.Log.e("PROJECTS_DEBUG", "HTTP error: " + response.code());
                            Toast.makeText(requireContext(), "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                            showEmptyState(true);
                        }
                    });
                }
            }
        });
    }

    private void showEmptyState(boolean show) {
        if (textNoProjects != null && projectsRecyclerView != null) {
            textNoProjects.setVisibility(show ? View.VISIBLE : View.GONE);
            projectsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    // Метод для обновления списка после создания проекта
    public void refreshProjects() {
        loadProjects();
    }
}