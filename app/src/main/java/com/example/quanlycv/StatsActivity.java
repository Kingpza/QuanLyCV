package com.example.quanlycv;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.quanlycv.database.DatabaseHelper;
import com.example.quanlycv.model.Task;
import com.example.quanlycv.widget.CircularProgressView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatsActivity extends AppCompatActivity {

    private enum TimeFilter { WEEK, MONTH, ALL }

    private TimeFilter selectedFilter = TimeFilter.WEEK;

    private DatabaseHelper dbHelper;

    private TextView tabWeek, tabMonth, tabAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        dbHelper = new DatabaseHelper(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tabWeek = findViewById(R.id.tabWeek);
        tabMonth = findViewById(R.id.tabMonth);
        tabAll = findViewById(R.id.tabAll);

        if (tabWeek != null) tabWeek.setOnClickListener(v -> selectTimeFilter(TimeFilter.WEEK));
        if (tabMonth != null) tabMonth.setOnClickListener(v -> selectTimeFilter(TimeFilter.MONTH));
        if (tabAll != null) tabAll.setOnClickListener(v -> selectTimeFilter(TimeFilter.ALL));

        selectTimeFilter(TimeFilter.WEEK);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
    }

    private void selectTimeFilter(TimeFilter filter) {
        selectedFilter = filter;

        int activeBg = R.drawable.bg_tab_active;
        int activeText = ContextCompat.getColor(this, R.color.white);
        int inactiveText = ContextCompat.getColor(this, R.color.text_secondary);

        if (tabWeek != null) {
            tabWeek.setBackgroundResource(filter == TimeFilter.WEEK ? activeBg : 0);
            tabWeek.setTextColor(filter == TimeFilter.WEEK ? activeText : inactiveText);
        }
        if (tabMonth != null) {
            tabMonth.setBackgroundResource(filter == TimeFilter.MONTH ? activeBg : 0);
            tabMonth.setTextColor(filter == TimeFilter.MONTH ? activeText : inactiveText);
        }
        if (tabAll != null) {
            tabAll.setBackgroundResource(filter == TimeFilter.ALL ? activeBg : 0);
            tabAll.setTextColor(filter == TimeFilter.ALL ? activeText : inactiveText);
        }

        loadStatistics();
    }

    @SuppressWarnings("ReassignedVariable")
    private void loadStatistics() {
        List<Task> rawTasks = dbHelper.getFilteredTasks(0, "", 0, 0);
        List<Task> allTasks = (rawTasks != null) ? rawTasks : new ArrayList<>();

        // Filter tasks according to selected time frame
        List<Task> filteredTasks = filterTasksByTime(allTasks, selectedFilter);

        int total = filteredTasks.size();
        int completed = 0;
        int pending = 0;

        for (Task t : filteredTasks) {
            boolean isCompleted = (t.getStatus() == 2);
            if (isCompleted) {
                completed++;
            } else {
                pending++;
            }
        }

        int streak = dbHelper.getStreakDays();
        int overallPercent = total > 0 ? (completed * 100 / total) : 0;

        // 1. Performance Overview Card
        CircularProgressView cpvOverview = findViewById(R.id.cpvOverview);
        TextView tvCompletedRatio = findViewById(R.id.tvCompletedRatio);
        TextView tvStreakAndPendingInfo = findViewById(R.id.tvStreakAndPendingInfo);

        if (cpvOverview != null) {
            int cPrimary = ContextCompat.getColor(this, R.color.primary);
            int cTrack = ContextCompat.getColor(this, R.color.divider);
            cpvOverview.setColors(cPrimary, cTrack, cPrimary);
            cpvOverview.setProgress(overallPercent);
        }

        if (tvCompletedRatio != null) {
            tvCompletedRatio.setText(getString(R.string.completed_ratio_format, completed, total));
        }

        if (tvStreakAndPendingInfo != null) {
            tvStreakAndPendingInfo.setText(getString(R.string.streak_and_pending_format, streak, pending));
        }

        // 2. Bar Chart (Daily completion Mon -> Sun)
        updateDailyBarChart(allTasks);



        // 4. Priority Breakdown
        int highCount = 0, mediumCount = 0, lowCount = 0;
        for (Task t : filteredTasks) {
            if (t.getPriority() >= 3) highCount++;
            else if (t.getPriority() == 2) mediumCount++;
            else lowCount++;
        }

        TextView tvPriorityHighCount = findViewById(R.id.tvPriorityHighCount);
        TextView tvPriorityMediumCount = findViewById(R.id.tvPriorityMediumCount);
        TextView tvPriorityLowCount = findViewById(R.id.tvPriorityLowCount);

        if (tvPriorityHighCount != null) tvPriorityHighCount.setText(String.format(Locale.getDefault(), "%d", highCount));
        if (tvPriorityMediumCount != null) tvPriorityMediumCount.setText(String.format(Locale.getDefault(), "%d", mediumCount));
        if (tvPriorityLowCount != null) tvPriorityLowCount.setText(String.format(Locale.getDefault(), "%d", lowCount));
    }

    @SuppressWarnings("IfCanBeSwitch")
    private List<Task> filterTasksByTime(List<Task> tasks, TimeFilter filter) {
        if (filter == TimeFilter.ALL) return tasks;

        List<Task> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Calendar now = Calendar.getInstance();

        for (Task t : tasks) {
            if (t.getDate() == null || t.getDate().isEmpty()) {
                result.add(t);
                continue;
            }
            try {
                Date d = sdf.parse(t.getDate());
                if (d == null) {
                    result.add(t);
                    continue;
                }
                Calendar taskCal = Calendar.getInstance();
                taskCal.setTime(d);

                if (filter == TimeFilter.WEEK) {
                    if (now.get(Calendar.YEAR) == taskCal.get(Calendar.YEAR) &&
                        now.get(Calendar.WEEK_OF_YEAR) == taskCal.get(Calendar.WEEK_OF_YEAR)) {
                        result.add(t);
                    }
                } else if (filter == TimeFilter.MONTH) {
                    if (now.get(Calendar.YEAR) == taskCal.get(Calendar.YEAR) &&
                        now.get(Calendar.MONTH) == taskCal.get(Calendar.MONTH)) {
                        result.add(t);
                    }
                }
            } catch (Exception e) {
                result.add(t);
            }
        }
        return result;
    }

    @SuppressWarnings({"ForLoopReplaceableByWhile", "ReassignedVariable"})
    private void updateDailyBarChart(List<Task> allTasks) {
        int[] dayCounts = new int[7]; // 0: Mon, 1: Tue, ..., 6: Sun
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar now = Calendar.getInstance();

        for (Task t : allTasks) {
            if (t.getStatus() == 2 && t.getDate() != null && !t.getDate().isEmpty()) {
                try {
                    Date d = sdf.parse(t.getDate());
                    if (d != null) {
                        Calendar taskCal = Calendar.getInstance();
                        taskCal.setTime(d);

                        if (now.get(Calendar.YEAR) == taskCal.get(Calendar.YEAR) &&
                            now.get(Calendar.WEEK_OF_YEAR) == taskCal.get(Calendar.WEEK_OF_YEAR)) {
                            int dow = taskCal.get(Calendar.DAY_OF_WEEK); // 1 = Sun, 2 = Mon, ...
                            int dayIdx = (dow == Calendar.SUNDAY) ? 6 : (dow - 2);
                            if (dayIdx >= 0 && dayIdx < 7) {
                                dayCounts[dayIdx]++;
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        int maxVal = 0;
        for (int c : dayCounts) {
            if (c > maxVal) maxVal = c;
        }
        maxVal = Math.max(1, maxVal);

        int[] valViewIds = {R.id.tvBarValMon, R.id.tvBarValTue, R.id.tvBarValWed, R.id.tvBarValThu, R.id.tvBarValFri, R.id.tvBarValSat, R.id.tvBarValSun};
        int[] barViewIds = {R.id.vBarMon, R.id.vBarTue, R.id.vBarWed, R.id.vBarThu, R.id.vBarFri, R.id.vBarSat, R.id.vBarSun};

        float density = getResources().getDisplayMetrics().density;
        int minPx = (int) (8 * density);
        int maxPx = (int) (70 * density);

        for (int i = 0; i < 7; i++) {
            TextView tvVal = findViewById(valViewIds[i]);
            View vBar = findViewById(barViewIds[i]);

            if (tvVal != null) tvVal.setText(String.format(Locale.getDefault(), "%d", dayCounts[i]));

            if (vBar != null) {
                int height = (dayCounts[i] <= 0) ? minPx : (minPx + (int) (((float) dayCounts[i] / maxVal) * (maxPx - minPx)));
                ViewGroup.LayoutParams lp = vBar.getLayoutParams();
                if (lp != null) {
                    lp.height = height;
                    vBar.setLayoutParams(lp);
                }
            }
        }
    }


}
