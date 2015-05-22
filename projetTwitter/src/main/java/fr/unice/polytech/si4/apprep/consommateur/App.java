package fr.unice.polytech.si4.apprep.consommateur;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;


public class App implements MessageListener
{

    public static String brokerURL = "tcp://localhost:61616";

    private ConnectionFactory factory;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;

    public static void main( String[] args )
    {
        App app = new App();
        app.run();
    }

    public void run()
    {
        try
        {
            ConnectionFactory factory = new ActiveMQConnectionFactory(brokerURL);
            connection = factory.createConnection();
            connection.start();
            // Pour consommer, il faut simplement ouvrir une session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // et dire dans cette session quelle queue(s) et topic(s) on accèdera et dans quel mode
            Destination destination = session.createQueue("test");
            System.out.println("Nom de la queue " + destination);
            //MessageConsumer est typé en QueueReceiver puisque on a passé queue comme param.
            consumer = session.createConsumer(destination);
            consumer.setMessageListener(this);
            //Chaque topic aura son objet consumer
        }
        catch (Exception e)
        {
            System.out.println("Caught:" + e);
            e.printStackTrace();
        }
    }


    public void onMessage(Message message)
    {
        try
        {
            if (message instanceof TextMessage)
            {
                TextMessage txtMessage = (TextMessage)message;
                System.out.println("Message received: " + txtMessage.getText());
            }
            else
            {
                System.out.println("Invalid message received.");
            }
        }
        catch (JMSException e)
        {
            System.out.println("Caught:" + e);
            e.printStackTrace();
        }
    }
}
