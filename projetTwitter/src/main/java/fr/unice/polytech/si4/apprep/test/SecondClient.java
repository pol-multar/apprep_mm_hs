package fr.unice.polytech.si4.apprep.test;

/**
 * @author Maxime
 * @version 22/05/2015.
 */
import java.util.Properties;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class SecondClient {
    private Context context = null;
    private TopicConnectionFactory factory = null;
    private TopicConnection connection = null;
    private TopicSession session = null;
    private Topic topic = null;
    private TopicSubscriber subscriber = null;

    public SecondClient() {

    }

    public void receiveMessage() {
        Properties initialProperties = new Properties();
        initialProperties.put(InitialContext.INITIAL_CONTEXT_FACTORY,
                "org.exolab.jms.jndi.InitialContextFactory");
        initialProperties.put(InitialContext.PROVIDER_URL,
                "tcp://localhost:3035");
        try {
            context = new InitialContext(initialProperties);
            factory = (TopicConnectionFactory) context
                    .lookup("ConnectionFactory");
            topic = (Topic) context.lookup("topic1");
            connection = factory.createTopicConnection();
            session = connection.createTopicSession(false,
                    TopicSession.AUTO_ACKNOWLEDGE);
            subscriber = session.createSubscriber(topic);
            connection.start();
            Message message = subscriber.receive();
            if (message instanceof ObjectMessage) {
                Object object = ((ObjectMessage) message).getObject();
                System.out.println(this.getClass().getName()
                        + " has received a message : " + (EventMessage) object);
            }

        } catch (NamingException e) {

            e.printStackTrace();
        } catch (JMSException e) {

            e.printStackTrace();
        }
        if (context != null) {
            try {
                context.close();
            } catch (NamingException ex) {
                ex.printStackTrace();
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        SecondClient secondClient = new SecondClient();
        secondClient.receiveMessage();

    }

}


