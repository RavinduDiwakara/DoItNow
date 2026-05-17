package com.example.doitnow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvValUsername, tvValEmail, tvProfileName;
    private DatabaseHelper dbHelper;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);

        tvValUsername = findViewById(R.id.tv_val_username);
        tvValEmail = findViewById(R.id.tv_val_email);
        tvProfileName = findViewById(R.id.tv_profile_name);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.nav_tasks).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        findViewById(R.id.nav_developer).setOnClickListener(v -> {
            Intent intent = new Intent(this, DeveloperActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        findViewById(R.id.btn_sign_out).setOnClickListener(v -> {
            showSignOutDialog();
        });

        findViewById(R.id.btn_edit_info).setOnClickListener(v -> {
            showEditInfoDialog();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        SharedPreferences sharedPref = getSharedPreferences("DoitNowPrefs", Context.MODE_PRIVATE);
        currentUsername = sharedPref.getString("current_user", "");

        if (!currentUsername.isEmpty()) {
            Cursor cursor = dbHelper.getUserByUsername(currentUsername);
            if (cursor != null && cursor.moveToFirst()) {
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                
                tvValUsername.setText(username);
                tvProfileName.setText(username);
                tvValEmail.setText(email);
            }
            if (cursor != null) cursor.close();
        }
    }

    private void showEditInfoDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_info, null);
        TextInputEditText etUsername = dialogView.findViewById(R.id.et_username);
        TextInputEditText etEmail = dialogView.findViewById(R.id.et_email);
        View btnOk = dialogView.findViewById(R.id.btn_ok);
        View btnCancel = dialogView.findViewById(R.id.btn_cancel);

        etUsername.setText(tvValUsername.getText());
        etEmail.setText(tvValEmail.getText());

        AlertDialog dialog = new MaterialAlertDialogBuilder(this, R.style.Theme_Doitnow_Dialog)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnOk.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();

            if (!newUsername.isEmpty() && !newEmail.isEmpty()) {
                dbHelper.updateUser(currentUsername, newUsername, newEmail);
                
                // Update SharedPreferences with the new username
                SharedPreferences sharedPref = getSharedPreferences("DoitNowPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("current_user", newUsername);
                editor.apply();

                currentUsername = newUsername;
                loadUserProfile(); // Refresh UI
                
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showSignOutDialog() {
        new MaterialAlertDialogBuilder(this, R.style.Theme_Doitnow_Dialog)
                .setTitle(R.string.sign_out)
                .setMessage(R.string.sign_out_confirm_msg)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    SharedPreferences sharedPref = getSharedPreferences("DoitNowPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("is_logged_in", false);
                    editor.remove("current_user");
                    editor.apply();

                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .show();
    }
}
