package com.example.azimbalife.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.azimbalife.R;
import com.example.azimbalife.databinding.DateViewBinding;

import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.TimeViewholder> {
    private final List<String> timeSlots;
    private int selectedPosition = -1;
    private int lastSelectedPosition = -1;

    // Add listener interface
    public interface OnItemClickListener {
        void onItemClick(String selectedDate);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public DateAdapter(List<String> timeSlots) {
        this.timeSlots = timeSlots;
    }

    @NonNull
    @Override
    public DateAdapter.TimeViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DateViewBinding binding = DateViewBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false
        );

        return new TimeViewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DateAdapter.TimeViewholder holder, int position) {
        holder.bind(timeSlots.get(position), position, this);
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    public class TimeViewholder extends RecyclerView.ViewHolder {
        private final DateViewBinding binding;

        public TimeViewholder(DateViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String date, int position, DateAdapter adapter) {
            String[] dateparts = date.split("/");
            if (dateparts.length == 3) {
                binding.dayTxt.setText(dateparts[0]);
                binding.dayMonth.setText(dateparts[1] + " " + dateparts[2]);
                Context context = binding.getRoot().getContext();

                if (adapter.selectedPosition == position) {
                    binding.main.setBackgroundResource(R.drawable.blue_btn_bg);
                    binding.dayTxt.setTextColor(ContextCompat.getColor(context, R.color.white));
                    binding.dayMonth.setTextColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    binding.main.setBackgroundResource(R.drawable.light_grey_bg);
                    binding.dayTxt.setTextColor(ContextCompat.getColor(context, R.color.black));
                    binding.dayMonth.setTextColor(ContextCompat.getColor(context, R.color.black));
                }

                binding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.lastSelectedPosition = adapter.selectedPosition;
                        adapter.selectedPosition = position;
                        adapter.notifyItemChanged(adapter.lastSelectedPosition);
                        adapter.notifyItemChanged(adapter.selectedPosition);

                        // Notify listener of selected date
                        if (listener != null) {
                            listener.onItemClick(date);
                        }
                    }
                });
            }
        }
    }
}
