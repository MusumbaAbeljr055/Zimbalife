package com.example.azimbalife.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.azimbalife.Domain.CategoryModel;
import com.example.azimbalife.Domain.DoctorsModel;
import com.example.azimbalife.Respository.MainRepository;

import java.util.List;

public class MainViewModel  extends ViewModel {
    private  final MainRepository repository;


    public MainViewModel() {
        this.repository = new MainRepository();
    }
    public LiveData<List<CategoryModel>> loadCategory(){return  repository.loadCategory();}


    public LiveData<List<DoctorsModel>> loadDoctors(){return  repository.loadDoctor();}

}
