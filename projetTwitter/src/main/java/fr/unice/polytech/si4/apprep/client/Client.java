package fr.unice.polytech.si4.apprep.client;

import fr.unice.polytech.si4.apprep.serveur.TwitterRemote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


public class Client implements MessageListener {
    public static String brokerURL = "tcp://localhost:61616";
    private static final int PORT = 2345;
    TwitterRemote twitterRemote = null;
    BufferedReader br = null;
    List<String> myHashtags = null;

    private Connection connect = null;
    private Session receiveSession = null;
    InitialContext context = null;

    public Client() {
        myHashtags = new ArrayList<String>();
        try {
            configuration();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method in charges of the configuration of the MS part of the client
     * @throws JMSException
     */
    private void configuration() throws JMSException {
        try {
            // Create a connection
            Hashtable properties = new Hashtable();
            properties.put(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            properties.put(Context.PROVIDER_URL, "tcp://localhost:61616");

            context = new InitialContext(properties);

            ConnectionFactory factory = (ConnectionFactory) context.lookup("ConnectionFactory");
            connect = factory.createConnection();
            for (String s : myHashtags) {
                this.subscribeTo(s);
            }

            //connect.start(); -> fait dans la methode au dessus

        } catch (JMSException jmse) {
            jmse.printStackTrace();
        } catch (NamingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Method in charge of the subscription on a topic
     * @param s the topic to subscribe
     * @throws JMSException
     * @throws NamingException
     */
    private void subscribeTo(String s) throws JMSException, NamingException {
        // Pour consommer, il faudra simplement ouvrir une session
        receiveSession = connect.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
        // et dire dans cette session quelle queue(s) et topic(s) on accedera et dans quel mode
        Topic topic = (Topic) context.lookup("dynamicTopics/"+s);
        System.out.println("Abonnement au hashtag #" + topic.getTopicName());
        final String topicName = topic.getTopicName();
        final MessageConsumer topicReceiver = receiveSession.createConsumer(topic);

        connect.start(); // on peut activer la connection.

        //On cree le thread charge de l affichage des tweets recus par jms
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                System.out.println("Vous etes maintenant abonne au hashtag "+topicName);
                while (true) {
                    Message m = null;
                    try {
                        m = topicReceiver.receive();
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                    onMessage(m);
                    System.out.println("Recu depuis l abonnement : #" + topicName);
                }
            }
        });
        //On lance le thread cree
        t.start();


    }

    public void mainLoop() {//TODO tester avec plusieurs clients
        try {
            String username = "";
            boolean connected = false;
            br = new BufferedReader(new InputStreamReader(
                    System.in));
            System.out.println("*** CONNEXION ***");
            Registry reg = LocateRegistry.getRegistry(2345);
            twitterRemote = (TwitterRemote) reg
                    .lookup("rmi://localhost:" + PORT + "/TwitterServer");
            // Loop until the user is connected
            while (!connected) {
                System.out.print("Entrez votre username : ");
                username = br.readLine();
                System.out.print("Entrez votre mot de passe : ");
                String pwd = br.readLine();
                connected = twitterRemote.connect(username, pwd);
                if (!connected) {
                    System.out.println("La connexion a echoue...");
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
                            subscribeHashtag();
                            break;
                        // 4 - Quitter
                        case 4:
                            wannaQuit = true;
                            twitterRemote.disconnect(username);
                            break;
                    }
                } catch (NumberFormatException e) {
                }
            }

            System.out.println("*** A BIENTOT ! ***");
            System.exit(0);

        } catch (NotBoundException | IOException e) {
            System.out.println("Connexion impossible.");
            e.printStackTrace();
        }
    }

    /**
     * The method in charges of the subscribe procedure
     * It tests if the hashtag exists.
     * If it is, it calls the method to subscribe on it
     * If it isn't, it calls the method to create it and then it subscribes on it
     */
    private void subscribeHashtag() {
        System.out.println("Vous pouvez vous abonner a :");
        try {
            List<String> availableHashtags = twitterRemote.getAvailableHashtags();
            displayAvailableHashtags(availableHashtags);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println("Entrez le hashtag auquel vous voulez souscrire (sans #)");
        //Waiting for response
        try {
            String response = br.readLine();
            myHashtags.add(response);
            if(twitterRemote.getAvailableHashtags().contains(response)) {//Si le topic existe j y souscris directement
                subscribeTo(response);
            }else{
                System.out.println("Ce hastag n'a encore jamais ete utilise, creation du topic correspondant");
                twitterRemote.addNewHashtag(response); //Sinon je la cree avant
                subscribeTo(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    /**
     * Method in charge of displaying used Hashtags for the current user
     * @param hashtags the list of hashtags
     */
    private void displayAvailableHashtags(List<String> hashtags) {
        for (String s : hashtags) {
            System.out.println("#" + s);
        }
    }

    /**
     * Methode permettant au souscripteur de consommer effectivement chaque tweet recu
     * via le hashtag auquel il a souscrit
     * @param message
     */
    @Override
    public void onMessage(Message message) {
        try {
            System.out.println("Recu un message de : " + ((MapMessage) message).getString("author"));
            System.out.println(((MapMessage) message).getString("contenu"));
        } catch (JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * The main of the client
     *
     * @param args
     */
    public static void main(String[] args) {
        Client client = new Client();
        client.mainLoop();
    }


}
