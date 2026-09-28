package com.example.quanlycv;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlycv.database.DatabaseHelper;

import com.example.quanlycv.model.Task;
import com.example.quanlycv.utils.AlarmHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;
import java.util.Locale;

public class KanbanActivity extends AppCompatActivity {

    private TextView tvKanbanTotal, tvKanbanInProgressCount, tvKanbanCompletedCount;
    private TextView tvPriorityHighCount, tvPriorityMediumCount, tvPriorityLowCount;
    private TextView tvHeaderPending, tvHeaderInProgress, tvHeaderCompleted;
    private LinearLayout llContainerPending, llContainerInProgress, llContainerCompleted;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kanban);

        dbHelper = new DatabaseHelper(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        tvKanbanTotal = findViewById(R.id.tvKanbanTotal);
        tvKanbanInProgressCount = findViewById(R.id.tvKanbanInProgressCount);
        tvKanbanCompletedCount = findViewById(R.id.tvKanbanCompletedCount);

        tvPriorityHighCount = findViewById(R.id.tvPriorityHighCount);
        tvPriorityMediumCount = findViewById(R.id.tvPriorityMediumCount);
        tvPriorityLowCount = findViewById(R.id.tvPriorityLowCount);

        tvHeaderPending = findViewById(R.id.tvHeaderPending);
        tvHeaderInProgress = findViewById(R.id.tvHeaderInProgress);
        tvHeaderCompleted = findViewById(R.id.tvHeaderCompleted);

        llContainerPending = findViewById(R.id.llContainerPending);
        llContainerInProgress = findViewById(R.id.llContainerInProgress);
        llContainerCompleted = findViewById(R.id.llContainerCompleted);

        loadKanbanData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadKanbanData();
    }





    private String formatShortDateTime(String rawDateStr, String timeStr) {
        String dateInput = (rawDateStr != null) ? rawDateStr : "";
        String time = (timeStr != null) ? timeStr : "";
        String dateDisplay;
        if (dateInput.length() >= 10 && dateInput.contains("/")) {
            String[] parts = dateInput.split("/");
            dateDisplay = (parts.length == 3) ? (parts[0] + "/" + parts[1]) : dateInput;
        } else {
            dateDisplay = dateInput;
        }
        return (dateDisplay + " " + time).trim();
    }

    @SuppressWarnings("ReassignedVariable")
    private int countPriorityTasks(List<Task> tasks, int minPriority, int maxPriority) {
        int count = 0;
        if (tasks != null) {
            for (Task t : tasks) {
                if (t.getPriority() >= minPriority && t.getPriority() <= maxPriority) {
                    count++;
                }
            }
        }
        return count;
    }

    private void loadKanbanData() {
        List<Task> pending = dbHelper.getTasksByStatus(0);
        List<Task> inProgress = dbHelper.getTasksByStatus(1);
        List<Task> completed = dbHelper.getTasksByStatus(2);

        int total = dbHelper.getTotalCount();
        if (tvKanbanTotal != null) tvKanbanTotal.setText(String.format(Locale.getDefault(), "%d", total));
        if (tvKanbanInProgressCount != null) tvKanbanInProgressCount.setText(String.format(Locale.getDefault(), "%d", inProgress.size()));
        if (tvKanbanCompletedCount != null) tvKanbanCompletedCount.setText(String.format(Locale.getDefault(), "%d", completed.size()));

        // Calculate priorities
        List<Task> allTasks = dbHelper.getFilteredTasks(0, "", 0, 0);
        int highCount = countPriorityTasks(allTasks, 3, 4);
        int mediumCount = countPriorityTasks(allTasks, 2, 2);
        int lowCount = countPriorityTasks(allTasks, 1, 1);

        if (tvPriorityHighCount != null) tvPriorityHighCount.setText(String.format(Locale.getDefault(), "%d", highCount));
        if (tvPriorityMediumCount != null) tvPriorityMediumCount.setText(String.format(Locale.getDefault(), "%d", mediumCount));
        if (tvPriorityLowCount != null) tvPriorityLowCount.setText(String.format(Locale.getDefault(), "%d", lowCount));

        // Update Overall Progress Board
        int overallPercent = total > 0 ? (completed.size() * 100 / total) : 0;
        ProgressBar pbKanbanOverall = findViewById(R.id.pbKanbanOverall);
        TextView tvKanbanProgressPercent = findViewById(R.id.tvKanbanProgressPercent);
        TextView tvKanbanProgressDetail = findViewById(R.id.tvKanbanProgressDetail);

        if (pbKanbanOverall != null) pbKanbanOverall.setProgress(overallPercent);
        if (tvKanbanProgressPercent != null) tvKanbanProgressPercent.setText(getString(R.string.percent_format, overallPercent));
        if (tvKanbanProgressDetail != null) tvKanbanProgressDetail.setText(getString(R.string.kanban_progress_format, completed.size(), total));



        tvHeaderPending.setText(getString(R.string.kanban_header_pending, pending.size()));
        tvHeaderInProgress.setText(getString(R.string.kanban_header_in_progress, inProgress.size()));
        tvHeaderCompleted.setText(getString(R.string.kanban_header_completed, completed.size()));

        populateContainer(llContainerPending, pending);
        populateContainer(llContainerInProgress, inProgress);
        populateContainer(llContainerCompleted, completed);
    }

    private void populateContainer(LinearLayout container, List<Task> tasks) {
        if (container == null) return;
        container.removeAllViews();

        if (tasks == null || tasks.isEmpty()) {
            TextView emptyTv = new TextView(this);
            emptyTv.setText(R.string.no_tasks_for_date);
            emptyTv.setTextSize(12);
            emptyTv.setPadding(0, 16, 0, 16);
            emptyTv.setTextColor(getColor(R.color.text_hint));
            container.addView(emptyTv);
            return;
        }

        for (Task task : tasks) {
            View cardView = LayoutInflater.from(this).inflate(R.layout.item_kanban_card, container, false);

            TextView tvCategory = cardView.findViewById(R.id.tvKanbanCategory);
            TextView tvPriority = cardView.findViewById(R.id.tvKanbanPriority);
            TextView tvTitle = cardView.findViewById(R.id.tvKanbanTitle);
            TextView tvDate = cardView.findViewById(R.id.tvKanbanDate);
            TextView tvAuthorBadge = cardView.findViewById(R.id.tvKanbanAuthorBadge);
            ImageView imgCircleBadge = cardView.findViewById(R.id.imgKanbanCircleBadge);

            String title = task.getTitle() != null ? task.getTitle() : getString(R.string.task_list_title);
            tvTitle.setText(title);

            String catName = task.getCategoryName() != null ? task.getCategoryName() : getString(R.string.default_user_name);
            tvCategory.setText(catName);

            String initial = title.substring(0, 1).toUpperCase();
            tvAuthorBadge.setText(initial);

            int catId = task.getCategoryId();
            int badgeBgRes;
            switch (catId % 4) {
                case 1:
                    badgeBgRes = R.drawable.bg_pill_purple;
                    break;
                case 2:
                    badgeBgRes = R.drawable.bg_pill_magenta;
                    break;
                case 3:
                    badgeBgRes = R.drawable.bg_pill_yellow;
                    break;
                default:
                    badgeBgRes = R.drawable.bg_pill_blue;
                    break;
            }
            tvAuthorBadge.setBackgroundResource(badgeBgRes);

            if (imgCircleBadge != null) {
                switch (task.getStatus()) {
                    case 2:
                        imgCircleBadge.setImageResource(R.drawable.ic_check_green_circle);
                        imgCircleBadge.setColorFilter(getColor(R.color.status_completed));
                        break;
                    case 1:
                        imgCircleBadge.setImageResource(R.drawable.ic_circle_outline);
                        imgCircleBadge.setColorFilter(getColor(R.color.status_pending));
                        break;
                    default:
                        imgCircleBadge.setImageResource(R.drawable.ic_circle_outline);
                        imgCircleBadge.setColorFilter(getColor(R.color.icon_grey));
                        break;
                }
            }

            switch (task.getPriority()) {
                case 4:
                    tvPriority.setText(R.string.priority_urgent);
                    tvPriority.setTextColor(getColor(R.color.red_delete));
                    break;
                case 3:
                    tvPriority.setText(R.string.priority_high);
                    tvPriority.setTextColor(getColor(R.color.cat_magenta));
                    break;
                case 1:
                    tvPriority.setText(R.string.priority_low);
                    tvPriority.setTextColor(getColor(R.color.text_secondary));
                    break;
                case 2:
                default:
                    tvPriority.setText(R.string.priority_normal);
                    tvPriority.setTextColor(getColor(R.color.status_pending));
                    break;
            }

            tvDate.setText(formatShortDateTime(task.getDate(), task.getTime()));

            cardView.setOnClickListener(v -> showMoveStatusDialog(task));
            if (imgCircleBadge != null) {
                imgCircleBadge.setOnClickListener(v -> showMoveStatusDialog(task));
            }

            container.addView(cardView);
        }
    }

    private void showMoveStatusDialog(Task task) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_task_options, findViewById(android.R.id.content), false);
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            View bottomSheet = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTaskTitle);
        TextView tvCategory = dialogView.findViewById(R.id.tvDialogTaskCategory);
        TextView tvSubtitle = dialogView.findViewById(R.id.tvDialogTaskSubtitle);
        ImageButton btnClose = dialogView.findViewById(R.id.btnCloseDialog);

        View btnStatusPending = dialogView.findViewById(R.id.btnStatusPending);
        View btnStatusInProgress = dialogView.findViewById(R.id.btnStatusInProgress);
        View btnStatusCompleted = dialogView.findViewById(R.id.btnStatusCompleted);

        ImageView imgCheckPending = dialogView.findViewById(R.id.imgCheckPending);
        ImageView imgCheckInProgress = dialogView.findViewById(R.id.imgCheckInProgress);
        ImageView imgCheckCompleted = dialogView.findViewById(R.id.imgCheckCompleted);

        View btnEdit = dialogView.findViewById(R.id.btnEditTask);
        View btnDelete = dialogView.findViewById(R.id.btnDeleteTask);

        tvTitle.setText(task.getTitle() != null ? task.getTitle() : "Công việc");
        String catName = task.getCategoryName() != null ? task.getCategoryName() : "Cá nhân";
        tvCategory.setText(catName);

        int currentStatus = task.getStatus();
        String currentStatusName;

        switch (currentStatus) {
            case 0:
                currentStatusName = getString(R.string.status_pending);
                btnStatusPending.setBackgroundResource(R.drawable.bg_status_option_active);
                imgCheckPending.setVisibility(View.VISIBLE);
                break;
            case 1:
                currentStatusName = getString(R.string.status_in_progress);
                btnStatusInProgress.setBackgroundResource(R.drawable.bg_status_option_active);
                imgCheckInProgress.setVisibility(View.VISIBLE);
                break;
            case 2:
                currentStatusName = getString(R.string.status_completed);
                btnStatusCompleted.setBackgroundResource(R.drawable.bg_status_option_active);
                imgCheckCompleted.setVisibility(View.VISIBLE);
                break;
            default:
                currentStatusName = getString(R.string.status_pending);
                break;
        }

        tvSubtitle.setText(getString(R.string.kanban_current_status_format, currentStatusName));

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnStatusPending.setOnClickListener(v -> {
            task.setStatus(0);
            dbHelper.updateTask(task);
            loadKanbanData();
            Toast.makeText(this, "Đã chuyển sang CẦN LÀM", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnStatusInProgress.setOnClickListener(v -> {
            task.setStatus(1);
            dbHelper.updateTask(task);
            loadKanbanData();
            Toast.makeText(this, "Đã chuyển sang ĐANG LÀM", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnStatusCompleted.setOnClickListener(v -> {
            task.setStatus(2);
            dbHelper.updateTask(task);
            AlarmHelper.cancelAlarm(this, task.getId());
            loadKanbanData();
            Toast.makeText(this, "Đã chuyển sang HOÀN THÀNH", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, AddEditTaskActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(AddEditTaskActivity.EXTRA_TASK, task);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            dbHelper.softDeleteTask(task.getId());
            AlarmHelper.cancelAlarm(this, task.getId());
            loadKanbanData();
            Toast.makeText(this, R.string.msg_task_deleted, Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }
}
