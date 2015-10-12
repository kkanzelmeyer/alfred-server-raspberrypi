package com.alfred.server.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage;
import com.alfred.common.network.NetworkHandler;
import com.alfred.server.utils.Email;

/**
 * This class is the abstraction of the Alfred server connections. It maintains
 * a list of client connections and a list of connection handlers
 * 
 * @author Kevin Kanzelmeyer
 *
 */
public class Server {

    private static List<Socket> serverConnections = new ArrayList<Socket>();
    private static List<String> smsClients = new ArrayList<String>();
    private static List<NetworkHandler> networkHandlers = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private static Properties properties = null;
    
    /* ------------------------------------------------------------------
     *   PROPERTIES
     * ------------------------------------------------------------------*/
    public static final String EMAIL_USERNAME = "mail.username";
    public static final String EMAIL_TOKEN = "mail.token";
    public static final String HOST_ADDRESS = "alfred.hostaddress";
    public static final String HOST_PORT = "alfred.hostport";
    public static final String IMAGE_PATH = "alfred.imagepath";
    
    public static void loadProperties() {
        if(properties == null) {
            properties = new Properties();
            try {
                InputStream input = new FileInputStream("cfg/config.properties");
                properties.load(input);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static Properties getProperties() {
        return properties;
    }
    
    public static String getProperty(String key) {
        if(properties.containsKey(key)) {
            return properties.getProperty(key);
        } else 
            return null;
    }
    
    /* ------------------------------------------------------------------
     *   CONNECTIONS
     * ------------------------------------------------------------------*/
    public static List<Socket> getServerConnections() {
        return serverConnections;
    }

    /**
     * @param connection
     */
    public static void addServerConnection(Socket connection) {
        serverConnections.add(connection);
        log.info("New Connection added");
        // Notify Connection Handlers
        for(NetworkHandler handler : networkHandlers) {
            handler.onConnect(connection);
        }
    }

    /**
     * @param connection
     */
    public static void removeServerConnection(Socket connection) {
        serverConnections.remove(connection);
        log.info("Connection removed");
    }

    public static int getConnectionCount() {
        return serverConnections.size();
    }

    /* ------------------------------------------------------------------
     *   EMAIL CLIENTS
     * ------------------------------------------------------------------*/
    
    public static List<String> getSMSClients() {
        return smsClients;
    }
    
    /**
     * @param phoneNumber
     */
    public static void addSMSClient(String phoneNumber) {
        smsClients.add(phoneNumber);
        log.info("New Connection added");
    }

    /**
     * @param phoneNumber
     */
    public static void removeSMSClient(String phoneNumber) {
        smsClients.remove(phoneNumber);
        log.info("Connection removed");
    }

    /* ------------------------------------------------------------------
     *   CONNECTION HANDLERS
     * ------------------------------------------------------------------*/
    
    public static void addNetworkHandler(NetworkHandler handler) {
        if(!networkHandlers.contains(handler)) {
            log.info("Adding server connection handler: " + handler.getClass());
            networkHandlers.add(handler);
        }
    }
    
    public static void removeNetworkHandler(NetworkHandler handler) {
        if(networkHandlers.contains(handler)) {
            log.info("Removing server connection handler: " + handler.getClass());
            networkHandlers.remove(handler);
        }
    }
    
    /**
     * Method for receiving a new message
     */
    public static void messageReceived(StateDeviceMessage msg) {
     // Notify Connection Handlers
        for(NetworkHandler handler : networkHandlers) {
            log.debug("Message Received: notifying handler " + handler);
            handler.onMessageReceived(msg);
        }
    }
    
    /**
     * Helper method to send a state update message to all connected clients
     * 
     * @param device
     */
    public static void sendMessage(StateDeviceMessage msg) {

        // Send message to each client
        for (Socket socket : getServerConnections()) {
            if (socket.isConnected()) {
                try {
                    log.info("Sending message");
                    msg.writeDelimitedTo(socket.getOutputStream());
                } catch (Exception e) {
                    Server.removeServerConnection(socket);
                    log.error("Writing to socket failed", e);
                }
            }
        }
    }
    
    /**
     * Email function
     */
    public static void sendEmail(Email email) {
        if(properties == null) {
            loadProperties();
        }
        final String username = getProperty(EMAIL_USERNAME);
        final String password = getProperty(EMAIL_TOKEN);

        Session session = Session.getInstance(getProperties(), new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse("kevin.kanzelmeyer@gmail.com"));
            message.setSubject(email.getSubject());
            
            // put everything together
            message.setContent(email.getContent());

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            log.error("Email error", e);
            throw new RuntimeException(e);
        }
    }
}


