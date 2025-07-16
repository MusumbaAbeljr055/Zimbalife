package com.example.azimbalife.Respository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.azimbalife.Domain.CategoryModel;
import com.example.azimbalife.Domain.DoctorsModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainRepository {

    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    public LiveData<List<CategoryModel>> loadCategory(){
        final MutableLiveData<List<CategoryModel>> listData = new MutableLiveData<>();
        DatabaseReference reference = firebaseDatabase.getReference("Category");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CategoryModel> list = new ArrayList<>();
                for (DataSnapshot childSnapshot:snapshot.getChildren()){
                    CategoryModel item = childSnapshot.getValue(CategoryModel.class);
                    if(item!=null)
                    {
                     list.add(item);
                    }
                }
                listData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return listData;

    }
    public LiveData<List<DoctorsModel>> loadDoctor(){
        final  MutableLiveData<List<DoctorsModel>> liveData = new MutableLiveData<>();
        DatabaseReference reference = firebaseDatabase.getReference("Doctors");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<DoctorsModel> lists = new ArrayList<>();
                for(DataSnapshot childSnapshot:snapshot.getChildren()){
                    DoctorsModel item = childSnapshot.getValue(DoctorsModel.class);
                    if(item!= null){
                        lists.add(item);

                    }

                }
                liveData.setValue(lists);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


            }
        });
        return liveData;
    }
}
