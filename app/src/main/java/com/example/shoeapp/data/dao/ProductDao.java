package com.example.shoeapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.shoeapp.data.entity.*;
import java.util.List;

@Dao
public interface ProductDao {

    // ── Product ──────────────────────────────────────
    @Insert void insert(Product product);
    @Update void update(Product product);
    @Delete void delete(Product product);
    @Query("SELECT * FROM product") List<Product> getAllProducts();
    @Query("SELECT * FROM product WHERE shoe_category = :categoryId")
    List<Product> getProductsByCategory(int categoryId);
    @Query("SELECT * FROM product WHERE id = :id") Product getProductById(int id);

    // ── Brand ─────────────────────────────────────────
    @Insert void insertBrand(Brand brand);
    @Update void updateBrand(Brand brand);
    @Delete void deleteBrand(Brand brand);
    @Query("SELECT * FROM brand") List<Brand> getAllBrands();
    @Query("SELECT * FROM brand WHERE id = :id") Brand getBrandById(int id);

    // ── Color ─────────────────────────────────────────
    @Insert void insertColor(Color color);
    @Update void updateColor(Color color);
    @Delete void deleteColor(Color color);
    @Query("SELECT * FROM color") List<Color> getAllColors();

    // ── Size ──────────────────────────────────────────
    @Insert void insertSize(Size size);
    @Update void updateSize(Size size);
    @Delete void deleteSize(Size size);
    @Query("SELECT * FROM size ORDER BY sort_order ASC") List<Size> getAllSizes();

    // ── ProductImg ────────────────────────────────────
    @Insert void insertProductImg(ProductImg img);
    @Update void updateProductImg(ProductImg img);
    @Delete void deleteProductImg(ProductImg img);
    @Query("SELECT * FROM product_img WHERE product_id = :productId")
    List<ProductImg> getImagesByProduct(int productId);
    @Query("SELECT * FROM product_img WHERE product_id = :productId AND is_thumbnail = 1 LIMIT 1")
    ProductImg getThumbnail(int productId);

    // ── ProductVariant ────────────────────────────────
    @Insert void insertProductVariant(ProductVariant variant);
    @Update void updateProductVariant(ProductVariant variant);
    @Delete void deleteProductVariant(ProductVariant variant);
    @Query("SELECT * FROM product_variant WHERE product_id = :productId")
    List<ProductVariant> getVariantsByProduct(int productId);
    @Query("SELECT * FROM product_variant WHERE product_id = :pid AND size_id = :sid AND color_id = :cid LIMIT 1")
    ProductVariant getVariant(int pid, int sid, int cid);

    // ── Promotion ─────────────────────────────────────
    @Insert void insertPromotion(Promotion promotion);
    @Update void updatePromotion(Promotion promotion);
    @Delete void deletePromotion(Promotion promotion);
    @Query("SELECT * FROM promotion WHERE is_active = 1") List<Promotion> getActivePromotions();

    // ── PromotionProduct ──────────────────────────────
    @Insert void insertPromotionProduct(PromotionProduct pp);
    @Delete void deletePromotionProduct(PromotionProduct pp);
    @Query("SELECT * FROM promotion_product WHERE promotion_id = :promotionId")
    List<PromotionProduct> getProductsByPromotion(int promotionId);

    // ── ProductReview ─────────────────────────────────
    @Insert void insertReview(ProductReview review);
    @Update void updateReview(ProductReview review);
    @Delete void deleteReview(ProductReview review);
    @Query("SELECT * FROM product_review WHERE product_id = :productId ORDER BY created_at DESC")
    List<ProductReview> getReviewsByProduct(int productId);
    @Query("SELECT AVG(rating) FROM product_review WHERE product_id = :productId")
    float getAverageRating(int productId);
}
