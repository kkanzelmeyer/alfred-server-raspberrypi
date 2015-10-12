package com.alfred.server.utils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

public class VisitorEmail extends Email {

    @Override
    public void build() {
        MimeMultipart multipart = new MimeMultipart("related");
        
        // first part (the html)
        BodyPart messageBodyPart = new MimeBodyPart();
        String htmlText = "<h3>" + getDisplayDate() + "</h3>"
                        + "<img src=\"cid:image\">";
        try {
            messageBodyPart.setContent(htmlText, "text/html");
            // add it
            multipart.addBodyPart(messageBodyPart);

            // second part (the image)
            messageBodyPart = new MimeBodyPart();
            DataSource fds = new FileDataSource(getImagePath());

            messageBodyPart.setDataHandler(new DataHandler(fds));
            messageBodyPart.setHeader("Content-ID", "<image>");

            // add image to the multipart
            multipart.addBodyPart(messageBodyPart);
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        content = multipart;
    }

}
