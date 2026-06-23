package com.example.shoeapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.shoeapp.data.entity.Brand;
import com.example.shoeapp.data.entity.Category;
import com.example.shoeapp.data.entity.Color;
import com.example.shoeapp.data.entity.Product;
import com.example.shoeapp.data.entity.ProductImg;
import com.example.shoeapp.data.entity.ProductVariant;
import com.example.shoeapp.data.entity.Size;
import java.util.List;

@Dao
public interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Product product);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertBrand(Brand brand);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertCategory(Category category);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertColor(Color color);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertSize(Size size);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertVariant(ProductVariant variant);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertProductImage(ProductImg productImg);

    @Query("SELECT * FROM product WHERE is_available = 1 AND is_discontinue = 0 ORDER BY id ASC")
    List<Product> getAllProducts();

    @Query("SELECT * FROM product WHERE id = :id LIMIT 1")
    Product getProductById(int id);

    @Query("SELECT * FROM product WHERE shoe_category = :categoryId")
    List<Product> getProductsByCategory(int categoryId);

    @Query("SELECT COUNT(*) FROM product")
    int countProducts();

    @Query("SELECT name FROM brand WHERE id = :brandId LIMIT 1")
    String getBrandName(int brandId);

    @Query("SELECT name FROM category WHERE id = :categoryId LIMIT 1")
    String getCategoryName(int categoryId);

    @Query("SELECT COALESCE(SUM(stock), 0) FROM product_variant WHERE product_id = :productId AND is_discontinue_variant = 0")
    int getProductStock(int productId);

    @Query("SELECT img_url FROM product_img WHERE product_id = :productId AND is_thumbnail = 1 AND is_active = 1 ORDER BY sort_order ASC LIMIT 1")
    String getThumbnailUrl(int productId);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);
}
