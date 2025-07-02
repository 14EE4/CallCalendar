package com.example.callcalendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.ViewHolder> {

    private List<RecordingItem> recordingItems; // 예시로 File 객체 리스트 사용, 실제로는 파일 경로 문자열이나 URI 등을 담은 모델 클래스가 더 좋음
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(RecordingItem item);
    }

    public RecordingAdapter(List<RecordingItem> recordingItems, OnItemClickListener listener) {
        this.recordingItems = recordingItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_recording, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecordingItem item = recordingItems.get(position);
        holder.textViewRecordingName.setText(item.getFileName());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recordingItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewRecordingName;

        ViewHolder(View itemView) {
            super(itemView);
            textViewRecordingName = itemView.findViewById(R.id.textViewRecordingName);
        }
    }

    // 데이터 업데이트를 위한 메서드 (선택 사항)
    public void updateData(List<RecordingItem> newItems) {
        this.recordingItems.clear();
        this.recordingItems.addAll(newItems);
        notifyDataSetChanged();
    }
}