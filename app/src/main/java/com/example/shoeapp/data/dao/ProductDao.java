package com.example.shoeapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.OnConflictStrategy;

import com.example.shoeapp.data.entity.*;
import com.example.shoeapp.data.model.ProductColorOption;
import com.example.shoeapp.data.model.ProductSizeOption;
import java.util.List;

@Dao
public interface ProductDao {

    // ── Insert with Ignore (for seed data / base operations) ─────────────
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
    void insertProductVariant(ProductVariant variant);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertVariant(ProductVariant variant);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertProductImg(ProductImg img);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertProductImage(ProductImg productImg);

    // ── Product ──────────────────────────────────────
    @Update
    void update(Product product);

    @Delete
    void delete(Product product);

    @Query("SELECT * FROM product")
    List<Product> getAllProducts();

    @Query("SELECT p.* FROM product p INNER JOIN category c ON p.shoe_category = c.id WHERE p.is_available = 1 AND p.is_discontinue = 0 AND c.is_active = 1")
    List<Product> getAllProductsActive();

    @Query("SELECT * FROM product WHERE shoe_category = :categoryId")
    List<Product> getProductsByCategory(int categoryId);

    @Query("SELECT p.* FROM product p INNER JOIN category c ON p.shoe_category = c.id WHERE p.shoe_category = :categoryId AND p.is_available = 1 AND p.is_discontinue = 0 AND c.is_active = 1")
    List<Product> getProductsByCategoryActive(int categoryId);

    @Query("SELECT * FROM product WHERE id = :id LIMIT 1")
    Product getProductById(int id);

    @Query("SELECT COUNT(*) FROM product")
    int countProducts();

    // ── Brand ─────────────────────────────────────────
    @Update
    void updateBrand(Brand brand);

    @Delete
    void deleteBrand(Brand brand);

    @Query("SELECT * FROM brand")
    List<Brand> getAllBrands();

    @Query("SELECT * FROM brand WHERE id = :id LIMIT 1")
    Brand getBrandById(int id);

    @Query("SELECT name FROM brand WHERE id = :brandId LIMIT 1")
    String getBrandName(int brandId);

    @Query("SELECT * FROM brand WHERE is_active = 1 ORDER BY id ASC")
    List<Brand> getAllBrandsActive();

    // ── Color ─────────────────────────────────────────
    @Update
    void updateColor(Color color);

    @Delete
    void deleteColor(Color color);

    @Query("SELECT * FROM color")
    List<Color> getAllColors();

    @Query("SELECT * FROM color WHERE id = :id LIMIT 1")
    Color getColorById(int id);

    // ── Size ──────────────────────────────────────────
    @Update
    void updateSize(Size size);

    @Delete
    void deleteSize(Size size);

    @Query("SELECT * FROM size ORDER BY sort_order ASC")
    List<Size> getAllSizes();

    @Query("SELECT * FROM size WHERE id = :id LIMIT 1")
    Size getSizeById(int id);

    // ── ProductImg ────────────────────────────────────
    @Update
    void updateProductImg(ProductImg img);

    @Delete
    void deleteProductImg(ProductImg img);

    @Query("SELECT * FROM product_img WHERE product_id = :productId")
    List<ProductImg> getImagesByProduct(int productId);

    @Query("SELECT * FROM product_img WHERE product_id = :productId AND is_thumbnail = 1 LIMIT 1")
    ProductImg getThumbnail(int productId);

    @Query("SELECT img_url FROM product_img WHERE product_id = :productId AND is_thumbnail = 1 AND is_active = 1 ORDER BY sort_order ASC LIMIT 1")
    String getThumbnailUrl(int productId);

    @Query("SELECT img_url FROM product_img WHERE product_id = :productId AND color_id = :colorId AND is_active = 1 ORDER BY is_thumbnail DESC, sort_order ASC LIMIT 1")
    String getImageUrl(int productId, int colorId);

    // ── Category ──────────────────────────────────────
    @Query("SELECT name FROM category WHERE id = :categoryId LIMIT 1")
    String getCategoryName(int categoryId);

    @Query("SELECT * FROM category WHERE is_active = 1 ORDER BY sort_order ASC")
    List<Category> getAllCategories();

    // ── ProductVariant ────────────────────────────────
    @Update
    void updateProductVariant(ProductVariant variant);

    @Delete
    void deleteProductVariant(ProductVariant variant);

    @Query("SELECT * FROM product_variant WHERE product_id = :productId")
    List<ProductVariant> getVariantsByProduct(int productId);

    @Query("SELECT COALESCE(SUM(stock), 0) FROM product_variant WHERE product_id = :productId AND is_discontinue_variant = 0")
    int getProductStock(int productId);

    @Query("SELECT DISTINCT c.id AS id, c.name AS name, c.hexcode AS hexcode FROM color c INNER JOIN product_variant pv ON pv.color_id = c.id WHERE pv.product_id = :productId AND pv.stock > 0 AND pv.is_discontinue_variant = 0 ORDER BY c.id ASC")
    List<ProductColorOption> getAvailableColors(int productId);

    @Query("SELECT s.id AS id, s.name AS name, COALESCE(pv.stock, 0) AS stock FROM size s INNER JOIN product_variant pv ON pv.size_id = s.id WHERE pv.product_id = :productId AND pv.color_id = :colorId AND pv.is_discontinue_variant = 0 ORDER BY s.sort_order ASC")
    List<ProductSizeOption> getAvailableSizes(int productId, int colorId);

    @Query("SELECT * FROM product_variant WHERE product_id = :productId AND color_id = :colorId AND size_id = :sizeId AND is_discontinue_variant = 0 LIMIT 1")
    ProductVariant getVariant(int productId, int colorId, int sizeId);

    // ── Promotion ─────────────────────────────────────
    @Insert
    void insertPromotion(Promotion promotion);

    @Update
    void updatePromotion(Promotion promotion);

    @Delete
    void deletePromotion(Promotion promotion);

    @Query("SELECT * FROM promotion WHERE is_active = 1")
    List<Promotion> getActivePromotions();

    // ── PromotionProduct ──────────────────────────────
    @Insert
    void insertPromotionProduct(PromotionProduct pp);

    @Delete
    void deletePromotionProduct(PromotionProduct pp);

    @Query("SELECT * FROM promotion_product WHERE promotion_id = :promotionId")
    List<PromotionProduct> getProductsByPromotion(int promotionId);

    // ── ProductReview ─────────────────────────────────
    @Insert
    void insertReview(ProductReview review);

    @Update
    void updateReview(ProductReview review);

    @Delete
    void deleteReview(ProductReview review);

    @Query("SELECT * FROM product_review WHERE product_id = :productId ORDER BY created_at DESC")
    List<ProductReview> getReviewsByProduct(int productId);

    @Query("SELECT AVG(rating) FROM product_review WHERE product_id = :productId")
    float getAverageRating(int productId);

    @Query("SELECT COUNT(*) FROM brand")
    int countBrands();

    @Query("SELECT COUNT(*) FROM category")
    int countCategories();

    @Query("SELECT COUNT(*) FROM color")
    int countColors();

    @Query("SELECT COUNT(*) FROM size")
    int countSizes();

    @Query("SELECT COUNT(*) FROM product_variant")
    int countVariants();
}
