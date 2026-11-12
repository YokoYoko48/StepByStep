package com.example.stepbystep;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {

    private Context context;
    private List<ActivityItem> activityList;
    private DBHelper dbHelper;

    public ActivityAdapter(Context context, List<ActivityItem> activityList) {
        this.context = context;
        this.activityList = activityList;
        this.dbHelper = new DBHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityItem item = activityList.get(position);

        holder.tvHistoryDate.setText(item.getDate());
        holder.tvHistorySteps.setText(String.valueOf(item.getSteps()) + " ก้าว");
        holder.tvHistoryShakes.setText(String.valueOf(item.getShakes()) + " ครั้ง");
        holder.tvHistoryDuration.setText(item.getDuration());

        // ปุ่มลบรายการประวัติ
        holder.btnDelete.setOnClickListener(v -> {
            dbHelper.deleteActivity(item.getId());
            activityList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, activityList.size());
        });
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHistoryDate, tvHistorySteps, tvHistoryShakes, tvHistoryDuration;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHistoryDate = itemView.findViewById(R.id.tvHistoryDate);
            tvHistorySteps = itemView.findViewById(R.id.tvHistorySteps);
            tvHistoryShakes = itemView.findViewById(R.id.tvHistoryShakes);
            tvHistoryDuration = itemView.findViewById(R.id.tvHistoryDuration);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}