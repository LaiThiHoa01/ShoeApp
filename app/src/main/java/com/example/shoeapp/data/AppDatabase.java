package com.example.shoeapp.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.shoeapp.data.dao.CategoryDao;
import com.example.shoeapp.data.dao.OrderDao;
import com.example.shoeapp.data.dao.ProductDao;
import com.example.shoeapp.data.dao.UserDao;
import com.example.shoeapp.data.entity.Brand;
import com.example.shoeapp.data.entity.Cart;
import com.example.shoeapp.data.entity.CartItem;
import com.example.shoeapp.data.entity.Category;
import com.example.shoeapp.data.entity.Color;
import com.example.shoeapp.data.entity.Order;
import com.example.shoeapp.data.entity.OrderDetail;
import com.example.shoeapp.data.entity.Product;
import com.example.shoeapp.data.entity.ProductImg;
import com.example.shoeapp.data.entity.ProductReview;
import com.example.shoeapp.data.entity.ProductVariant;
import com.example.shoeapp.data.entity.Promotion;
import com.example.shoeapp.data.entity.PromotionProduct;
import com.example.shoeapp.data.entity.Size;
import com.example.shoeapp.data.entity.TokenType;
import com.example.shoeapp.data.entity.User;

@Database(entities = {
        Brand.class,
        Cart.class,
        CartItem.class,
        Category.class,
        Color.class,
        Order.class,
        OrderDetail.class,
        Product.class,
        ProductImg.class,
        ProductReview.class,
        ProductVariant.class,
        Promotion.class,
        PromotionProduct.class,
        Size.class,
        TokenType.class,
        User.class
}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract ProductDao productDao();
    public abstract CategoryDao categoryDao();
    public abstract OrderDao orderDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "shoeapp.db")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
