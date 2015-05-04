package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import server.TwitterRemote;

public class Client {
    private static final int PORT = 2345;

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
            
            System.out.println("*** À BIENTÔT ! ***");

        } catch (NotBoundException | IOException e) {
            System.out.println("Connexion impossible.");
        }
    }
}
