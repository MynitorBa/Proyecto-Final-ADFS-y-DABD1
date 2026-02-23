package org.example.helpers;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailHelper {

    private static final String HOST     = "smtp.gmail.com";
    private static final int    PUERTO   = 587;
    private static final String CORREO   = "distribuidorapine@gmail.com";
    private static final String PASSWORD = "axvv hnkv gylv gupb";

    private static Session crearSesion() {
        Properties props = new Properties();
        props.put("mail.smtp.host",            HOST);
        props.put("mail.smtp.port",            PUERTO);
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(CORREO, PASSWORD);
            }
        });
    }

    public static void enviar(String destinatario, String asunto, String cuerpoHtml) {
        try {
            Session session = crearSesion();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(CORREO));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(asunto);
            message.setContent(cuerpoHtml, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error enviando correo: " + e.getMessage(), e);
        }
    }
}