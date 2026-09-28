package com.example.quanlycv;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.quanlycv.adapter.TaskAdapter;
import com.example.quanlycv.database.DatabaseHelper;
import com.example.quanlycv.model.Task;
import com.example.quanlycv.utils.AlarmHelper;
import com.example.quanlycv.utils.SharedPrefManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static boolean isPinVerified = false;

    public static void setPinVerified(boolean verified) {
        isPinVerified = verified;
    }

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView tvEmptyList;
    private ListView lvTasks;

    private TextView btnFilterAll, btnFilterPending, btnFilterDone;

    private DatabaseHelper dbHelper;
    private SharedPrefManager prefManager;
    private TaskAdapter taskAdapter;

    private int currentFilterStatus = 0; // 0: Tất cả, 1: Chưa xong, 2: Đã xong
    private int currentCategoryId = 0; // 0: Tất cả

    private ActivityResultLauncher<Intent> addEditLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefManager = new SharedPrefManager(this);

        if (prefManager.isDarkMode()) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Check PIN Lock
        if (prefManager.isPinEnabled() && !isPinVerified) {
            Intent pinIntent = new Intent(this, PinLockActivity.class);
            startActivity(pinIntent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        dbHelper.seedSampleDataIfEmpty();

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        tvEmptyList = findViewById(R.id.tvEmptyList);
        lvTasks = findViewById(R.id.lvTasks);
        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);

        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterPending = findViewById(R.id.btnFilterPending);
        btnFilterDone = findViewById(R.id.btnFilterDone);

        setupFilterPills();

        // Register Activity Result Launcher for Add/Edit
        addEditLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    boolean isOk = (result.getResultCode() == RESULT_OK);
                    if (isOk) {
                        reloadTasks();
                    }
                }
        );

        // Nút 3 gạch ☰ mở Navigation Drawer
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Setup Navigation Drawer Items
        setupNavigationDrawer();

        // Setup ListView & ContextMenu
        taskAdapter = new TaskAdapter(this, null, dbHelper);
        taskAdapter.setOnTaskStatusChangeListener(this::reloadTasks);
        taskAdapter.setOnTaskActionListener(new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onEditTask(Task task) {
                Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(AddEditTaskActivity.EXTRA_TASK, task);
                intent.putExtras(bundle);
                addEditLauncher.launch(intent);
            }

            @Override
            public void onDeleteTask(Task task) {
                showDeleteConfirmationDialog(task);
            }
        });
        lvTasks.setAdapter(taskAdapter);
        registerForContextMenu(lvTasks);

        // FAB Add Task
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
            addEditLauncher.launch(intent);
        });

        // Register Back Press Listener
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    showExitConfirmationDialog();
                }
            }
        });

        requestNotificationPermissionIfNeeded();
    }

    private void setupFilterPills() {
        if (btnFilterAll == null) return;

        TextView[] pills = {btnFilterAll, btnFilterPending, btnFilterDone};

        btnFilterAll.setOnClickListener(v -> {
            currentFilterStatus = 0;
            currentCategoryId = 0;
            updatePillsUI(pills, btnFilterAll);
            reloadTasks();
        });

        btnFilterPending.setOnClickListener(v -> {
            currentFilterStatus = 1;
            currentCategoryId = 0;
            updatePillsUI(pills, btnFilterPending);
            reloadTasks();
        });

        btnFilterDone.setOnClickListener(v -> {
            currentFilterStatus = 2;
            currentCategoryId = 0;
            updatePillsUI(pills, btnFilterDone);
            reloadTasks();
        });
    }

    private void updatePillsUI(TextView[] pills, TextView activePill) {
        int colorWhite = androidx.core.content.ContextCompat.getColor(this, R.color.white);
        int colorDark = androidx.core.content.ContextCompat.getColor(this, R.color.text_primary);

        for (TextView pill : pills) {
            if (pill == activePill) {
                pill.setBackgroundResource(R.drawable.bg_pill_blue);
                pill.setTextColor(colorWhite);
            } else {
                pill.setBackgroundResource(R.drawable.bg_action_grey);
                pill.setTextColor(colorDark);
            }
        }
    }

    private void setupNavigationDrawer() {
        if (navigationView == null) return;

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            drawerLayout.closeDrawer(GravityCompat.START);

            if (itemId == R.id.nav_tasks) {
                reloadTasks();
                return true;
            } else if (itemId == R.id.nav_kanban) {
                startActivity(new Intent(this, KanbanActivity.class));
                return true;
            } else if (itemId == R.id.nav_calendar) {
                startActivity(new Intent(this, CalendarActivity.class));
                return true;
            } else if (itemId == R.id.nav_stats) {
                startActivity(new Intent(this, StatsActivity.class));
                return true;
            } else if (itemId == R.id.nav_trash) {
                startActivity(new Intent(this, TrashActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            } else if (itemId == R.id.nav_exit) {
                showExitConfirmationDialog();
                return true;
            }
            return false;
        });
    }

    private void updateNavHeader() {
        if (navigationView != null && navigationView.getHeaderCount() > 0) {
            View headerView = navigationView.getHeaderView(0);
            TextView tvNavUserName = headerView.findViewById(R.id.tvNavUserName);
            TextView tvNavUserEmail = headerView.findViewById(R.id.tvNavUserEmail);

            if (tvNavUserName != null) {
                tvNavUserName.setText(prefManager.getUserName());
            }
            if (tvNavUserEmail != null) {
                tvNavUserEmail.setText(prefManager.getUserEmail());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavHeader();
        reloadTasks();
    }

    private void reloadTasks() {
        List<Task> filteredList = dbHelper.getFilteredTasks(currentFilterStatus, "", currentCategoryId, 0);
        taskAdapter.updateList(filteredList);

        if (filteredList.isEmpty()) {
            tvEmptyList.setVisibility(View.VISIBLE);
            lvTasks.setVisibility(View.GONE);
        } else {
            tvEmptyList.setVisibility(View.GONE);
            lvTasks.setVisibility(View.VISIBLE);
        }
    }

    // --- OPTIONS MENU ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_calendar) {
            startActivity(new Intent(this, CalendarActivity.class));
            return true;
        } else if (itemId == R.id.action_kanban) {
            startActivity(new Intent(this, KanbanActivity.class));
            return true;
        } else if (itemId == R.id.action_stats) {
            startActivity(new Intent(this, StatsActivity.class));
            return true;
        } else if (itemId == R.id.action_trash) {
            startActivity(new Intent(this, TrashActivity.class));
            return true;
        } else if (itemId == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (itemId == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (itemId == R.id.action_exit) {
            showExitConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- CONTEXT MENU ---
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.lvTasks) {
            getMenuInflater().inflate(R.menu.menu_context, menu);
            menu.setHeaderTitle("Tùy chọn công việc");
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info != null) {
            Task selectedTask = taskAdapter.getItem(info.position);
            if (selectedTask != null) {
                int itemId = item.getItemId();
                if (itemId == R.id.context_edit) {
                    Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(AddEditTaskActivity.EXTRA_TASK, selectedTask);
                    intent.putExtras(bundle);
                    addEditLauncher.launch(intent);
                    return true;
                } else if (itemId == R.id.context_share) {
                    shareTask(selectedTask);
                    return true;
                } else if (itemId == R.id.context_delete) {
                    showDeleteConfirmationDialog(selectedTask);
                    return true;
                }
            }
        }
        return super.onContextItemSelected(item);
    }

    private void shareTask(Task task) {
        String shareContent = "📋 CÔNG VIỆC: " + task.getTitle() + "\n"
                + "📝 Mô tả: " + (task.getDesc() != null ? task.getDesc() : "") + "\n"
                + "🗓 Hạn chót: " + task.getDate() + " " + task.getTime();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Chia sẻ công việc qua");
        startActivity(shareIntent);
    }

    // --- DIALOGS ---
    private void showDeleteConfirmationDialog(Task task) {
        String message = getString(R.string.dialog_delete_message, task.getTitle());
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(message)
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton(R.string.btn_yes, (dialog, which) -> {
                    dbHelper.softDeleteTask(task.getId());
                    AlarmHelper.cancelAlarm(MainActivity.this, task.getId());
                    reloadTasks();
                    Toast.makeText(MainActivity.this, R.string.msg_task_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_exit_title)
                .setMessage(R.string.dialog_exit_message)
                .setPositiveButton(R.string.btn_yes, (dialog, which) -> finish())
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
}
