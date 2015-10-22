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

import org.bridj.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage;
import com.alfred.common.network.NetworkHandler;
import com.alfred.server.email.Email;

/**
 * This class is the abstraction of the Alfred server. It maintains the client
 * connections, the connection handlers, the properties, and the network
 * handlers
 * 
 * @author Kevin Kanzelmeyer
 *
 */
public class Server {

    private static ArrayList<Socket> serverConnections = new ArrayList<Socket>();
    private static List<String> emailClients = new ArrayList<String>();
    private static ArrayList<NetworkHandler> networkHandlers = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private static Properties properties = null;
    
    /* ------------------------------------------------------------------
     *   PROPERTIES
     * ------------------------------------------------------------------*/
    // convenient constants for getting property keys
    public static final String EMAIL_USERNAME = "mail.username";
    public static final String EMAIL_TOKEN    = "mail.token";
    public static final String HOST_ADDRESS   = "alfred.hostaddress";
    public static final String HOST_PORT      = "alfred.hostport";
    public static final String IMAGE_PATH     = "alfred.imagepath";
    public static final String EMAIL_CLIENTS  = "alfred.emailclients";

    /**
     * Method to load properties from the configuration file. Your configuration
     * file should be in {project root}/cfg as notated below. The configuration
     * property keys should match the string values in the above server
     * constants. An example and notes can be found in the cfg directory
     * 
     * @param path the path to the configuration file
     */
    public static void loadProperties(String path) {
        if(properties == null) {
            properties = new Properties();
            try {
                InputStream input = new FileInputStream(path);
                properties.load(input);
                
                // email clients
                String[] emails = getProperty(Server.EMAIL_CLIENTS).split(",");
                for(String email : emails) {
                    if(!email.equals("")) {
                        addEmailClient(email);
                    }
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method to return the properties object
     * 
     * @return A reference to the properties object created from the config file
     */
    public static Properties getProperties() {
        return properties;
    }
    
    /**
     * Method to get the property value of a given key. Note that the keys can
     * be easily captured using the above server constants
     * 
     * @param key The key of the property
     * @return The property value of the given key
     */
    public static String getProperty(String key) {
        if(properties.containsKey(key)) {
            return properties.getProperty(key);
        } else 
            return null;
    }
    
    /* ------------------------------------------------------------------
     *   CONNECTIONS
     * ------------------------------------------------------------------*/
   /**
    *  Method to fetch all client connections registered with the server
    *  
    * @return A List of the connections registered with the server
    */
    public static List<Socket> getServerConnections() {
        return serverConnections;
    }

    /**
     * Method to add a server connection
     * 
     * @param connection A reference to the socket connection
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
     * Method to remove a specific server connection
     * 
     * @param connection A reference to the socket connection
     */
    public static void removeServerConnection(Socket connection) {
        serverConnections.remove(connection);
        log.info("Connection removed");
    }

    /**
     * Method to count the number of active server socket connections
     * 
     * @return The number of connections registered with the server
     */
    public static int getConnectionCount() {
        return serverConnections.size();
    }

    /* ------------------------------------------------------------------
     *   EMAIL CLIENTS
     * ------------------------------------------------------------------*/
    /**
     * Method to fetch all email clients
     * 
     * @return A List of email clients
     */
    public static List<String> getEmailClients() {
        return emailClients;
    }
    
    /**
     * Method to add an email client
     * 
     * @param email A valid email address
     */
    public static void addEmailClient(String email) {
        emailClients.add(email);
        log.info("New email added");
    }

    /**
     * Method to remove an email client
     * 
     * @param email A valid email address
     */
    public static void removeEmailClient(String email) {
        if(emailClients.contains(email)) {
            emailClients.remove(email);
            log.info("Email removed");
        } else {
            log.error("Remove email failed - email not found");
        }
    }

    /* ------------------------------------------------------------------
     *   CONNECTION HANDLERS
     * ------------------------------------------------------------------*/
    /**
     * Method to add a network handler. Add yourself as a network handler if you
     * want to be notified when a connection is added to the server and when a
     * message is received by the server
     * 
     * @param handler a reference to the Network Handler
     */
    public static void addNetworkHandler(NetworkHandler handler) {
        if(!networkHandlers.contains(handler)) {
            log.info("Adding server connection handler: " + handler.getClass());
            networkHandlers.add(handler);
        }
    }
    
    /**
     * Method to remove a network handler
     * 
     * @param handler A reference to the Network Handler
     */
    public static void removeNetworkHandler(NetworkHandler handler) {
        if(networkHandlers.contains(handler)) {
            log.info("Removing server connection handler: " + handler.getClass());
            networkHandlers.remove(handler);
        }
    }
    
    /**
     * Method for receiving a new message. This method notifies all registered
     * network handlers when a new message is received by the server
     * 
     * @param msg A reference to a StateDeviceMessage
     */
    public static void messageReceived(StateDeviceMessage msg) {
     // Notify Connection Handlers
        for(NetworkHandler handler : networkHandlers) {
            log.debug("Message Received: notifying handler " + handler);
            handler.onMessageReceived(msg);
        }
    }

    /* ------------------------------------------------------------------
     *   HELPER METHODS
     * ------------------------------------------------------------------*/
    /**
     * Helper method to send a state update message to all connected clients
     * 
     * @param msg A reference to a StateDevice
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
     * Method to send an email message to all email clients
     * 
     * @param email A reference to a valid email object
     */
    public static void sendEmail(Email email) {
        if(emailClients.size() > 0) {
    
            final String username = getProperty(EMAIL_USERNAME);
            final String password = getProperty(EMAIL_TOKEN);

            Session session = Session.getInstance(getProperties(), new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            // convert clients list into a comma separated string
            String clients = StringUtils.implode(emailClients, ",");
            try {
                log.info("Email on thread " + Thread.currentThread().getId());
                Message message = new MimeMessage(session);
                message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(clients));
                message.setSubject(email.getSubject());
                
                // add email content to message
                message.setContent(email.getContent());
    
                // send email
                Transport.send(message);
                log.info("Email sent to " + clients);
            } catch (MessagingException e) {
                log.error("Email error", e);
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * Simple method to print a greeting at the startup of the server
     */
    public static void printGreeting() {
        log.info( "\n-----------------------------------------------------------"
                + "\n             Alfred Home Server"
                + "\n-----------------------------------------------------------" 
                + "\n");
        log.info("Starting Alfred Server");
    }
    
}