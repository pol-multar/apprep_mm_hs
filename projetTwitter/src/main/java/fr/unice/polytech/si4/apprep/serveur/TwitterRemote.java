package fr.unice.polytech.si4.apprep.serveur;


import java.rmi.Remote;
import java.rmi.RemoteException;

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
}
