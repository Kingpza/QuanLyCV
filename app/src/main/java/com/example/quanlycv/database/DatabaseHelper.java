package com.example.quanlycv.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.quanlycv.model.Subtask;
import com.example.quanlycv.model.Task;
import com.example.quanlycv.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TasksDB.db";
    private static final int DATABASE_VERSION = 2; // Upgraded to v2

    // Tables
    private static final String TABLE_TASKS = "Tasks";
    private static final String TABLE_CATEGORIES = "Categories";
    private static final String TABLE_SUBTASKS = "Subtasks";
    private static final String TABLE_USERS = "Users";

    // Task Columns
    private static final String KEY_TASK_ID = "id";
    private static final String KEY_TASK_TITLE = "title";
    private static final String KEY_TASK_DESC = "description";
    private static final String KEY_TASK_DATE = "date";
    private static final String KEY_TASK_TIME = "time";
    private static final String KEY_TASK_STATUS = "status";
    private static final String KEY_TASK_PRIORITY = "priority";
    private static final String KEY_TASK_CATEGORY_ID = "category_id";
    private static final String KEY_TASK_ATTACHMENT = "attachment_uri";
    private static final String KEY_TASK_REPEAT = "repeat_rule";
    private static final String KEY_TASK_REMINDER_LEAD = "reminder_lead_time";
    private static final String KEY_TASK_IS_DELETED = "is_deleted";
    private static final String KEY_TASK_CREATED_AT = "created_at";

    // Category Columns
    private static final String KEY_CAT_ID = "id";
    private static final String KEY_CAT_NAME = "name";
    private static final String KEY_CAT_COLOR = "color";
    private static final String KEY_CAT_ICON = "icon";

    // Subtask Columns
    private static final String KEY_SUB_ID = "id";
    private static final String KEY_SUB_TASK_ID = "task_id";
    private static final String KEY_SUB_TITLE = "title";
    private static final String KEY_SUB_IS_COMPLETED = "is_completed";

    // User Columns
    private static final String KEY_USER_ID = "id";
    private static final String KEY_USER_USERNAME = "username";
    private static final String KEY_USER_EMAIL = "email";
    private static final String KEY_USER_PASSWORD = "password";
    private static final String KEY_USER_FULL_NAME = "full_name";
    private static final String KEY_USER_AVATAR = "avatar_uri";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users Table
        String createUsers = "CREATE TABLE " + TABLE_USERS + " ("
                + KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_USER_USERNAME + " TEXT, "
                + KEY_USER_EMAIL + " TEXT UNIQUE, "
                + KEY_USER_PASSWORD + " TEXT, "
                + KEY_USER_FULL_NAME + " TEXT, "
                + KEY_USER_AVATAR + " TEXT"
                + ")";
        db.execSQL(createUsers);

        // Insert Default Demo User
        db.execSQL("INSERT INTO " + TABLE_USERS + " (username, email, password, full_name) VALUES ('nguoidung', 'user@quanlycv.com', '123456', 'Người dùng')");

        // Categories Table
        String createCategories = "CREATE TABLE " + TABLE_CATEGORIES + " ("
                + KEY_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_CAT_NAME + " TEXT, "
                + KEY_CAT_COLOR + " TEXT, "
                + KEY_CAT_ICON + " TEXT"
                + ")";
        db.execSQL(createCategories);

        // Seed Default Categories
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, color, icon) VALUES ('Cá nhân', '#4CAF50', 'ic_person')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, color, icon) VALUES ('Công việc', '#2196F3', 'ic_work')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, color, icon) VALUES ('Học tập', '#FF9800', 'ic_school')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, color, icon) VALUES ('Mua sắm', '#E91E63', 'ic_shopping')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (name, color, icon) VALUES ('Sức khỏe', '#9C27B0', 'ic_health')");

        // Tasks Table
        String createTasks = "CREATE TABLE " + TABLE_TASKS + " ("
                + KEY_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_TASK_TITLE + " TEXT, "
                + KEY_TASK_DESC + " TEXT, "
                + KEY_TASK_DATE + " TEXT, "
                + KEY_TASK_TIME + " TEXT, "
                + KEY_TASK_STATUS + " INTEGER DEFAULT 0, "
                + KEY_TASK_PRIORITY + " INTEGER DEFAULT 2, "
                + KEY_TASK_CATEGORY_ID + " INTEGER DEFAULT 1, "
                + KEY_TASK_ATTACHMENT + " TEXT, "
                + KEY_TASK_REPEAT + " TEXT DEFAULT 'NONE', "
                + KEY_TASK_REMINDER_LEAD + " INTEGER DEFAULT 0, "
                + KEY_TASK_IS_DELETED + " INTEGER DEFAULT 0, "
                + "is_favorite INTEGER DEFAULT 0, "
                + KEY_TASK_CREATED_AT + " INTEGER"
                + ")";
        db.execSQL(createTasks);

        // Subtasks Table
        String createSubtasks = "CREATE TABLE " + TABLE_SUBTASKS + " ("
                + KEY_SUB_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_SUB_TASK_ID + " INTEGER, "
                + KEY_SUB_TITLE + " TEXT, "
                + KEY_SUB_IS_COMPLETED + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(createSubtasks);
    }

    public void seedSampleDataIfEmpty() {
        boolean isEmpty = (getTotalCount() == 0);
        if (isEmpty) {
            insertVietnameseSampleData();
        }
    }



    private void insertVietnameseSampleData() {
        // Task 1: In progress (Đang thực hiện)
        Task t1 = new Task("Kế hoạch dự án mới", "Khảo sát yêu cầu và thiết kế giao diện ứng dụng.", "26/09/2026", "09:00", 1);
        t1.setCategoryId(2);
        List<Subtask> s1 = new ArrayList<>();
        s1.add(new Subtask("Khảo sát yêu cầu", true));
        s1.add(new Subtask("Thiết kế giao diện", true));
        s1.add(new Subtask("Kiểm thử sản phẩm", false));
        t1.setSubtasks(s1);
        addTask(t1);

        // Task 2: Pending (Chờ xử lý)
        Task t2 = new Task("Chuẩn bị báo cáo tuần", "Tổng hợp kết quả công việc và gửi ban giám đốc.", "27/09/2026", "10:00", 0);
        t2.setCategoryId(1);
        addTask(t2);

        // Task 3: Completed (Hoàn thành)
        Task t3 = new Task("Họp nhóm khởi động", "Thảo luận phân công nhiệm vụ cho các thành viên.", "25/09/2026", "14:00", 2);
        t3.setCategoryId(2);
        addTask(t3);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBTASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }



    // --- USER METHODS ---
    public long registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_USERNAME, user.getUsername());
        values.put(KEY_USER_EMAIL, user.getEmail());
        values.put(KEY_USER_PASSWORD, user.getPassword());
        values.put(KEY_USER_FULL_NAME, user.getFullName());
        values.put(KEY_USER_AVATAR, user.getAvatarUri());

        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    public User checkLogin(String account, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE (" + KEY_USER_EMAIL + " = ? OR " + KEY_USER_USERNAME + " = ?) AND " + KEY_USER_PASSWORD + " = ?",
                new String[]{account, account, password});
        if (cursor.moveToFirst()) {
            User user = new User();
            user.setId(cursor.getInt(0));
            user.setUsername(cursor.getString(1));
            user.setEmail(cursor.getString(2));
            user.setPassword(cursor.getString(3));
            user.setFullName(cursor.getString(4));
            user.setAvatarUri(cursor.getString(5));
            cursor.close();
            db.close();
            return user;
        }
        cursor.close();
        db.close();
        return null;
    }

    public void updateUserProfile(int userId, String fullName, String email, String avatarUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (fullName != null) {
            values.put(KEY_USER_FULL_NAME, fullName);
            values.put(KEY_USER_USERNAME, fullName);
        }
        if (email != null) {
            values.put(KEY_USER_EMAIL, email);
        }
        if (avatarUri != null) {
            values.put(KEY_USER_AVATAR, avatarUri);
        }
        db.update(TABLE_USERS, values, KEY_USER_ID + " = ?", new String[]{"" + userId});
        db.close();
    }



    public int getOrCreateCategoryByName(String categoryNameInput) {
        String name = (categoryNameInput == null || categoryNameInput.trim().isEmpty()) ? "Cá nhân" : categoryNameInput.trim();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_CATEGORIES + " WHERE LOWER(name) = LOWER(?)",
                new String[]{name});

        int categoryId;
        if (cursor.moveToFirst()) {
            categoryId = cursor.getInt(0);
            cursor.close();
        } else {
            cursor.close();
            ContentValues values = new ContentValues();
            values.put(KEY_CAT_NAME, name);
            values.put(KEY_CAT_COLOR, "#0E33F3");
            values.put(KEY_CAT_ICON, "ic_work");
            categoryId = (int) db.insert(TABLE_CATEGORIES, null, values);
        }

        db.close();
        return categoryId > 0 ? categoryId : 1;
    }

    // --- TASK CRUD ---


    private ContentValues createTaskContentValues(Task task) {
        ContentValues values = new ContentValues();
        values.put(KEY_TASK_TITLE, task.getTitle());
        values.put(KEY_TASK_DESC, task.getDesc());
        values.put(KEY_TASK_DATE, task.getDate());
        values.put(KEY_TASK_TIME, task.getTime());
        values.put(KEY_TASK_STATUS, task.getStatus());
        values.put(KEY_TASK_PRIORITY, task.getPriority());
        values.put(KEY_TASK_CATEGORY_ID, task.getCategoryId());
        values.put(KEY_TASK_ATTACHMENT, task.getAttachmentUri());
        values.put(KEY_TASK_REPEAT, task.getRepeatRule());
        values.put(KEY_TASK_REMINDER_LEAD, task.getReminderLeadTime());
        values.put(KEY_TASK_IS_DELETED, task.getIsDeleted());
        return values;
    }

    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = createTaskContentValues(task);
        values.put(KEY_TASK_CREATED_AT, System.currentTimeMillis());

        long taskId = db.insert(TABLE_TASKS, null, values);

        // Save Subtasks
        if (taskId > 0 && task.getSubtasks() != null) {
            for (Subtask s : task.getSubtasks()) {
                ContentValues subVal = new ContentValues();
                subVal.put(KEY_SUB_TASK_ID, taskId);
                subVal.put(KEY_SUB_TITLE, s.getTitle());
                subVal.put(KEY_SUB_IS_COMPLETED, s.isCompleted() ? 1 : 0);
                db.insert(TABLE_SUBTASKS, null, subVal);
            }
        }

        db.close();
        return taskId;
    }

    @SuppressWarnings("UnusedReturnValue")
    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = createTaskContentValues(task);

        int result = db.update(TABLE_TASKS, values, KEY_TASK_ID + " = ?",
                new String[]{"" + task.getId()});

        // Replace Subtasks
        db.delete(TABLE_SUBTASKS, KEY_SUB_TASK_ID + " = ?", new String[]{"" + task.getId()});
        if (task.getSubtasks() != null) {
            for (Subtask s : task.getSubtasks()) {
                ContentValues subVal = new ContentValues();
                subVal.put(KEY_SUB_TASK_ID, task.getId());
                subVal.put(KEY_SUB_TITLE, s.getTitle());
                subVal.put(KEY_SUB_IS_COMPLETED, s.isCompleted() ? 1 : 0);
                db.insert(TABLE_SUBTASKS, null, subVal);
            }
        }

        db.close();
        return result;
    }

    public void softDeleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TASK_IS_DELETED, 1);
        db.update(TABLE_TASKS, values, KEY_TASK_ID + " = ?", new String[]{"" + taskId});
        db.close();
    }

    public void updateTaskFavoriteStatus(int taskId, int isFavorite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_favorite", isFavorite);
        db.update(TABLE_TASKS, values, KEY_TASK_ID + " = ?", new String[]{"" + taskId});
        db.close();
    }

    public void restoreTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TASK_IS_DELETED, 0);
        db.update(TABLE_TASKS, values, KEY_TASK_ID + " = ?", new String[]{"" + taskId});
        db.close();
    }

    public void permanentlyDeleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SUBTASKS, KEY_SUB_TASK_ID + " = ?", new String[]{"" + taskId});
        db.delete(TABLE_TASKS, KEY_TASK_ID + " = ?", new String[]{"" + taskId});
        db.close();
    }

    public void clearTrash() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_TASKS + " WHERE " + KEY_TASK_IS_DELETED + " = 1", null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                db.delete(TABLE_SUBTASKS, KEY_SUB_TASK_ID + " = ?", new String[]{"" + id});
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.delete(TABLE_TASKS, KEY_TASK_IS_DELETED + " = 1", null);
        db.close();
    }

    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SUBTASKS, null, null);
        db.delete(TABLE_TASKS, null, null);
        db.close();
    }

    // --- TASK QUERIES & FILTERS ---
    public List<Task> getFilteredTasks(int filterStatus, String keyword, int categoryId, int priority) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder query = new StringBuilder(
                "SELECT t.*, c.name, c.color FROM " + TABLE_TASKS + " t " +
                        "LEFT JOIN " + TABLE_CATEGORIES + " c ON t." + KEY_TASK_CATEGORY_ID + " = c.id " +
                        "WHERE t." + KEY_TASK_IS_DELETED + " = 0"
        );

        List<String> argsList = new ArrayList<>();

        if (filterStatus == 1) { // Chưa xong (Status 0 hoặc 1)
            query.append(" AND t.").append(KEY_TASK_STATUS).append(" IN (0, 1)");
        } else if (filterStatus == 2) { // Đã xong (Status 2)
            query.append(" AND t.").append(KEY_TASK_STATUS).append(" = 2");
        } else if (filterStatus == 5) { // Yêu thích
            query.append(" AND t.is_favorite = 1");
        } else if (filterStatus > 2) {
            query.append(" AND t.").append(KEY_TASK_STATUS).append(" = ?");
            argsList.add("" + filterStatus);
        }

        if (categoryId > 0) {
            query.append(" AND t.").append(KEY_TASK_CATEGORY_ID).append(" = ?");
            argsList.add("" + categoryId);
        }

        if (priority > 0) {
            query.append(" AND t.").append(KEY_TASK_PRIORITY).append(" = ?");
            argsList.add("" + priority);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append(" AND (t.").append(KEY_TASK_TITLE).append(" LIKE ? OR t.")
                    .append(KEY_TASK_DESC).append(" LIKE ?)");
            String searchPattern = "%" + keyword.trim() + "%";
            argsList.add(searchPattern);
            argsList.add(searchPattern);
        }

        query.append(" ORDER BY t.").append(KEY_TASK_ID).append(" DESC");

        String[] args = argsList.isEmpty() ? null : argsList.toArray(new String[0]);
        Cursor cursor = db.rawQuery(query.toString(), args);

        if (cursor.moveToFirst()) {
            do {
                Task task = parseTaskFromCursor(cursor);
                task.setSubtasks(getSubtasksForTask(db, task.getId()));
                taskList.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return taskList;
    }

    public List<Task> getTasksForDate(String targetDate) {
        List<Task> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT t.*, c.name, c.color FROM " + TABLE_TASKS + " t " +
                "LEFT JOIN " + TABLE_CATEGORIES + " c ON t.category_id = c.id " +
                "WHERE t.is_deleted = 0 AND t.date = ? ORDER BY t.time ASC", new String[]{targetDate});
        if (cursor.moveToFirst()) {
            do {
                Task task = parseTaskFromCursor(cursor);
                task.setSubtasks(getSubtasksForTask(db, task.getId()));
                list.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public List<Task> getTasksByStatus(int status) {
        List<Task> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT t.*, c.name, c.color FROM " + TABLE_TASKS + " t " +
                "LEFT JOIN " + TABLE_CATEGORIES + " c ON t.category_id = c.id " +
                "WHERE t.is_deleted = 0 AND t.status = ? ORDER BY t.id DESC", new String[]{"" + status});
        if (cursor.moveToFirst()) {
            do {
                Task task = parseTaskFromCursor(cursor);
                task.setSubtasks(getSubtasksForTask(db, task.getId()));
                list.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public List<Task> getTrashTasks() {
        List<Task> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT t.*, c.name, c.color FROM " + TABLE_TASKS + " t " +
                "LEFT JOIN " + TABLE_CATEGORIES + " c ON t.category_id = c.id " +
                "WHERE t.is_deleted = 1 ORDER BY t.id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                Task task = parseTaskFromCursor(cursor);
                task.setSubtasks(getSubtasksForTask(db, task.getId()));
                list.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    private Task parseTaskFromCursor(Cursor cursor) {
        Task task = new Task();
        int idxId = cursor.getColumnIndex(KEY_TASK_ID);
        int idxTitle = cursor.getColumnIndex(KEY_TASK_TITLE);
        int idxDesc = cursor.getColumnIndex(KEY_TASK_DESC);
        int idxDate = cursor.getColumnIndex(KEY_TASK_DATE);
        int idxTime = cursor.getColumnIndex(KEY_TASK_TIME);
        int idxStatus = cursor.getColumnIndex(KEY_TASK_STATUS);
        int idxPriority = cursor.getColumnIndex(KEY_TASK_PRIORITY);
        int idxCatId = cursor.getColumnIndex(KEY_TASK_CATEGORY_ID);
        int idxAttachment = cursor.getColumnIndex(KEY_TASK_ATTACHMENT);
        int idxRepeat = cursor.getColumnIndex(KEY_TASK_REPEAT);
        int idxReminder = cursor.getColumnIndex(KEY_TASK_REMINDER_LEAD);
        int idxIsDeleted = cursor.getColumnIndex(KEY_TASK_IS_DELETED);
        int idxFav = cursor.getColumnIndex("is_favorite");
        int idxCreatedAt = cursor.getColumnIndex(KEY_TASK_CREATED_AT);
        int idxCatName = cursor.getColumnIndex("name");
        int idxCatColor = cursor.getColumnIndex("color");

        if (idxId != -1) task.setId(cursor.getInt(idxId));
        if (idxTitle != -1) task.setTitle(cursor.getString(idxTitle));
        if (idxDesc != -1) task.setDesc(cursor.getString(idxDesc));
        if (idxDate != -1) task.setDate(cursor.getString(idxDate));
        if (idxTime != -1) task.setTime(cursor.getString(idxTime));
        if (idxStatus != -1) task.setStatus(cursor.getInt(idxStatus));
        if (idxPriority != -1) task.setPriority(cursor.getInt(idxPriority));
        if (idxCatId != -1) task.setCategoryId(cursor.getInt(idxCatId));
        if (idxAttachment != -1) task.setAttachmentUri(cursor.getString(idxAttachment));
        if (idxRepeat != -1) task.setRepeatRule(cursor.getString(idxRepeat));
        if (idxReminder != -1) task.setReminderLeadTime(cursor.getInt(idxReminder));
        if (idxIsDeleted != -1) task.setIsDeleted(cursor.getInt(idxIsDeleted));
        if (idxFav != -1) task.setIsFavorite(cursor.getInt(idxFav));
        if (idxCreatedAt != -1) task.setCreatedAt(cursor.getLong(idxCreatedAt));
        if (idxCatName != -1) task.setCategoryName(cursor.getString(idxCatName));
        if (idxCatColor != -1) task.setCategoryColor(cursor.getString(idxCatColor));

        return task;
    }

    private List<Subtask> getSubtasksForTask(SQLiteDatabase db, int taskId) {
        List<Subtask> subtasks = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SUBTASKS + " WHERE " + KEY_SUB_TASK_ID + " = ?",
                new String[]{"" + taskId});
        if (cursor.moveToFirst()) {
            do {
                Subtask s = new Subtask(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getInt(3) == 1
                );
                subtasks.add(s);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return subtasks;
    }

    // --- STATISTICS & METRICS ---
    @SuppressWarnings("ReassignedVariable")
    public int getTotalCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TASKS + " WHERE " + KEY_TASK_IS_DELETED + " = 0", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }





    // Calculate Streak: consecutive days with at least 1 completed task
    @SuppressWarnings("ReassignedVariable")
    public int getStreakDays() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT " + KEY_TASK_DATE + " FROM " + TABLE_TASKS + " WHERE " + KEY_TASK_IS_DELETED + " = 0 AND " + KEY_TASK_STATUS + " = 2 ORDER BY (substr(" + KEY_TASK_DATE + ", 7, 4) || '-' || substr(" + KEY_TASK_DATE + ", 4, 2) || '-' || substr(" + KEY_TASK_DATE + ", 1, 2)) DESC", null);
        int streak = 0;
        if (cursor.moveToFirst()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar checkCal = Calendar.getInstance();
            checkCal.set(Calendar.HOUR_OF_DAY, 0);
            checkCal.set(Calendar.MINUTE, 0);
            checkCal.set(Calendar.SECOND, 0);
            checkCal.set(Calendar.MILLISECOND, 0);

            do {
                String dateStr = cursor.getString(0);
                try {
                    Date d = sdf.parse(dateStr);
                    if (d != null) {
                        Calendar taskCal = Calendar.getInstance();
                        taskCal.setTime(d);
                        taskCal.set(Calendar.HOUR_OF_DAY, 0);
                        taskCal.set(Calendar.MINUTE, 0);
                        taskCal.set(Calendar.SECOND, 0);
                        taskCal.set(Calendar.MILLISECOND, 0);

                        long diffInMs = checkCal.getTimeInMillis() - taskCal.getTimeInMillis();
                        long diffInDays = diffInMs / (1000 * 60 * 60 * 24);

                        if (diffInDays >= 0 && diffInDays <= 1) {
                            streak++;
                            checkCal.setTime(d);
                            checkCal.set(Calendar.HOUR_OF_DAY, 0);
                            checkCal.set(Calendar.MINUTE, 0);
                            checkCal.set(Calendar.SECOND, 0);
                            checkCal.set(Calendar.MILLISECOND, 0);
                        } else {
                            break;
                        }
                    }
                } catch (Exception ignored) {}
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return streak;
    }
}
