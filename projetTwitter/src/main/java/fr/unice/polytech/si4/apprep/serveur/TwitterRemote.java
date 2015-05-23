package fr.unice.polytech.si4.apprep.serveur;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface TwitterRemote extends Remote{
    /**
     * Allows the unknown user to connect to their account
     * @param username, the username
     * @param pwd, the password
     * @return true if connection allowed
     * @throws RemoteException
     */
    public boolean connect(String username, String pwd) throws RemoteException;

    /**
     * Allow users to be disconnected
     * @param username the username to be disconnected
     * @throws RemoteException
     */
    public void disconnect(String username) throws RemoteException;

    /**
     * Posts a tweet on the server
     * @param username the name of the user
     * @param msg the message to post (includes the hashtags)
     * @throws RemoteException
     */
    public void tweet(String username, String msg) throws RemoteException;

    /**
     * Reposts a tweet that already exists
     * @param username the name of the user
     * @param id the id of the tweet
     * @throws RemoteException
     */
    public void retweet(String username, int id) throws RemoteException;

    /**
     * Get the list of available hashtags
     * @throws RemoteException
     */
    public List<String> getAvailableHashtags() throws RemoteException;

    public void addNewHashtag(String hashtag) throws RemoteException;
}
