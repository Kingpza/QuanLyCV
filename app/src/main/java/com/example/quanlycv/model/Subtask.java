package com.example.quanlycv.model;

import java.io.Serializable;

@SuppressWarnings("unused")
public class Subtask implements Serializable {
    private int id;
    private int taskId;
    private String title;
    private boolean isCompleted;

    public Subtask() {}

    public Subtask(int id, int taskId, String title, boolean isCompleted) {
        this.id = id;
        this.taskId = taskId;
        this.title = title;
        this.isCompleted = isCompleted;
    }

    public Subtask(int taskId, String title, boolean isCompleted) {
        this.taskId = taskId;
        this.title = title;
        this.isCompleted = isCompleted;
    }

    public Subtask(String title, boolean isCompleted) {
        this.title = title;
        this.isCompleted = isCompleted;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
