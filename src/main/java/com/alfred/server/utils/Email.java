package com.alfred.server.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.mail.internet.MimeMultipart;

public abstract class Email {
    
    private String subject;
    private String date;
    private String imagePath;
    protected MimeMultipart content;
    
    
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getDisplayDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.valueOf(date));
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, y 'at' h:m:s a");
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
    
    public MimeMultipart getContent() {
        return content;
    }
    
    /**
     * This method should be overriden and set the content value
     * of the email parent class
     */
    public abstract void build();
}
