package com.example.azimbalife.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.azimbalife.Activity.DetailActivity;
import com.example.azimbalife.Domain.DoctorsModel;
import com.example.azimbalife.databinding.ViewholderTopDoctorsBinding;

import java.util.List;

public class TopDoctorAdapter extends RecyclerView.Adapter<TopDoctorAdapter.Viewholder> {

    private final List<DoctorsModel> items;
    private final Context context;

    // ✅ Constructor now takes Context instead of AllDoctorsActivity
    public TopDoctorAdapter(List<DoctorsModel> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public TopDoctorAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderTopDoctorsBinding binding = ViewholderTopDoctorsBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TopDoctorAdapter.Viewholder holder, int position) {
        DoctorsModel doctorsModel = items.get(position);
        holder.binding.nameTxt.setText(doctorsModel.getName());
        holder.binding.special.setText(doctorsModel.getSpecial());
        holder.binding.rating.setText(String.valueOf(doctorsModel.getRating()));
        holder.binding.patiensTxt.setText(doctorsModel.getPatiens() + " years");

        Glide.with(context)
                .load(doctorsModel.getPicture())
                .apply(new RequestOptions().transform(new CenterCrop()))
                .into(holder.binding.img);

        holder.binding.getRoot().setOnClickListener(v -> {
            Intent i = new Intent(context, DetailActivity.class);
            i.putExtra("object", doctorsModel);
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        private final ViewholderTopDoctorsBinding binding;

        public Viewholder(ViewholderTopDoctorsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
