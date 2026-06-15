package com.example.shoeapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.shoeapp.data.entity.Product;
import java.util.List;

@Dao
public interface ProductDao {
    @Insert
    void insert(Product product);

    @Query("SELECT * FROM product")
    List<Product> getAllProducts();

    @Query("SELECT * FROM product WHERE shoe_category = :categoryId")
    List<Product> getProductsByCategory(int categoryId);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);
}
