package fr.unice.polytech.si4.apprep.serveur;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TwitterServer extends UnicastRemoteObject implements TwitterRemote {
    private static final long serialVersionUID = 1L;
    private static final int PORT = 2345;

    private ConcurrentMap<Integer, Tweet> tweets;
    private List<String> availableHashtags;
    private ConcurrentMap<String,String> logins;
    private List<String> connectedUsers;

    //JMS Part
private Connection connect = null;
    private Session sendSession = null;
    private MessageProducer sender = null;
    private InitialContext context = null;

    protected TwitterServer() throws RemoteException {
        super();
        tweets = new ConcurrentHashMap<Integer, Tweet>();
        logins = new ConcurrentHashMap<String,String>();
        availableHashtags = new ArrayList<String>();
        connectedUsers = new ArrayList<String>();
        //TODO seulement pour les tests
        availableHashtags.add("test1");
        availableHashtags.add("test2");
        initialise();
        System.out.println("Serveur lanc� !");
    }

    private void initialise() {
        try
        {	//On initialise le syst�me
            Hashtable properties = new Hashtable();
            properties.put(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            properties.put(Context.PROVIDER_URL, "tcp://localhost:61616");
            context = new InitialContext(properties);
            javax.jms.ConnectionFactory factory = (ConnectionFactory) context.lookup("ConnectionFactory");
            connect = factory.createConnection();
            //On cr�e les topics des hashtags existants
            for(String s : availableHashtags){
                createNewTopic(s);
            }
        } catch (javax.jms.JMSException jmse){
            jmse.printStackTrace();
        } catch (NamingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void createNewTopic(String hashtag)throws JMSException, NamingException{
        sendSession = connect.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
        //On cr�e le topic responsable du hashtag donn�
        Topic topic = (Topic) context.lookup("dynamicTopics/"+hashtag);
        sender = sendSession.createProducer(topic); //sender ne sera pas forcement utilise
    }
/*
    private void sendAvailableHastags() throws JMSException{
        for (int i=1;i<=10;i++){
            //Fabriquer un message
            MapMessage mess = sendSession.createMapMessage();
            mess.setInt("num",i);
            mess.setString("nom",i+"-");
            if (i%2==0)
                mess.setStringProperty("typeMess","important");
            if (i==1) mess.setIntProperty("numMess",1);
            sender.send(mess); // equivaut � publier dans le topic
        }
    }*/

    private void broadcastTweet(Tweet t) throws NamingException, JMSException {
        //Je reccupere la liste des hastags
        //pour chaque hastag du tweet, je diffuse celui-ci

        List<String> h = t.getHashtags();

        for(String s : h){
            /*
            Topic topic = (Topic) context.lookup(s);
            sender = sendSession.createProducer(topic);
            */
            createNewTopic(s);
            //On fabrique le message
            MapMessage message = sendSession.createMapMessage();
            message.setInt("id",t.getId());
            message.setString("author",t.getAuthor());
            message.setString("contenu",t.getMessage());
            //On l'envoie
            sender.send(message);
        }
    }


    @Override
    public boolean connect(String username, String pwd) {
        if (("user1".equals(username) && "pwd".equals(pwd))
                || ("user2".equals(username) && "pwd".equals(pwd))
                || ("user3".equals(username) && "pwd".equals(pwd))) {
            connectedUsers.add(username);
            System.out.println(username+" s'est connecté.");
            return true;
        }
        return false;
    }

    @Override
    public void disconnect (String username) {
        if(connectedUsers.contains(username)){
            connectedUsers.remove(username);
        }
    }

    @Override
    public void tweet(String username, String msg) {
        Tweet t = new Tweet(tweets.size(), username, msg);
        tweets.put(t.getId(), t);
        try {//On diffuse le tweet
            broadcastTweet(t);
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        //On l'affiche sur le serveur
        System.out.println(t);
        System.out.print("hashtags :");
        for(String hashtag : t.getHashtags()){
            System.out.print(" "+hashtag);
            if(!availableHashtags.contains(hashtag)){
                availableHashtags.add(hashtag);
                try {
                    createNewTopic(hashtag);
                } catch (JMSException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
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
        try {
            broadcastTweet(t);
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.out.print("hashtags :");
        for(String hashtag : t.getHashtags()){
            System.out.print(" "+hashtag);
        }
        System.out.print("\n");
    }

    @Override
    public List<String> getAvailableHashtags(){
        return this.availableHashtags;
    }

    @Override
    public void addNewHashtag(String hashtag){
        if(!availableHashtags.contains(hashtag)) {
            this.availableHashtags.add(hashtag);
        }
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
