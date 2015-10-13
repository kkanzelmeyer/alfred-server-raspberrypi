package com.alfred.server.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.mail.internet.MimeMultipart;

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
     * This method should be used by child classes to create
     * the message content
     */
    public abstract MimeMultipart getContent();
}
