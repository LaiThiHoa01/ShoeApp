package com.example.shoeapp.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.shoeapp.data.dao.AddressDao;
import com.example.shoeapp.data.dao.CategoryDao;
import com.example.shoeapp.data.dao.CartDao;
import com.example.shoeapp.data.dao.ChatMessageDao;
import com.example.shoeapp.data.dao.OrderDao;
import com.example.shoeapp.data.dao.ProductDao;
import com.example.shoeapp.data.dao.UserDao;
import com.example.shoeapp.data.entity.Brand;
import com.example.shoeapp.data.entity.Cart;
import com.example.shoeapp.data.entity.CartItem;
import com.example.shoeapp.data.entity.Category;
import com.example.shoeapp.data.entity.ChatMessage;
import com.example.shoeapp.data.entity.Color;
import com.example.shoeapp.data.entity.DeliveryAddress;
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
        ChatMessage.class,
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
        User.class,
        DeliveryAddress.class
}, version = 14, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract ProductDao productDao();
    public abstract CartDao cartDao();
    public abstract CategoryDao categoryDao();
    public abstract OrderDao orderDao();
    public abstract ChatMessageDao chatMessageDao();
    public abstract AddressDao addressDao();

    private static volatile AppDatabase INSTANCE;

    public static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE promotion ADD COLUMN voucher_code TEXT");
            database.execSQL("ALTER TABLE promotion ADD COLUMN description TEXT");
            database.execSQL("ALTER TABLE promotion ADD COLUMN quantity INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE promotion ADD COLUMN target_type TEXT");
            database.execSQL("ALTER TABLE promotion ADD COLUMN category_id INTEGER");
        }
    };

    public static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE promotion ADD COLUMN brand_id INTEGER");
        }
    };

    public static final Migration MIGRATION_13_14 = new Migration(13, 14) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE promotion ADD COLUMN max_discount_amount REAL");
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "shoeapp.db")
                            .addMigrations(MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14)
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
