package com.example.shoeapp.user;

import android.content.Context;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.dao.ProductDao;
import com.example.shoeapp.data.entity.Brand;
import com.example.shoeapp.data.entity.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContextBuilder {

    public static String buildProductContext(Context context, String userQuery) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            return "";
        }

        AppDatabase db = AppDatabase.getDatabase(context);
        ProductDao productDao = db.productDao();
        
        List<Product> allActive = productDao.getAllProductsActive();
        List<Brand> allBrands = productDao.getAllBrands();
        
        Map<Integer, String> brandMap = new HashMap<>();
        for (Brand b : allBrands) {
            brandMap.put(b.id, b.name);
        }

        String queryLower = userQuery.toLowerCase(Locale.getDefault());

        List<Integer> targetBrandIds = new ArrayList<>();
        for (Brand b : allBrands) {
            String brandNameLower = b.name.toLowerCase(Locale.getDefault());
            if (queryLower.contains(brandNameLower)) {
                targetBrandIds.add(b.id);
            }
        }
        if (queryLower.contains("nb") && !targetBrandIds.contains(3)) {
            targetBrandIds.add(3);
        }

        double minPrice = 0;
        double maxPrice = Double.MAX_VALUE;
        boolean hasPriceFilter = false;

        Pattern underMillionPattern = Pattern.compile("(dưới|nhỏ hơn|<|thấp hơn)\\s*([0-9.,]+)\\s*(triệu|tr)");
        Matcher matcher = underMillionPattern.matcher(queryLower);
        if (matcher.find()) {
            try {
                String valStr = matcher.group(2).replace(",", ".");
                double value = Double.parseDouble(valStr);
                maxPrice = value * 1000000;
                hasPriceFilter = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Pattern underThousandPattern = Pattern.compile("(dưới|nhỏ hơn|<|thấp hơn)\\s*([0-9.,]+)\\s*(k|nghìn|ngàn)");
            matcher = underThousandPattern.matcher(queryLower);
            if (matcher.find()) {
                try {
                    String valStr = matcher.group(2).replace(",", ".");
                    double value = Double.parseDouble(valStr);
                    maxPrice = value * 1000;
                    hasPriceFilter = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Pattern overMillionPattern = Pattern.compile("(trên|lớn hơn|>|cao hơn)\\s*([0-9.,]+)\\s*(triệu|tr)");
        matcher = overMillionPattern.matcher(queryLower);
        if (matcher.find()) {
            try {
                String valStr = matcher.group(2).replace(",", ".");
                double value = Double.parseDouble(valStr);
                minPrice = value * 1000000;
                hasPriceFilter = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Pattern approxMillionPattern = Pattern.compile("(tầm|khoảng|xấp xỉ)\\s*([0-9.,]+)\\s*(triệu|tr)");
        matcher = approxMillionPattern.matcher(queryLower);
        if (matcher.find() && !hasPriceFilter) {
            try {
                String valStr = matcher.group(2).replace(",", ".");
                double value = Double.parseDouble(valStr);
                double basePrice = value * 1000000;
                minPrice = basePrice - 500000;
                maxPrice = basePrice + 500000;
                hasPriceFilter = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<Product> matchedProducts = new ArrayList<>();
        for (Product p : allActive) {
            boolean matchesBrand = targetBrandIds.isEmpty() || targetBrandIds.contains(p.brandId);
            boolean matchesPrice = p.price >= minPrice && p.price <= maxPrice;
            
            boolean matchesKeyword = false;
            String productNameLower = p.name.toLowerCase(Locale.getDefault());
            String[] words = productNameLower.split("\\s+");
            for (String word : words) {
                if (word.length() > 2 && queryLower.contains(word)) {
                    matchesKeyword = true;
                    break;
                }
            }

            if (matchesBrand && matchesPrice) {
                matchedProducts.add(p);
            } else if (matchesKeyword && matchesPrice) {
                matchedProducts.add(p);
            }
        }

        if (matchedProducts.isEmpty()) {
            int count = Math.min(8, allActive.size());
            for (int i = 0; i < count; i++) {
                matchedProducts.add(allActive.get(i));
            }
        }

        int limit = Math.min(10, matchedProducts.size());
        List<Product> finalProducts = matchedProducts.subList(0, limit);

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("THÔNG TIN SẢN PHẨM HIỆN CÓ TẠI CỬA HÀNG:\n");
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        for (int i = 0; i < finalProducts.size(); i++) {
            Product p = finalProducts.get(i);
            String brandName = brandMap.get(p.brandId);
            if (brandName == null) brandName = "Khác";

            String formattedPrice = currencyFormat.format(p.price);
            String formattedOriginal = currencyFormat.format(p.originalPrice);

            contextBuilder.append(String.format(Locale.getDefault(),
                    "%d. Tên sản phẩm: \"%s\"\n" +
                    "   - Thương hiệu: %s\n" +
                    "   - Giá bán hiện tại: %s\n" +
                    "   - Giá gốc: %s\n" +
                    "   - Mô tả ngắn: %s\n" +
                    "   - Mã sản phẩm (productId): %s\n\n",
                    (i + 1), p.name, brandName, formattedPrice, formattedOriginal, p.description, p.productId));
        }

        contextBuilder.append("HƯỚNG DẪN TRẢ LỜI CHO TRỢ LÝ AI:\n");
        contextBuilder.append("- Hãy sử dụng thông tin sản phẩm ở trên để trả lời chính xác câu hỏi của khách hàng.\n");
        contextBuilder.append("- Khi nhắc đến sản phẩm, hãy ghi ĐÚNG và ĐẦY ĐỦ tên sản phẩm trong dấu ngoặc kép (ví dụ: \"Nike Air Force 1 '07\") để hệ thống có thể tạo link liên kết cho khách hàng bấm vào xem chi tiết.\n");
        contextBuilder.append("- Nếu sản phẩm có giá bán thấp hơn giá gốc, hãy nhấn mạnh đây là sản phẩm đang khuyến mãi giảm giá.\n");
        contextBuilder.append("- Trả lời thân thiện, lịch sự và ngắn gọn bằng Tiếng Việt.");

        return contextBuilder.toString();
    }
}
