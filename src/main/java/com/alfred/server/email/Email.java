package com.alfred.server.email;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.mail.internet.MimeMultipart;

/**
 * Email parent class. This parent class contains the common email properties
 * and methods. Subclasses only need to define the getContent method.
 * 
 * @author Kevin Kanzelmeyer
 *
 */
public abstract class Email {
    
    private String subject;
    private String date;
    private String imagePath;    
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    /**
     * Method to create a pretty date 
     * @return A string with a nice display date
     */
    public String getDisplayDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.valueOf(date));
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, y 'at' hh:mm:ss a");
        String displayDate = dateFormat.format(calendar.getTime());
        return displayDate;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    /**
     * This method should be used by child classes to create the email message
     * content
     * 
     * @return The content of the email message
     */
    public abstract MimeMultipart getContent();
}
