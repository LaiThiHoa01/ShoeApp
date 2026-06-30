package com.example.shoeapp.model;
import java.util.List;

/**
 * Model dữ liệu sản phẩm — dùng cho AdminProductsActivity.
 * Trong thực tế có thể thay bằng Room Entity hoặc Retrofit DTO.
 */
public class Product {

    private final int    id;
    private final String name;
    private final String brand;       // Ví dụ: "NovaSole · Sneakers"
    private final String category;    // "Sneakers" | "Running" | "Casual"
    private final double price;
    private final double originalPrice;
    private final int    stock;
    private final boolean isNew;
    private final List<Integer> sizes;
    private final float  rating;
    private final int    reviewCount;
    private final int    imageResId;  // R.drawable.xxx
    private final String imageUrl;
    private final boolean isAvailable;
    private final boolean isDiscontinued;

    public Product(int id, String name, String brand, String category,
                   double price, double originalPrice, int stock,
                   boolean isNew, List<Integer> sizes,
                   float rating, int reviewCount, int imageResId) {
        this(id, name, brand, category, price, originalPrice, stock, isNew, sizes, rating, reviewCount, imageResId, "", true, false);
    }

    public Product(int id, String name, String brand, String category,
                   double price, double originalPrice, int stock,
                   boolean isNew, List<Integer> sizes,
                   float rating, int reviewCount, int imageResId,
                   String imageUrl) {
        this(id, name, brand, category, price, originalPrice, stock, isNew, sizes, rating, reviewCount, imageResId, imageUrl, true, false);
    }

    public Product(int id, String name, String brand, String category,
                   double price, double originalPrice, int stock,
                   boolean isNew, List<Integer> sizes,
                   float rating, int reviewCount, int imageResId,
                   String imageUrl, boolean isAvailable, boolean isDiscontinued) {
        this.id            = id;
        this.name          = name;
        this.brand         = brand;
        this.category      = category;
        this.price         = price;
        this.originalPrice = originalPrice;
        this.stock         = stock;
        this.isNew         = isNew;
        this.sizes         = sizes;
        this.rating        = rating;
        this.reviewCount   = reviewCount;
        this.imageResId    = imageResId;
        this.imageUrl      = imageUrl;
        this.isAvailable   = isAvailable;
        this.isDiscontinued = isDiscontinued;
    }

    private List<String> colors;

    public List<String> getColors() { return colors; }
    public void setColors(List<String> colors) { this.colors = colors; }

    public int            getId()            { return id; }
    public String         getName()          { return name; }
    public String         getBrand()         { return brand; }
    public String         getCategory()      { return category; }
    public double         getPrice()         { return price; }
    public double         getOriginalPrice() { return originalPrice; }
    public int            getStock()         { return stock; }
    public boolean        isNew()            { return isNew; }
    public List<Integer>  getSizes()         { return sizes; }
    public float          getRating()        { return rating; }
    public int            getReviewCount()   { return reviewCount; }
    public int            getImageResId()    { return imageResId; }
    public String         getImageUrl()      { return imageUrl; }
    public boolean        isAvailable()      { return isAvailable; }
    public boolean        isDiscontinued()   { return isDiscontinued; }
}
