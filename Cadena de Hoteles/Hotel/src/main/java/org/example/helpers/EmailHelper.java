package org.example.helpers;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

/**
 * Helper para el envio de correos electronicos via SMTP usando Gmail.
 * Configura la sesion con autenticacion y TLS, y expone un metodo
 * estatico para enviar mensajes en formato HTML.
 */
public class EmailHelper {

    private static final String HOST     = "smtp.gmail.com";
    private static final int    PUERTO   = 587;
    private static final String CORREO   = "distribuidorapine@gmail.com";
    private static final String PASSWORD = "axvv hnkv gylv gupb";

    /**
     * Crea y configura una sesion SMTP autenticada con el servidor de Gmail.
     * Usa STARTTLS en el puerto 587 para cifrar la conexion.
     *
     * @return sesion de JavaMail lista para enviar mensajes.
     */
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

    /**
     * Envia un correo electronico en formato HTML al destinatario indicado.
     *
     * @param destinatario direccion de correo del receptor.
     * @param asunto       asunto del mensaje.
     * @param cuerpoHtml   contenido del mensaje en formato HTML.
     * @throws RuntimeException si ocurre un error durante el envio del correo.
     */
    public static void enviar(String destinatario, String asunto, String cuerpoHtml) {
        try {
            Session session = crearSesion();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(CORREO));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(asunto);
            // Especificar charset UTF-8 para soportar caracteres especiales en el HTML
            message.setContent(cuerpoHtml, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error enviando correo: " + e.getMessage(), e);
        }
    }

    /**
     * Envia un correo electronico con un archivo adjunto al destinatario indicado.
     *
     * @param destinatario  direccion de correo del receptor.
     * @param asunto        asunto del mensaje.
     * @param cuerpoHtml    contenido del mensaje en formato HTML.
     * @param adjunto       bytes del archivo adjunto.
     * @param nombreArchivo nombre del archivo adjunto (con extension).
     * @param contentType   tipo MIME del adjunto (ej. "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").
     * @throws RuntimeException si ocurre un error durante el envio del correo.
     */
    public static void enviarConAdjunto(String destinatario, String asunto, String cuerpoHtml,
                                         byte[] adjunto, String nombreArchivo, String contentType) {
        try {
            Session session = crearSesion();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(CORREO));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(asunto);

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(cuerpoHtml, "text/html; charset=utf-8");

            MimeBodyPart attachPart = new MimeBodyPart();
            attachPart.setFileName(nombreArchivo);
            attachPart.setContent(adjunto, contentType);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(htmlPart);
            multipart.addBodyPart(attachPart);

            message.setContent(multipart);
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error enviando correo con adjunto: " + e.getMessage(), e);
        }
    }
}