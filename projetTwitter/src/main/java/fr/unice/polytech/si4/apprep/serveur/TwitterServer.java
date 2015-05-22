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
    //JMS Part
    private Connection connect;
    private Session sendSession = null;
    private MessageProducer sender = null;
    InitialContext context = null;


    protected TwitterServer() throws RemoteException {
        super();
        tweets = new ConcurrentHashMap<Integer, Tweet>();
        logins = new ConcurrentHashMap<String,String>();
        availableHashtags = new ArrayList<String>();
        System.out.println("Serveur lanc� !");
    }

    private void configuration() throws JMSException {
        try
        {	// Create a connection
            // Si le producteur et le consommateur �taient cod�s s�par�ment, ils auraient eu ce m�me bout de code

            Hashtable properties = new Hashtable();
            properties.put(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            properties.put(Context.PROVIDER_URL, "tcp://localhost:61616");

            context = new InitialContext(properties);

            ConnectionFactory factory = (ConnectionFactory) context.lookup("ConnectionFactory");
            connect = factory.createConnection();

            this.configurerPublisher();
            //connect.start(); // on peut activer la connection.
        } catch (javax.jms.JMSException jmse){
            jmse.printStackTrace();
        } catch (NamingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.publier();
    }

    private void configurerPublisher() throws JMSException, NamingException{
        // Dans ce programme, on decide que le producteur decouvre la queue (ce qui la cr��ra si le nom n'est pas encore utilis�)
        // et y accedera au cours d'1 session
        sendSession = connect.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
        Topic topic = (Topic) context.lookup("dynamicTopics/topicExo2");
        sender = sendSession.createProducer(topic);
    }

    private void publier() throws JMSException{
        for (int i=1;i<=10;i++){
            //Fabriquer un message
            MapMessage mess = sendSession.createMapMessage();
            mess.setInt("num",i);
            mess.setString("nom",i+"-");
            if (i%2==0)
                mess.setStringProperty("typeMess","important");
            if (i==1) mess.setIntProperty("numMess",1);
            //Poster ce message dans la queue
            sender.send(mess); // equivaut � publier dans le topic
        }
    }

    @Override
    public boolean connect(String username, String pwd) {
        if ("username".equals(username) && "pwd".equals(pwd)) { // TODO username/pwd database
            System.out.println(username+" s'est connect�.");
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

        try {
            ts.configuration();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
