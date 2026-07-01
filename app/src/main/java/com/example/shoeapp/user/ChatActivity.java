package com.example.shoeapp.user;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.ChatMessage;
import com.example.shoeapp.data.entity.Product;
import com.example.shoeapp.data.model.ProductColorOption;
import com.example.shoeapp.data.model.ProductSizeOption;
import com.example.shoeapp.ui.BaseSoleStepActivity;
import com.example.shoeapp.user.adapter.ChatAdapter;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import com.example.shoeapp.BuildConfig;
import java.util.ArrayList;

public class ChatActivity extends BaseSoleStepActivity implements ChatAdapter.ChatCallback {

    private AppDatabase db;
    private ChatAdapter chatAdapter;
    private RecyclerView recyclerView;
    private EditText inputText;
    private View sendButton;
    private View statusDot;
    private TextView statusText;

    private boolean isWaitingForResponse = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = AppDatabase.getDatabase(this);
        View rootLayout = findViewById(R.id.main);
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets imeBars = insets.getInsets(WindowInsetsCompat.Type.ime());
                int bottomPadding = imeBars.bottom > 0 ? imeBars.bottom : systemBars.bottom;
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);
                return insets;
            });
        }
        if (rootLayout != null) {
            rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                android.graphics.Rect r = new android.graphics.Rect();
                rootLayout.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootLayout.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;
                if (keypadHeight > screenHeight * 0.15) {
                    if (chatAdapter.getItemCount() > 0) {
                        recyclerView.postDelayed(() -> recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1), 50);
                    }
                }
            });
        }

        recyclerView = findViewById(R.id.chat_recycler_view);
        inputText = findViewById(R.id.chat_input_text);
        sendButton = findViewById(R.id.chat_send_button);
        statusDot = findViewById(R.id.chat_status_dot);
        statusText = findViewById(R.id.chat_status_text);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(this, this);
        recyclerView.setAdapter(chatAdapter);

        loadChatHistory();
        updateNetworkStatusIndicator();

        findViewById(R.id.chat_back_button).setOnClickListener(v -> finish());

        sendButton.setOnClickListener(v -> {
            String query = inputText.getText().toString().trim();
            if (!query.isEmpty()) {
                sendChatMessage(query);
                inputText.setText("");
            }
        });

        findViewById(R.id.chip_nike_under_3m).setOnClickListener(v -> sendChatMessage("Nike dưới 3 triệu"));
        findViewById(R.id.chip_running_shoes).setOnClickListener(v -> sendChatMessage("Giày chạy bộ tốt"));
        findViewById(R.id.chip_size_guide).setOnClickListener(v -> sendChatMessage("Tư vấn chọn size"));
        findViewById(R.id.chip_discounts).setOnClickListener(v -> sendChatMessage("Sản phẩm giảm giá"));
        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                recyclerView.postDelayed(() -> {
                    if (chatAdapter.getItemCount() > 0) {
                        recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                    }
                }, 100);
            }
        });
    }

    private void loadChatHistory() {
        new Thread(() -> {
            List<ChatMessage> history = db.chatMessageDao().getAllMessages();
            runOnUiThread(() -> {
                chatAdapter.setMessages(history);
                if (chatAdapter.getItemCount() > 0) {
                    recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                }
            });
        }).start();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    private void updateNetworkStatusIndicator() {
        if (isNetworkConnected()) {
            statusText.setText("Trực tuyến • Sẵn sàng hỗ trợ");
            statusDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#10B981")));
        } else {
            statusText.setText("Chế độ Ngoại tuyến");
            statusDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF5500")));
        }
    }

    private void setWaitingState(boolean waiting) {
        isWaitingForResponse = waiting;
        sendButton.setEnabled(!waiting);
        inputText.setEnabled(!waiting);
    }

    private void sendChatMessage(String query) {
        if (isWaitingForResponse) return;

        updateNetworkStatusIndicator();

        ChatMessage userMsg = new ChatMessage();
        userMsg.isUser = true;
        userMsg.content = query;
        userMsg.timestamp = System.currentTimeMillis();
        userMsg.sessionId = "sole_step_assistant_session";

        new Thread(() -> db.chatMessageDao().insert(userMsg)).start();
        chatAdapter.addMessage(userMsg);
        recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);

        if (GuardrailFilter.isOffTopic(query)) {
            setWaitingState(true);
            showLoadingBubble();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                chatAdapter.removeLastMessage();
                setWaitingState(false);

                ChatMessage botMsg = new ChatMessage();
                botMsg.isUser = false;
                botMsg.content = GuardrailFilter.getRejectionResponse();
                botMsg.timestamp = System.currentTimeMillis();
                botMsg.sessionId = "sole_step_assistant_session";

                new Thread(() -> db.chatMessageDao().insert(botMsg)).start();
                chatAdapter.addMessage(botMsg);
                recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            }, 800);

            return;
        }

        if (!isNetworkConnected()) {
            setWaitingState(true);
            showLoadingBubble();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                chatAdapter.removeLastMessage();
                setWaitingState(false);

                String faqAnswer = findMatchingFAQ(query);
                ChatMessage botMsg = new ChatMessage();
                botMsg.isUser = false;
                botMsg.timestamp = System.currentTimeMillis();
                botMsg.sessionId = "sole_step_assistant_session";

                if (faqAnswer != null) {
                    botMsg.content = "[FAQ Ngoại tuyến] " + faqAnswer;
                } else {
                    botMsg.content = "Thiết bị đang ngoại tuyến. Vui lòng kết nối Internet để trò chuyện cùng trợ lý AI!";
                    botMsg.isError = true;
                }

                new Thread(() -> db.chatMessageDao().insert(botMsg)).start();
                chatAdapter.addMessage(botMsg);
                recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            }, 600);

            return;
        }

        setWaitingState(true);
        showLoadingBubble();

        String productContext = ContextBuilder.buildProductContext(this, query);

        List<ChatMessage> allMessages = new ArrayList<>();
        try {
            allMessages = db.chatMessageDao().getAllMessages();
        } catch (Exception ignored) {}
        List<ChatMessage> recentMessages;
        if (allMessages.size() > 10) {
            recentMessages = allMessages.subList(allMessages.size() - 10, allMessages.size());
        } else {
            recentMessages = allMessages;
        }

        StringBuilder historyPrompt = new StringBuilder();
        for (ChatMessage msg : recentMessages) {
            if (msg.isUser) {
                historyPrompt.append("Khách hàng: ").append(msg.content).append("\n");
            } else if (msg.content != null && !msg.isError) {
                historyPrompt.append("Trợ lý AI: ").append(msg.content).append("\n");
            }
        }

        String systemInstruction = "Bạn là một trợ lý ảo tư vấn bán hàng của cửa hàng giày SoleStep. Nhiệm vụ của bạn là hỗ trợ khách hàng tìm kiếm giày, tư vấn chọn size, cung cấp thông tin khuyến mãi.\n" +
                "Quy tắc quan trọng:\n" +
                "1. Chỉ trả lời các câu hỏi liên quan đến sản phẩm giày dép của cửa hàng, dịch vụ của cửa hàng, size giày, hãng giày. Cực kỳ từ chối trả lời các chủ đề khác như nấu ăn, thời tiết, lập trình, tử vi,... một cách lịch sự.\n" +
                "2. Luôn ghi ĐÚNG và ĐẦY ĐỦ tên sản phẩm trong dấu ngoặc kép (ví dụ: \"Nike Air Force 1 '07\", \"adidas Samba OG\",...) khi đề xuất sản phẩm để ứng dụng có thể tự động tạo link liên kết cho người dùng nhấp vào.\n" +
                "3. Nếu sản phẩm có giá bán hiện tại rẻ hơn giá gốc, hãy nói đây là sản phẩm đang giảm giá.\n" +
                "4. Trả lời thân thiện, ngắn gọn, súc tích bằng Tiếng Việt.\n\n";

        String fullPrompt = systemInstruction +
                productContext + "\n\n" +
                "LỊCH SỬ TRÒ CHUYỆN GẦN ĐÂY:\n" +
                historyPrompt.toString() + "\n" +
                "Khách hàng: " + query + "\n" +
                "Trợ lý AI:";

        try {
            GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", BuildConfig.GEMINI_API_KEY);
            GenerativeModelFutures model = GenerativeModelFutures.from(gm);

            Content.Builder contentBuilder = new Content.Builder();
            contentBuilder.setRole("user");
            contentBuilder.addText(fullPrompt);
            Content content = contentBuilder.build();

            ListenableFuture<GenerateContentResponse> future = model.generateContent(content);
            Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    chatAdapter.removeLastMessage();
                    setWaitingState(false);

                    String reply = result.getText();
                    if (reply == null || reply.trim().isEmpty()) {
                        reply = "Xin lỗi, tôi chưa tìm thấy thông tin phù hợp. Bạn cần hỏi thêm gì về giày không?";
                    }

                    ChatMessage botMsg = new ChatMessage();
                    botMsg.isUser = false;
                    botMsg.content = reply;
                    botMsg.timestamp = System.currentTimeMillis();
                    botMsg.sessionId = "sole_step_assistant_session";

                    botMsg.productId = detectProductIdInReply(reply);

                    final ChatMessage finalBotMsg = botMsg;
                    new Thread(() -> db.chatMessageDao().insert(finalBotMsg)).start();
                    chatAdapter.addMessage(botMsg);
                    recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                }

                @Override
                public void onFailure(Throwable t) {
                    chatAdapter.removeLastMessage();
                    setWaitingState(false);

                    String errorMsg = "Kết nối lỗi: " + t.getMessage();
                    ChatMessage botMsg = new ChatMessage();
                    botMsg.isUser = false;
                    botMsg.content = errorMsg;
                    botMsg.isError = true;
                    botMsg.timestamp = System.currentTimeMillis();
                    botMsg.sessionId = "sole_step_assistant_session";

                    new Thread(() -> db.chatMessageDao().insert(botMsg)).start();
                    chatAdapter.addMessage(botMsg);
                    recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                }
            }, ContextCompat.getMainExecutor(this));

        } catch (Exception e) {
            chatAdapter.removeLastMessage();
            setWaitingState(false);

            ChatMessage botMsg = new ChatMessage();
            botMsg.isUser = false;
            botMsg.content = "Lỗi kết nối AI: " + e.getMessage();
            botMsg.isError = true;
            botMsg.timestamp = System.currentTimeMillis();
            botMsg.sessionId = "sole_step_assistant_session";

            new Thread(() -> db.chatMessageDao().insert(botMsg)).start();
            chatAdapter.addMessage(botMsg);
            recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    private Integer detectProductIdInReply(String reply) {
        try {
            List<Product> products = db.productDao().getAllProductsActive();
            String replyLower = reply.toLowerCase(Locale.getDefault());
            for (Product product : products) {
                if (product.name == null) continue;
                String nameLower = product.name.toLowerCase(Locale.getDefault());
                if (replyLower.contains(nameLower)) {
                    return product.id;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void showLoadingBubble() {
        ChatMessage loadingMsg = new ChatMessage();
        loadingMsg.isUser = false;
        loadingMsg.content = null;
        chatAdapter.addMessage(loadingMsg);
        recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
    }

    private String loadFAQJson() {
        String json = null;
        try {
            InputStream is = getAssets().open("faq.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return json;
    }

    private String findMatchingFAQ(String query) {
        String faqJson = loadFAQJson();
        if (faqJson == null) return null;

        String queryLower = query.toLowerCase(Locale.getDefault());
        try {
            JSONArray array = new JSONArray(faqJson);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                JSONArray keywords = obj.getJSONArray("keywords");
                for (int j = 0; j < keywords.length(); j++) {
                    String keyword = keywords.getString(j).toLowerCase(Locale.getDefault());
                    if (queryLower.contains(keyword)) {
                        return obj.getString("answer");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onRetryClick(ChatMessage message) {
        new Thread(() -> {
            db.chatMessageDao().delete(message);
            List<ChatMessage> history = db.chatMessageDao().getAllMessages();
            String lastUserQuery = "";
            for (int i = history.size() - 1; i >= 0; i--) {
                if (history.get(i).isUser) {
                    lastUserQuery = history.get(i).content;
                    break;
                }
            }
            final String queryToSend = lastUserQuery;
            runOnUiThread(() -> {
                chatAdapter.removeLastMessage();
                if (!queryToSend.isEmpty()) {
                    sendChatMessage(queryToSend);
                } else {
                    Toast.makeText(this, "Không tìm thấy tin nhắn cũ để gửi lại!", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    public void onFeedbackClick(ChatMessage message, int rating) {
        message.feedbackRating = rating;
        new Thread(() -> db.chatMessageDao().update(message)).start();
        chatAdapter.notifyDataSetChanged();
        if (rating != 0) {
            Toast.makeText(this, "Cảm ơn bạn đã phản hồi đóng góp!", Toast.LENGTH_SHORT).show();
        }
    }

}
