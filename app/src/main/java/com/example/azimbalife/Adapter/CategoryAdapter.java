package com.example.azimbalife.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.azimbalife.Domain.CategoryModel;
import com.example.azimbalife.R;
import com.example.azimbalife.databinding.ViewholderCategoryBinding;

import java.util.List;

public class CategoryAdapter  extends RecyclerView.Adapter<CategoryAdapter.Viewholder> {
    private List<CategoryModel> items;

    public CategoryAdapter(List<CategoryModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public CategoryAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderCategoryBinding binding = ViewholderCategoryBinding.inflate(LayoutInflater.from(parent.getContext()),parent,
                false);

        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.Viewholder holder, int position) {
        CategoryModel item = items.get(position);

        holder.binding.titleCat.setText(item.getName());

        Glide.with(holder.itemView.getContext())
                .load(item.getPicture())
                .into(holder.binding.picCat)
                ;

        int [] backgrounds = {
                R.drawable.blue_rec_bg,
                R.drawable.blue_btn_bg,
                R.drawable.purple_rec_bg,
                R.drawable.orange_rec_bg
        };
        int backgroundRes = backgrounds[position%4];
        holder.binding.getRoot().setBackgroundResource(backgroundRes);


    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    public class Viewholder extends RecyclerView.ViewHolder {
        ViewholderCategoryBinding binding;



        public Viewholder(ViewholderCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
