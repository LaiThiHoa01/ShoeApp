package com.example.shoeapp.authentication;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import com.example.shoeapp.BuildConfig;

public class EmailOtp {

    public static void sendOtp(String toEmail, String otp) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        BuildConfig.SMTP_EMAIL,
                        BuildConfig.SMTP_APP_PASSWORD
                );
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(BuildConfig.SMTP_EMAIL, "ShoeApp"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("ShoeApp- Mã xác nhận");
        message.setText("Mã OTP của bạn là: " + otp + "\nMã có hiệu lực trong 5 phút.");

        Transport.send(message);
    }
}