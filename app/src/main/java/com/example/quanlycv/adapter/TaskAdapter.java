package com.example.quanlycv.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.quanlycv.R;
import com.example.quanlycv.database.DatabaseHelper;
import com.example.quanlycv.model.Subtask;
import com.example.quanlycv.model.Task;
import com.example.quanlycv.utils.AlarmHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TaskAdapter extends BaseAdapter {

    public interface OnTaskStatusChangeListener {
        void onStatusChanged();
    }

    public interface OnTaskActionListener {
        void onEditTask(Task task);
        @SuppressWarnings("unused")
        void onDeleteTask(Task task);
    }

    private final Context context;
    private List<Task> taskList;
    private final DatabaseHelper dbHelper;
    private OnTaskStatusChangeListener listener;
    private OnTaskActionListener actionListener;

    private final Set<Integer> expandedTaskIdSet = new HashSet<>();

    public TaskAdapter(Context context, List<Task> taskList, DatabaseHelper dbHelper) {
        this.context = context;
        this.taskList = taskList != null ? taskList : new ArrayList<>();
        this.dbHelper = dbHelper;
    }

    public void setOnTaskStatusChangeListener(OnTaskStatusChangeListener listener) {
        this.listener = listener;
    }

    public void setOnTaskActionListener(OnTaskActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void updateList(List<Task> newList) {
        this.taskList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Task getItem(int position) {
        if (position >= 0 && position < taskList.size()) {
            return taskList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        Task task = getItem(position);
        return task != null ? task.getId() : position;
    }

    static class ViewHolder {
        View vCategoryIndicator;
        TextView tvTaskInitialBadge;
        TextView tvTaskTitle;
        TextView tvCategoryBadge;
        TextView tvPriorityBadge;
        TextView tvTaskDateTime;
        ImageView imgCheckmarkCircle;
        ImageView imgExpandArrow;
        LinearLayout llTaskFooter;
        TextView tvCommentCount;
        TextView tvAttachmentCount;
        ImageView imgStarFavorite;
        LinearLayout llSubtasksContainer;
        LinearLayout llSubtaskList;
        View cardTaskMain;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
            holder = new ViewHolder();
            holder.vCategoryIndicator = view.findViewById(R.id.vCategoryIndicator);
            holder.tvTaskInitialBadge = view.findViewById(R.id.tvTaskInitialBadge);
            holder.tvTaskTitle = view.findViewById(R.id.tvTaskTitle);
            holder.tvCategoryBadge = view.findViewById(R.id.tvCategoryBadge);
            holder.tvPriorityBadge = view.findViewById(R.id.tvPriorityBadge);
            holder.tvTaskDateTime = view.findViewById(R.id.tvTaskDateTime);
            holder.imgCheckmarkCircle = view.findViewById(R.id.imgCheckmarkCircle);
            holder.imgExpandArrow = view.findViewById(R.id.imgExpandArrow);
            holder.llTaskFooter = view.findViewById(R.id.llTaskFooter);
            holder.tvCommentCount = view.findViewById(R.id.tvCommentCount);
            holder.tvAttachmentCount = view.findViewById(R.id.tvAttachmentCount);
            holder.imgStarFavorite = view.findViewById(R.id.imgStarFavorite);
            holder.llSubtasksContainer = view.findViewById(R.id.llSubtasksContainer);
            holder.llSubtaskList = view.findViewById(R.id.llSubtaskList);
            holder.cardTaskMain = view.findViewById(R.id.cardTaskMain);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        Task task = getItem(position);
        if (task != null) {
            // Assign Badge Initial Letter and Color based on Category ID or Title
            String title = task.getTitle() != null ? task.getTitle() : "Công việc";
            String initial = title.substring(0, 1).toUpperCase();
            holder.tvTaskInitialBadge.setText(initial);

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

            holder.tvTaskInitialBadge.setBackgroundResource(badgeBgRes);
            if (holder.vCategoryIndicator != null) {
                holder.vCategoryIndicator.setBackgroundResource(badgeBgRes);
            }

            // Title
            holder.tvTaskTitle.setText(title);

            // Category Metadata
            String catName = task.getCategoryName() != null ? task.getCategoryName() : context.getString(R.string.default_user_name);
            holder.tvCategoryBadge.setText(catName);

            // Priority Metadata Tag
            switch (task.getPriority()) {
                case 4:
                    holder.tvPriorityBadge.setText(R.string.priority_urgent);
                    holder.tvPriorityBadge.setTextColor(ContextCompat.getColor(context, R.color.red_delete));
                    break;
                case 3:
                    holder.tvPriorityBadge.setText(R.string.priority_high);
                    holder.tvPriorityBadge.setTextColor(ContextCompat.getColor(context, R.color.cat_magenta));
                    break;
                case 1:
                    holder.tvPriorityBadge.setText(R.string.priority_low);
                    holder.tvPriorityBadge.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                    break;
                case 2:
                default:
                    holder.tvPriorityBadge.setText(R.string.priority_normal);
                    holder.tvPriorityBadge.setTextColor(ContextCompat.getColor(context, R.color.cat_blue));
                    break;
            }

            // Deadline Date & Time Metadata
            String dateStr = task.getDate() != null ? task.getDate() : "";
            String timeStr = task.getTime() != null ? task.getTime() : "";
            String dateTimeText = "🕒 " + (dateStr.isEmpty() ? "" : dateStr + " ") + timeStr;
            holder.tvTaskDateTime.setText(dateTimeText.trim());

            boolean isDone = (task.getStatus() == 2);
            boolean hasSubtasks = (task.getSubtasks() != null && !task.getSubtasks().isEmpty());

            // Checkmark Icon & Title Strikethrough Status
            if (isDone) {
                holder.imgCheckmarkCircle.setImageResource(R.drawable.ic_check_green_circle);
                holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.tvTaskTitle.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            } else {
                holder.imgCheckmarkCircle.setImageResource(R.drawable.ic_circle_outline);
                holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                holder.tvTaskTitle.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            }

            // Footer counts
            int subCount = task.getSubtasks() != null ? task.getSubtasks().size() : 0;
            holder.tvCommentCount.setText(String.format(Locale.getDefault(), "%d", subCount));
            holder.tvAttachmentCount.setText(task.getAttachmentUri() != null ? "1" : "0");

            // Star Favorite Toggle
            boolean isFav = task.isFavorite();
            if (holder.imgStarFavorite != null) {
                if (isFav) {
                    holder.imgStarFavorite.setColorFilter(ContextCompat.getColor(context, R.color.status_pending)); // Gold
                } else {
                    holder.imgStarFavorite.setColorFilter(ContextCompat.getColor(context, R.color.icon_grey));
                }

                holder.imgStarFavorite.setOnClickListener(v -> {
                    int newFav = isFav ? 0 : 1;
                    task.setIsFavorite(newFav);
                    dbHelper.updateTaskFavoriteStatus(task.getId(), newFav);
                    notifyDataSetChanged();
                    if (listener != null) {
                        listener.onStatusChanged();
                    }
                });
            }

            // Expand Arrow
            boolean isExpanded = expandedTaskIdSet.contains(task.getId());
            holder.imgExpandArrow.setVisibility(hasSubtasks ? View.VISIBLE : View.GONE);
            holder.imgExpandArrow.setImageResource(isExpanded ? R.drawable.ic_double_arrow_up : R.drawable.ic_double_arrow_down);

            holder.imgExpandArrow.setOnClickListener(v -> {
                if (isExpanded) {
                    expandedTaskIdSet.remove(task.getId());
                } else {
                    expandedTaskIdSet.add(task.getId());
                }
                notifyDataSetChanged();
            });

            // Toggle task completion on Checkmark Circle click
            holder.imgCheckmarkCircle.setOnClickListener(v -> {
                int newStatus = isDone ? 0 : 2; // 2: Completed, 0: Pending
                task.setStatus(newStatus);
                dbHelper.updateTask(task);

                boolean isNewStatusCompleted = (newStatus == 2);
                if (isNewStatusCompleted) {
                    AlarmHelper.cancelAlarm(context, task.getId());
                } else {
                    AlarmHelper.setAlarm(context, task);
                }

                notifyDataSetChanged();
                if (listener != null) {
                    listener.onStatusChanged();
                }
            });

            // Render Subtasks list inside item
            if (hasSubtasks && isExpanded) {
                holder.llSubtasksContainer.setVisibility(View.VISIBLE);
                holder.llSubtaskList.removeAllViews();

                for (Subtask s : task.getSubtasks()) {
                    View subView = LayoutInflater.from(context).inflate(R.layout.item_subtask, holder.llSubtaskList, false);
                    CheckBox cbDone = subView.findViewById(R.id.cbSubtaskDone);
                    TextView tvSubTitle = subView.findViewById(R.id.tvSubtaskTitle);
                    View btnDelSub = subView.findViewById(R.id.btnDeleteSubtask);

                    tvSubTitle.setText(s.getTitle());
                    cbDone.setChecked(s.isCompleted());
                    if (s.isCompleted()) {
                        tvSubTitle.setPaintFlags(tvSubTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    } else {
                        tvSubTitle.setPaintFlags(tvSubTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    }

                    cbDone.setOnClickListener(v1 -> {
                        s.setCompleted(cbDone.isChecked());
                        dbHelper.updateTask(task);
                        notifyDataSetChanged();
                        if (listener != null) {
                            listener.onStatusChanged();
                        }
                    });

                    btnDelSub.setVisibility(View.GONE);
                    holder.llSubtaskList.addView(subView);
                }
            } else {
                holder.llSubtasksContainer.setVisibility(View.GONE);
            }

            // Click Card to Edit
            holder.cardTaskMain.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onEditTask(task);
                }
            });
        }

        return view;
    }
}
