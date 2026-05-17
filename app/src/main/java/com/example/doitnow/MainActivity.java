package com.example.doitnow;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Task> taskList = new ArrayList<>();
    private LinearLayout llTasksContainer;
    private TextView tvTotalCount, tvDoneCount, tvPendingCount, tvTitle;
    private String selectedPriority = "Low";
    private DatabaseHelper dbHelper;
    private int currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        // Get current user ID
        SharedPreferences sharedPref = getSharedPreferences("DoitNowPrefs", Context.MODE_PRIVATE);
        String username = sharedPref.getString("current_user", "");
        currentUserId = dbHelper.getUserId(username);

        llTasksContainer = findViewById(R.id.ll_tasks_container);
        tvTotalCount = findViewById(R.id.tv_total_count);
        tvDoneCount = findViewById(R.id.tv_done_count);
        tvPendingCount = findViewById(R.id.tv_pending_count);
        tvTitle = findViewById(R.id.tv_title);

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> showAddTaskDialog());
        }

        findViewById(R.id.nav_developer).setOnClickListener(v -> {
            Intent intent = new Intent(this, DeveloperActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        loadTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        if (currentUserId != -1) {
            taskList = dbHelper.getAllTasks(currentUserId);
            updateTaskUI();
        }
    }

    private void showAddTaskDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_task);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText etTaskInput = dialog.findViewById(R.id.et_task_input);
        TextView btnCancel = dialog.findViewById(R.id.btn_cancel);
        View btnAddTask = dialog.findViewById(R.id.btn_add_task);

        TextView btnLow = dialog.findViewById(R.id.btn_priority_low);
        TextView btnMed = dialog.findViewById(R.id.btn_priority_med);
        TextView btnHigh = dialog.findViewById(R.id.btn_priority_high);

        selectedPriority = "Low"; // Default

        if (btnLow != null && btnMed != null && btnHigh != null) {
            btnLow.setOnClickListener(v -> {
                selectedPriority = "Low";
                selectPriorityUI(btnLow, btnMed, btnHigh);
            });
            btnMed.setOnClickListener(v -> {
                selectedPriority = "Med";
                selectPriorityUI(btnMed, btnLow, btnHigh);
            });
            btnHigh.setOnClickListener(v -> {
                selectedPriority = "High";
                selectPriorityUI(btnHigh, btnLow, btnMed);
            });
            selectPriorityUI(btnLow, btnMed, btnHigh);
        }

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());
        if (btnAddTask != null) {
            btnAddTask.setOnClickListener(v -> {
                String title = etTaskInput.getText().toString().trim();
                if (!title.isEmpty()) {
                    Task newTask = new Task(title, selectedPriority);
                    dbHelper.addTask(newTask, currentUserId);
                    loadTasks();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show();
                }
            });
        }

        dialog.show();
    }

    private void selectPriorityUI(TextView selected, TextView other1, TextView other2) {
        selected.setBackgroundResource(R.drawable.bg_priority_low);
        selected.setTextColor(getResources().getColor(R.color.priority_low_text));

        other1.setBackgroundResource(R.drawable.bg_priority_outline);
        other1.setTextColor(getResources().getColor(R.color.text_primary));

        other2.setBackgroundResource(R.drawable.bg_priority_outline);
        other2.setTextColor(getResources().getColor(R.color.text_primary));
    }

    private void updateTaskUI() {
        llTasksContainer.removeAllViews();
        int done = 0;
        int pending = 0;

        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);
            View taskView = LayoutInflater.from(this).inflate(R.layout.item_task, llTasksContainer, false);

            TextView tvTitle = taskView.findViewById(R.id.tv_task_title);
            TextView tvPriority = taskView.findViewById(R.id.tv_task_priority);
            CheckBox cbTask = taskView.findViewById(R.id.cb_task);
            ImageView ivDelete = taskView.findViewById(R.id.iv_delete);

            tvTitle.setText(task.getTitle());
            tvPriority.setText(task.getPriority());
            cbTask.setChecked(task.isCompleted());

            if (task.isCompleted()) {
                done++;
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                pending++;
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
            }

            // Apply priority styles
            if ("High".equals(task.getPriority())) {
                tvPriority.setBackgroundResource(R.drawable.bg_priority_high);
                tvPriority.setTextColor(getResources().getColor(R.color.priority_high_text));
            } else if ("Med".equals(task.getPriority())) {
                tvPriority.setBackgroundResource(R.drawable.bg_priority_med);
                tvPriority.setTextColor(getResources().getColor(R.color.priority_med_text));
            } else {
                tvPriority.setBackgroundResource(R.drawable.bg_priority_low);
                tvPriority.setTextColor(getResources().getColor(R.color.priority_low_text));
            }

            cbTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
                task.setCompleted(isChecked);
                dbHelper.updateTask(task);
                updateTaskUI();
            });

            ivDelete.setOnClickListener(v -> {
                dbHelper.deleteTask(task.getId());
                loadTasks();
            });

            llTasksContainer.addView(taskView);
        }

        tvTotalCount.setText(String.valueOf(taskList.size()));
        tvDoneCount.setText(String.valueOf(done));
        tvPendingCount.setText(String.valueOf(pending));
    }
}
