package com.example.shoeapp.user;

import java.util.Locale;

public class GuardrailFilter {
    private static final String[] OFF_TOPIC_KEYWORDS = {
        "nấu ăn", "recipes", "recipe", "cooking", "nấu gì", "món ăn", "món ngon", "hướng dẫn nấu",
        "lập trình", "java", "python", "javascript", "c++", "c#", "html", "css", "writing code", "coding", "software", "viết code", "lập trình viên",
        "thời tiết", "weather", "dự báo thời tiết",
        "chính trị", "politics", "bầu cử", "tổng thống", "đảng phái",
        "tử vi", "horoscope", "bói toán", "cung hoàng đạo",
        "nhạc", "bài hát", "song", "ca sĩ",
        "phim", "movie", "cinema", "diễn viên"
    };

    public static boolean isOffTopic(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        String lowerMessage = message.toLowerCase(Locale.getDefault());
        for (String keyword : OFF_TOPIC_KEYWORDS) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static String getRejectionResponse() {
        return "Xin lỗi bạn, tôi là trợ lý ảo AI của SoleStep. Tôi chỉ có thể hỗ trợ các thông tin liên quan đến sản phẩm giày dép (Nike, Adidas, New Balance...), giá cả, tư vấn kích cỡ size, và các hoạt động của cửa hàng. Vui lòng đặt câu hỏi liên quan đến sản phẩm để được hỗ trợ tốt nhất nhé!";
    }
}
