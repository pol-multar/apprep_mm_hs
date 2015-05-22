package fr.unice.polytech.si4.apprep.client;

import fr.unice.polytech.si4.apprep.serveur.TwitterRemote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Hashtable;
import java.util.List;

import javax.jms.Destination;
import javax.jms.MessageListener;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.jms.Connection;
import javax.jms.Session;


public class Client implements MessageListener {
    public static String brokerURL = "tcp://localhost:61616";
    private static final int PORT = 2345;
    private List<Destination> allTopics;

    private Connection connect=null;
    private Session receiveSession=null;
    InitialContext context = null;


    private void configurer() throws JMSException {

        try
        {	// Create a connection
            Hashtable properties = new Hashtable();
            properties.put(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            properties.put(Context.PROVIDER_URL, "tcp://localhost:61616");

            context = new InitialContext(properties);

            javax.jms.ConnectionFactory factory = (ConnectionFactory) context.lookup("ConnectionFactory");
            connect = factory.createConnection();

            this.configurerSouscripteur();
            connect.start(); // on peut activer la connection.
        } catch (javax.jms.JMSException jmse){
            jmse.printStackTrace();
        } catch (NamingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private void configurerSouscripteur() throws JMSException, NamingException{
        // Pour consommer, il faudra simplement ouvrir une session
        receiveSession = connect.createSession(false,javax.jms.Session.AUTO_ACKNOWLEDGE);
        // et dire dans cette session quelle queue(s) et topic(s) on accèdera et dans quel mode
        Topic topic = (Topic) context.lookup("dynamicTopics/topicExo2");
        System.out.println("Nom du topic " + topic.getTopicName());
        javax.jms.MessageConsumer topicReceiver = receiveSession.createConsumer(topic);//,"Conso");//,"typeMess = 'important'");
        //topicReceiver.setMessageListener(this);
        //ESSAI d'une reception synchrone
        connect.start(); // on peut activer la connection.
        while (true){
            Message m= topicReceiver.receive();
            System.out.print("recept synch: "); onMessage(m);
        }
    }



    /**
     * The main of the client
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            String username = "";
            boolean connected = false;
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    System.in));
            System.out.println("*** CONNEXION ***");
            Registry reg = LocateRegistry.getRegistry(2345);
            TwitterRemote twitterRemote = (TwitterRemote) reg
                    .lookup("rmi://localhost:" + PORT + "/TwitterServer");
            // Loop until the user is connected
            while (!connected) {
                System.out.print("Entrez votre username : ");
                username = br.readLine();
                System.out.print("Entrez votre mot de passe : ");
                String pwd = br.readLine();
                connected = twitterRemote.connect(username, pwd);
                if (!connected) {
                    System.out.println("La connexion a échoué...");
                }
            }
            System.out.println("*** CONNEXION REUSSIE ***\n"
                    + "*** BIENVENUE SUR TWITTER ***");

            boolean wannaQuit = false;
            int rep;
            // Loop until the user wants to quit
            while (!wannaQuit) {
                System.out.print("1 - Tweeter\n" + "2 - Retweeter\n" + "3 - S'abonner\n"
                        + "4 - Quitter\n" + ">");

                try {
                    rep = Integer.parseInt(br.readLine());
                    switch (rep) {
                        // 1 - Tweeter
                        case 1:
                            System.out.print("Tweeter : ");
                            twitterRemote.tweet(username, br.readLine());
                            break;
                        // 2 - Retweeter
                        case 2:
                            System.out.print("Retweeter : n°");
                            try {
                                twitterRemote.retweet(username, Integer.parseInt(br
                                        .readLine()));
                            } catch (NumberFormatException e) {
                            }
                            break;
                        // 3 - Quitter
                        case 3:
                            // TODO display the available subscriptions
                            try{
                                (new Client()).configurer();
                            }catch (JMSException e) {
                                e.printStackTrace();
                            }
                            System.out.print("S'abonner à : #");
                            String hashtag = "#"+br.readLine();
                            // TODO subscribe
                            break;
                        // 4 - Quitter
                        case 4:
                            wannaQuit = true;
                            break;
                    }
                } catch (NumberFormatException e) {
                }
            }

            System.out.println("*** A BIENTOT ! ***");

        } catch (NotBoundException | IOException e) {
            System.out.println("Connexion impossible.");
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message) {
        // Methode permettant au souscripteur de consommer effectivement chaque msg recu
        // via le topic auquel il a souscrit
        try {
            System.out.print("Recu un message du topic: "+((MapMessage)message).getString("nom"));
            System.out.println(((MapMessage)message).getString("num"));
        } catch (JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
