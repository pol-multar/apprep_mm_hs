package fr.unice.polytech.si4.apprep.test;

/**
 * @author Maxime
 * @version 22/05/2015.
 */
import java.util.Hashtable;

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

import org.apache.activemq.ActiveMQConnectionFactory;


public class Pub{

    private javax.jms.Connection connect = null;
    private javax.jms.Session sendSession = null;
    private javax.jms.MessageProducer sender = null;
    private javax.jms.Queue queue = null;
    InitialContext context = null;
    private void configurer() throws JMSException {

        try
        {	// Create a connection
            // Si le producteur et le consommateur étaient codés séparément, ils auraient eu ce même bout de code

            Hashtable properties = new Hashtable();
            properties.put(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            properties.put(Context.PROVIDER_URL, "tcp://localhost:61616");

            context = new InitialContext(properties);

            javax.jms.ConnectionFactory factory = (ConnectionFactory) context.lookup("ConnectionFactory");
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
        // Dans ce programme, on decide que le producteur decouvre la queue (ce qui la crééra si le nom n'est pas encore utilisé)
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
            sender.send(mess); // equivaut à publier dans le topic
        }
    }
    public static void main(String[] args) {
        try {
            (new Pub()).configurer();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }


}

