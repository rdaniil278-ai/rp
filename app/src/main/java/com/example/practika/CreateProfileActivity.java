package com.example.practika;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreateProfileActivity extends AppCompatActivity {

    private EditText editTextFieldName;
    private EditText editTextFieldPatronymic;
    private EditText editTextFieldSurname;
    private EditText editTextFieldDate;
    private EditText textFieldEmail;

    private TextView genderField;
    private LinearLayout genderDropdown;
    private ImageView genderArrow;

    private Button nextButton;

    private boolean isDropdownOpen = false;
    private boolean isGenderSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        initViews();
        setupClickListeners();
        setupTextWatchers();
        updateCreateButtonState();
    }

    private void initViews() {
        editTextFieldName = findViewById(R.id.editTextFieldName);
        editTextFieldPatronymic = findViewById(R.id.editTextFieldPatronomic);
        editTextFieldSurname = findViewById(R.id.editTextFieldSurname);
        editTextFieldDate = findViewById(R.id.editTextFieldDate);
        textFieldEmail = findViewById(R.id.textFieldEmail);

        genderField = findViewById(R.id.genderField);
        genderDropdown = findViewById(R.id.genderDropdown);
        genderArrow = findViewById(R.id.genderArrow);

        nextButton = findViewById(R.id.nextButton);
    }

    private void setupClickListeners() {
        genderField.setOnClickListener(v -> toggleDropdown());
        findViewById(R.id.maleOption).setOnClickListener(v -> selectGender("Мужской"));
        findViewById(R.id.femaleOption).setOnClickListener(v -> selectGender("Женский"));

        nextButton.setOnClickListener(v -> {
            String email = textFieldEmail.getText().toString().trim();
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                textFieldEmail.setError("Введите корректный email");
                textFieldEmail.requestFocus();
                return;
            }
            if (isAllFieldsFilled()) {
                // Передаем данные на следующий экран через Intent
                Intent intent = new Intent(CreateProfileActivity.this, CreateProfilePassActivity.class);
                intent.putExtra("name", editTextFieldName.getText().toString().trim());
                intent.putExtra("patronymic", editTextFieldPatronymic.getText().toString().trim());
                intent.putExtra("surname", editTextFieldSurname.getText().toString().trim());
                intent.putExtra("birthday", editTextFieldDate.getText().toString().trim());
                intent.putExtra("gender", genderField.getText().toString());
                intent.putExtra("email", email);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setupTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.hashCode() == textFieldEmail.getText().hashCode()) {
                    textFieldEmail.setError(null);
                }
            }
            @Override public void afterTextChanged(Editable s) {
                updateCreateButtonState();
            }
        };
        editTextFieldSurname.addTextChangedListener(watcher);
        editTextFieldName.addTextChangedListener(watcher);
        editTextFieldPatronymic.addTextChangedListener(watcher);
        editTextFieldDate.addTextChangedListener(watcher);
        textFieldEmail.addTextChangedListener(watcher);
    }

    private boolean isAllFieldsFilled() {
        return !TextUtils.isEmpty(editTextFieldSurname.getText().toString().trim()) &&
                !TextUtils.isEmpty(editTextFieldName.getText().toString().trim()) &&
                !TextUtils.isEmpty(editTextFieldPatronymic.getText().toString().trim()) &&
                !TextUtils.isEmpty(editTextFieldDate.getText().toString().trim()) &&
                !TextUtils.isEmpty(textFieldEmail.getText().toString().trim()) &&
                isGenderSelected;
    }

    private void updateCreateButtonState() {
        if (isAllFieldsFilled()) {
            nextButton.setBackgroundResource(R.drawable.shape_rounded_btn_enable);
            nextButton.setEnabled(true);
        } else {
            nextButton.setBackgroundResource(R.drawable.shape_rounded_btn_background_create_card);
            nextButton.setEnabled(false);
        }
    }

    // === Выпадающий список пола ===
    private void toggleDropdown() {
        if (isDropdownOpen) collapseDropdown(); else expandDropdown();
        isDropdownOpen = !isDropdownOpen;
    }
    private void expandDropdown() {
        genderArrow.animate().rotation(180f).setDuration(300).start();
        genderDropdown.setVisibility(View.VISIBLE);
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        genderDropdown.startAnimation(slideDown);
    }
    private void collapseDropdown() {
        genderArrow.animate().rotation(0f).setDuration(300).start();
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) { genderDropdown.setVisibility(View.GONE); }
            @Override public void onAnimationRepeat(Animation animation) {}
        });
        genderDropdown.startAnimation(slideUp);
    }
    private void selectGender(String gender) {
        genderField.setText(gender);
        genderField.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
        isGenderSelected = true;
        collapseDropdown();
        isDropdownOpen = false;
        updateCreateButtonState();
    }
}