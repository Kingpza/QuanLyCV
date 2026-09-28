package com.example.quanlycv;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlycv.adapter.TaskAdapter;
import com.example.quanlycv.database.DatabaseHelper;
import com.example.quanlycv.model.Task;

import java.util.List;

public class TrashActivity extends AppCompatActivity {

    private ListView lvTrashTasks;
    private TextView tvEmptyTrash;

    private DatabaseHelper dbHelper;
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash);

        dbHelper = new DatabaseHelper(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        lvTrashTasks = findViewById(R.id.lvTrashTasks);
        tvEmptyTrash = findViewById(R.id.tvEmptyTrash);
        Button btnClearTrash = findViewById(R.id.btnClearTrash);

        adapter = new TaskAdapter(this, null, dbHelper);
        adapter.setOnTaskActionListener(new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onEditTask(Task task) {
                // In trash, clicking card allows restoring
                dbHelper.restoreTask(task.getId());
                reloadTrash();
                Toast.makeText(TrashActivity.this, "Đã khôi phục công việc!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteTask(Task task) {
                dbHelper.permanentlyDeleteTask(task.getId());
                reloadTrash();
                Toast.makeText(TrashActivity.this, "Đã xóa vĩnh viễn!", Toast.LENGTH_SHORT).show();
            }
        });
        lvTrashTasks.setAdapter(adapter);

        registerForContextMenu(lvTrashTasks);

        btnClearTrash.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Xóa sạch thùng rác")
                .setMessage("Bạn có chắc muốn xóa vĩnh viễn tất cả công việc trong thùng rác?")
                .setPositiveButton(R.string.btn_yes, (dialog, which) -> {
                    dbHelper.clearTrash();
                    reloadTrash();
                    Toast.makeText(this, "Đã dọn sạch thùng rác!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.btn_no, null)
                .show());

        reloadTrash();
    }

    private void reloadTrash() {
        List<Task> trashList = dbHelper.getTrashTasks();
        adapter.updateList(trashList);

        if (trashList.isEmpty()) {
            tvEmptyTrash.setVisibility(View.VISIBLE);
            lvTrashTasks.setVisibility(View.GONE);
        } else {
            tvEmptyTrash.setVisibility(View.GONE);
            lvTrashTasks.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Tùy chọn thùng rác");
        menu.add(0, 1, 0, "Khôi phục công việc");
        menu.add(0, 2, 0, "Xóa vĩnh viễn");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info != null) {
            Task task = adapter.getItem(info.position);
            if (task != null) {
                switch (item.getItemId()) {
                    case 1:
                        dbHelper.restoreTask(task.getId());
                        reloadTrash();
                        Toast.makeText(this, "Đã khôi phục công việc!", Toast.LENGTH_SHORT).show();
                        return true;
                    case 2:
                        dbHelper.permanentlyDeleteTask(task.getId());
                        reloadTrash();
                        Toast.makeText(this, "Đã xóa vĩnh viễn!", Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        break;
                }
            }
        }
        return super.onContextItemSelected(item);
    }
}
