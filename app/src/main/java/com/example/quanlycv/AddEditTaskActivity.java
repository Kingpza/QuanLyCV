package com.example.quanlycv;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlycv.database.DatabaseHelper;
import com.example.quanlycv.model.Subtask;
import com.example.quanlycv.model.Task;
import com.example.quanlycv.utils.AlarmHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddEditTaskActivity extends AppCompatActivity {

    public static final String EXTRA_TASK = "EXTRA_TASK";

    private EditText etTaskTitle, etTaskDesc, etSubtaskInput, etTaskCategory;
    private Spinner spinnerPriority, spinnerStatus, spinnerReminder, spinnerRepeat;
    private Button btnSelectDate, btnSelectTime;
    private LinearLayout llSubtasksContainer;
    private TextView tvAttachmentPath;

    private DatabaseHelper dbHelper;
    private Task existingTask = null;

    private final List<Subtask> currentSubtasks = new ArrayList<>();
    private String selectedAttachmentUri = "";

    private int selectedYear, selectedMonth, selectedDay;
    private int selectedHour, selectedMinute;

    private ActivityResultLauncher<String> attachmentPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        dbHelper = new DatabaseHelper(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDesc = findViewById(R.id.etTaskDesc);
        etSubtaskInput = findViewById(R.id.etSubtaskInput);
        etTaskCategory = findViewById(R.id.etTaskCategory);

        spinnerPriority = findViewById(R.id.spinnerPriority);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerReminder = findViewById(R.id.spinnerReminder);
        spinnerRepeat = findViewById(R.id.spinnerRepeat);

        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        Button btnAddSubtask = findViewById(R.id.btnAddSubtask);
        Button btnSelectAttachment = findViewById(R.id.btnSelectAttachment);
        Button btnSaveTask = findViewById(R.id.btnSaveTask);
        Button btnCancel = findViewById(R.id.btnCancel);

        llSubtasksContainer = findViewById(R.id.llSubtasksContainer);
        tvAttachmentPath = findViewById(R.id.tvAttachmentPath);

        // Attachment File Picker Launcher
        attachmentPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedAttachmentUri = uri.toString();
                        tvAttachmentPath.setVisibility(View.VISIBLE);
                        tvAttachmentPath.setText(getString(R.string.selected_file_format, uri.getLastPathSegment()));
                    }
                }
        );

        setupSpinners();

        Calendar now = Calendar.getInstance();
        selectedYear = now.get(Calendar.YEAR);
        selectedMonth = now.get(Calendar.MONTH);
        selectedDay = now.get(Calendar.DAY_OF_MONTH);
        selectedHour = now.get(Calendar.HOUR_OF_DAY);
        selectedMinute = now.get(Calendar.MINUTE);

        updateDateButtonText();
        updateTimeButtonText();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.containsKey(EXTRA_TASK)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                existingTask = bundle.getSerializable(EXTRA_TASK, Task.class);
            } else {
                @SuppressWarnings("deprecation")
                Task taskExtra = (Task) bundle.getSerializable(EXTRA_TASK);
                existingTask = taskExtra;
            }

            if (existingTask != null) {
                tvHeaderTitle.setText(R.string.title_edit_task);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.title_edit_task);
                }

                etTaskTitle.setText(existingTask.getTitle());
                etTaskDesc.setText(existingTask.getDesc());

                if (existingTask.getCategoryName() != null && !existingTask.getCategoryName().isEmpty()) {
                    etTaskCategory.setText(existingTask.getCategoryName());
                } else {
                    etTaskCategory.setText(getString(R.string.default_user_name));
                }

                // Set Priority Spinner (1: Low, 2: Normal, 3: High, 4: Urgent)
                int priorityIndex = Math.max(0, existingTask.getPriority() - 1);
                spinnerPriority.setSelection(priorityIndex);

                // Set Status Spinner
                spinnerStatus.setSelection(Math.min(existingTask.getStatus(), 2));

                // Set Reminder Lead Time
                switch (existingTask.getReminderLeadTime()) {
                    case 10:
                        spinnerReminder.setSelection(1);
                        break;
                    case 60:
                        spinnerReminder.setSelection(2);
                        break;
                    case 1440:
                        spinnerReminder.setSelection(3);
                        break;
                    default:
                        spinnerReminder.setSelection(0);
                        break;
                }

                // Set Repeat Rule
                String rule = existingTask.getRepeatRule() != null ? existingTask.getRepeatRule() : "";
                switch (rule) {
                    case "DAILY":
                        spinnerRepeat.setSelection(1);
                        break;
                    case "WEEKLY":
                        spinnerRepeat.setSelection(2);
                        break;
                    case "MONTHLY":
                        spinnerRepeat.setSelection(3);
                        break;
                    default:
                        spinnerRepeat.setSelection(0);
                        break;
                }

                // Subtasks
                if (existingTask.getSubtasks() != null) {
                    currentSubtasks.addAll(existingTask.getSubtasks());
                    renderSubtasks();
                }

                // Attachment
                if (existingTask.getAttachmentUri() != null && !existingTask.getAttachmentUri().isEmpty()) {
                    selectedAttachmentUri = existingTask.getAttachmentUri();
                    tvAttachmentPath.setVisibility(View.VISIBLE);
                    tvAttachmentPath.setText(getString(R.string.selected_file_format, Uri.parse(selectedAttachmentUri).getLastPathSegment()));
                }

                parseExistingDateAndTime(existingTask.getDate(), existingTask.getTime());
            }
        } else {
            tvHeaderTitle.setText(R.string.title_add_task);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.title_add_task);
            }
            if (intent.hasExtra("EXTRA_PREFILL_STATUS")) {
                int prefillStatus = intent.getIntExtra("EXTRA_PREFILL_STATUS", 0);
                spinnerStatus.setSelection(Math.min(prefillStatus, 2));
            }
        }

        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectTime.setOnClickListener(v -> showTimePicker());
        btnAddSubtask.setOnClickListener(v -> addSubtask());
        btnSelectAttachment.setOnClickListener(v -> attachmentPickerLauncher.launch("*/*"));

        btnSaveTask.setOnClickListener(v -> saveTask());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        // Priority
        String[] priorities = {
                getString(R.string.priority_low),
                getString(R.string.priority_normal),
                getString(R.string.priority_high),
                getString(R.string.priority_urgent)
        };
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priorities);
        spinnerPriority.setAdapter(priorityAdapter);
        spinnerPriority.setSelection(1); // Default Normal

        // Status
        String[] statuses = {
                getString(R.string.status_pending),
                getString(R.string.status_in_progress),
                getString(R.string.status_completed)
        };
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
        spinnerStatus.setAdapter(statusAdapter);

        // Reminder Lead Times
        String[] reminderLeads = {
                getString(R.string.reminder_at_time),
                getString(R.string.reminder_10m),
                getString(R.string.reminder_1h),
                getString(R.string.reminder_1d)
        };
        ArrayAdapter<String> reminderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, reminderLeads);
        spinnerReminder.setAdapter(reminderAdapter);

        // Repeat Rules
        String[] repeatRules = {
                getString(R.string.repeat_none),
                getString(R.string.repeat_daily),
                getString(R.string.repeat_weekly),
                getString(R.string.repeat_monthly)
        };
        ArrayAdapter<String> repeatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, repeatRules);
        spinnerRepeat.setAdapter(repeatAdapter);
    }

    private void addSubtask() {
        String title = etSubtaskInput.getText().toString().trim();
        if (!title.isEmpty()) {
            currentSubtasks.add(new Subtask(title, false));
            etSubtaskInput.setText("");
            renderSubtasks();
        }
    }

    private void renderSubtasks() {
        llSubtasksContainer.removeAllViews();
        for (Subtask subtask : currentSubtasks) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_subtask, llSubtasksContainer, false);
            CheckBox cbDone = view.findViewById(R.id.cbSubtaskDone);
            TextView tvTitle = view.findViewById(R.id.tvSubtaskTitle);
            ImageButton btnDelete = view.findViewById(R.id.btnDeleteSubtask);

            tvTitle.setText(subtask.getTitle());
            cbDone.setChecked(subtask.isCompleted());

            cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> subtask.setCompleted(isChecked));
            btnDelete.setOnClickListener(v -> {
                currentSubtasks.remove(subtask);
                renderSubtasks();
            });

            llSubtasksContainer.addView(view);
        }
    }

    @SuppressWarnings("IfCanBeSwitch")
    private void parseExistingDateAndTime(String dateStr, String timeStr) {
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                String[] parts = dateStr.split("/");
                if (parts.length == 3) {
                    selectedDay = Integer.parseInt(parts[0]);
                    selectedMonth = Integer.parseInt(parts[1]) - 1;
                    selectedYear = Integer.parseInt(parts[2]);
                    updateDateButtonText();
                }
            } catch (Exception ignored) {}
        }

        if (timeStr != null && !timeStr.isEmpty()) {
            try {
                String[] parts = timeStr.split(":");
                if (parts.length == 2) {
                    selectedHour = Integer.parseInt(parts[0]);
                    selectedMinute = Integer.parseInt(parts[1]);
                    updateTimeButtonText();
                }
            } catch (Exception ignored) {}
        }
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedYear = year;
                    selectedMonth = month;
                    selectedDay = dayOfMonth;
                    updateDateButtonText();
                },
                selectedYear, selectedMonth, selectedDay);
        dialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    updateTimeButtonText();
                },
                selectedHour, selectedMinute, true);
        dialog.show();
    }

    private void updateDateButtonText() {
        String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                selectedDay, selectedMonth + 1, selectedYear);
        btnSelectDate.setText(getString(R.string.date_format_btn, formattedDate));
    }

    private void updateTimeButtonText() {
        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d",
                selectedHour, selectedMinute);
        btnSelectTime.setText(getString(R.string.time_format_btn, formattedTime));
    }

    @SuppressWarnings("IfCanBeSwitch")
    private void saveTask() {
        String title = etTaskTitle.getText().toString().trim();
        String desc = etTaskDesc.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, R.string.err_empty_title, Toast.LENGTH_SHORT).show();
            return;
        }

        String dateFormatted = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                selectedDay, selectedMonth + 1, selectedYear);
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d",
                selectedHour, selectedMinute);

        String rawCatName = etTaskCategory.getText().toString().trim();
        String catName = rawCatName.isEmpty() ? "Cá nhân" : rawCatName;
        int categoryId = dbHelper.getOrCreateCategoryByName(catName);

        int priority = spinnerPriority.getSelectedItemPosition() + 1; // 1 to 4
        int status = spinnerStatus.getSelectedItemPosition(); // 0 to 2

        // Reminder Lead Time
        int reminderLeadTime;
        int reminderPos = spinnerReminder.getSelectedItemPosition();
        switch (reminderPos) {
            case 1:
                reminderLeadTime = 10;
                break;
            case 2:
                reminderLeadTime = 60;
                break;
            case 3:
                reminderLeadTime = 1440;
                break;
            default:
                reminderLeadTime = 0;
                break;
        }

        // Repeat Rule
        String repeatRule;
        int repeatPos = spinnerRepeat.getSelectedItemPosition();
        switch (repeatPos) {
            case 1:
                repeatRule = "DAILY";
                break;
            case 2:
                repeatRule = "WEEKLY";
                break;
            case 3:
                repeatRule = "MONTHLY";
                break;
            default:
                repeatRule = "NONE";
                break;
        }

        if (existingTask != null) {
            existingTask.setTitle(title);
            existingTask.setDesc(desc);
            existingTask.setDate(dateFormatted);
            existingTask.setTime(timeFormatted);
            existingTask.setStatus(status);
            existingTask.setPriority(priority);
            existingTask.setCategoryId(categoryId);
            existingTask.setCategoryName(catName);
            existingTask.setReminderLeadTime(reminderLeadTime);
            existingTask.setRepeatRule(repeatRule);
            existingTask.setAttachmentUri(selectedAttachmentUri);
            existingTask.setSubtasks(currentSubtasks);

            dbHelper.updateTask(existingTask);

            if (status == 2) {
                AlarmHelper.cancelAlarm(this, existingTask.getId());
            } else {
                AlarmHelper.setAlarm(this, existingTask);
            }

            Toast.makeText(this, R.string.msg_task_updated, Toast.LENGTH_SHORT).show();
        } else {
            Task newTask = new Task(0, title, desc, dateFormatted, timeFormatted, status, priority, categoryId, repeatRule);
            newTask.setCategoryName(catName);
            newTask.setReminderLeadTime(reminderLeadTime);
            newTask.setAttachmentUri(selectedAttachmentUri);
            newTask.setSubtasks(currentSubtasks);

            long newId = dbHelper.addTask(newTask);

            if (newId > 0) {
                newTask.setId((int) newId);
                if (status != 2) {
                    AlarmHelper.setAlarm(this, newTask);
                }
            }

            Toast.makeText(this, R.string.msg_task_added, Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK);
        finish();
    }
}
