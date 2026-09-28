package com.example.quanlycv;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlycv.adapter.TaskAdapter;
import com.example.quanlycv.database.DatabaseHelper;
import com.example.quanlycv.model.Task;
import com.example.quanlycv.utils.AlarmHelper;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private TextView tvSelectedDateHeader, tvEmptyCalendarTasks;
    private ListView lvCalendarTasks;

    private DatabaseHelper dbHelper;
    private TaskAdapter adapter;
    private String currentSelectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        dbHelper = new DatabaseHelper(this);

        ImageButton btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        CalendarView calendarView = findViewById(R.id.calendarView);
        tvSelectedDateHeader = findViewById(R.id.tvSelectedDateHeader);
        tvEmptyCalendarTasks = findViewById(R.id.tvEmptyCalendarTasks);
        lvCalendarTasks = findViewById(R.id.lvCalendarTasks);

        adapter = new TaskAdapter(this, null, dbHelper);
        adapter.setOnTaskActionListener(new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onEditTask(Task task) {
                Intent intent = new Intent(CalendarActivity.this, AddEditTaskActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(AddEditTaskActivity.EXTRA_TASK, task);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onDeleteTask(Task task) {
                new AlertDialog.Builder(CalendarActivity.this)
                        .setTitle(R.string.dialog_delete_title)
                        .setMessage(getString(R.string.dialog_delete_message, task.getTitle()))
                        .setIcon(R.drawable.ic_delete)
                        .setPositiveButton(R.string.btn_yes, (dialog, which) -> {
                            dbHelper.softDeleteTask(task.getId());
                            AlarmHelper.cancelAlarm(CalendarActivity.this, task.getId());
                            loadTasksForDate(currentSelectedDate);
                            Toast.makeText(CalendarActivity.this, R.string.msg_task_deleted, Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton(R.string.btn_no, null)
                        .show();
            }
        });
        lvCalendarTasks.setAdapter(adapter);

        Calendar cal = Calendar.getInstance();
        currentSelectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
        loadTasksForDate(currentSelectedDate);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            currentSelectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
            loadTasksForDate(currentSelectedDate);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!currentSelectedDate.isEmpty()) {
            loadTasksForDate(currentSelectedDate);
        }
    }

    private void loadTasksForDate(String dateStr) {
        tvSelectedDateHeader.setText(getString(R.string.tasks_for_selected_date_format, dateStr));
        List<Task> tasks = dbHelper.getTasksForDate(dateStr);
        adapter.updateList(tasks);

        if (tasks.isEmpty()) {
            tvEmptyCalendarTasks.setVisibility(View.VISIBLE);
            lvCalendarTasks.setVisibility(View.GONE);
        } else {
            tvEmptyCalendarTasks.setVisibility(View.GONE);
            lvCalendarTasks.setVisibility(View.VISIBLE);
        }
    }
}
