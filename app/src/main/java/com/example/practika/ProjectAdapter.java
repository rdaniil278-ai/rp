package com.example.practika;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {
    private List<ProjectItem> projects;
    private OnOpenClickListener listener;

    public interface OnOpenClickListener {
        void onOpenClick(ProjectItem project);
    }

    public ProjectAdapter(List<ProjectItem> projects, OnOpenClickListener listener) {
        this.projects = projects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project_card, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        ProjectItem project = projects.get(position);
        holder.bind(project);
    }

    @Override
    public int getItemCount() {
        return projects != null ? projects.size() : 0;
    }

    public void updateProjects(List<ProjectItem> newProjects) {
        this.projects = newProjects;
        notifyDataSetChanged();
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textDays;
        Button btnOpen;
        CardView cardView;

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textProjectTitle);
            textDays = itemView.findViewById(R.id.textProjectDays);
            btnOpen = itemView.findViewById(R.id.btnOpenProject);
            cardView = itemView.findViewById(R.id.cardProject);
        }

        void bind(ProjectItem project) {
            textTitle.setText(project.getTitle());
            textDays.setText(project.getDaysPassed());

            btnOpen.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOpenClick(project);
                }
            });
        }
    }
}