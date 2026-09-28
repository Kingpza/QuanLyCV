package com.example.quanlycv.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Task implements Serializable {
    private int id;
    private String title;
    private String desc;
    private String date;
    private String time;
    private int status; // 0: Chưa làm, 1: Đang làm, 2: Hoàn thành
    private int priority; // 1: Thấp, 2: Bình thường, 3: Cao, 4: Khẩn cấp
    private int categoryId;
    private String categoryName;
    private String categoryColor;
    private String attachmentUri;
    private String repeatRule; // "NONE", "DAILY", "WEEKLY", "MONTHLY"
    private int reminderLeadTime; // 0: Đặt đúng giờ, 10: 10 phút trước, 60: 1 giờ trước, 1440: 1 ngày trước
    private int isDeleted; // 0: Active, 1: Trong thùng rác
    private int isFavorite = 0; // 0: Normal, 1: Starred / Favorite / Pinned
    private long createdAt;

    private List<Subtask> subtasks = new ArrayList<>();

    public Task() {
        this.status = 0;
        this.priority = 2; // Normal
        this.repeatRule = "NONE";
        this.reminderLeadTime = 0;
        this.isDeleted = 0;
        this.createdAt = System.currentTimeMillis();
    }

    public Task(int id, String title, String desc, String date, String time, int status, int priority, int categoryId, String repeatRule) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.date = date;
        this.time = time;
        this.status = status;
        this.priority = priority;
        this.categoryId = categoryId;
        this.repeatRule = repeatRule != null ? repeatRule : "NONE";
        this.reminderLeadTime = 0;
        this.isDeleted = 0;
        this.createdAt = System.currentTimeMillis();
    }

    public Task(String title, String desc, String date, String time, int status) {
        this();
        this.title = title;
        this.desc = desc;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public String getAttachmentUri() {
        return attachmentUri;
    }

    public void setAttachmentUri(String attachmentUri) {
        this.attachmentUri = attachmentUri;
    }

    public String getRepeatRule() {
        return repeatRule;
    }

    public void setRepeatRule(String repeatRule) {
        this.repeatRule = repeatRule;
    }

    public int getReminderLeadTime() {
        return reminderLeadTime;
    }

    public void setReminderLeadTime(int reminderLeadTime) {
        this.reminderLeadTime = reminderLeadTime;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }

    public void setIsFavorite(int isFavorite) {
        this.isFavorite = isFavorite;
    }

    public boolean isFavorite() {
        return isFavorite == 1;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks != null ? subtasks : new ArrayList<>();
    }
}
