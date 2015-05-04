package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TwitterServer extends UnicastRemoteObject implements TwitterRemote {
    private static final long serialVersionUID = 1L;
    private static final int PORT = 2345;

    private ConcurrentMap<Integer, Tweet> tweets;
    private List<String> availableHashtags;

    protected TwitterServer() throws RemoteException {
        super();
        tweets = new ConcurrentHashMap<Integer, Tweet>();
        availableHashtags = new ArrayList<String>();
        System.out.println("Serveur lancé !");
    }

    @Override
    public boolean connect(String username, String pwd) {
        if ("username".equals(username) && "pwd".equals(pwd)) { // TODO username/pwd database
            System.out.println(username+" s'est connecté.");
            return true;
        }
        return false;
    }

    @Override
    public void tweet(String username, String msg) {
        Tweet t = new Tweet(tweets.size(), username, msg);
        tweets.put(t.getId(), t);
        System.out.println(t);
        System.out.print("hashtags :");
        for(String hashtag : t.getHashtags()){
            System.out.print(" "+hashtag);
            if(!availableHashtags.contains(hashtag)){
                availableHashtags.add(hashtag);
                System.out.print(" (<--new!)");
            }
        }
        System.out.print("\n");
    }

    @Override
    public void retweet(String username, int id) {
        ReTweet t = new ReTweet(tweets.size(), username, tweets.get(id));
        tweets.put(t.getId(), t);
        System.out.println(t);
        System.out.print("hashtags :");
        for(String hashtag : t.getHashtags()){
            System.out.print(" "+hashtag);
        }
        System.out.print("\n");
    }

    /**
     * the main of the server
     * 
     * @param args
     */
    public static void main(String[] args) {
        TwitterServer ts = null;
        try {
            ts = new TwitterServer();
            Registry reg = LocateRegistry.createRegistry(2345);
            reg.rebind("rmi://localhost:" + PORT + "/TwitterServer", ts);
        } catch (RemoteException e) {
            e.printStackTrace();
            return;
        }
    }
}
