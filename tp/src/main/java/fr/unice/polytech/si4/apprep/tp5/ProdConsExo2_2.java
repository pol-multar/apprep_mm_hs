import java.util.Hashtable;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.activemq.ActiveMQConnectionFactory;


public class ProdConsExo2 implements javax.jms.MessageListener{

    private javax.jms.Connection connect = null;
    private javax.jms.Session sendSession = null;
    private javax.jms.Session receiveSession = null;
    private javax.jms.MessageProducer sender = null;
    private javax.jms.Queue queue = null;
    InitialContext context = null;
    private void configurer() {
        
        try
        {	// Create a connection
        	// Si le producteur et le consommateur �taient cod�s s�par�ment, ils auraient eu ce m�me bout de code
            
        	Hashtable properties = new Hashtable();
        	properties.put(Context.INITIAL_CONTEXT_FACTORY, 
        	    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        	properties.put(Context.PROVIDER_URL, "tcp://localhost:61616");

        	context = new InitialContext(properties);

        	javax.jms.ConnectionFactory factory = (ConnectionFactory) context.lookup("ConnectionFactory");
        	connect = factory.createConnection();
        
        	// On a echange l'ordre de config : d'abord le consommateur
        	// qui fait un lookup sur la queue
        	// =>On s'apercoit que m�me si la queue n'existe pas encore
        	// ce lookup a pour effet de d�clencher sa creation dynamique dans le broker
            this.configurerConsommateur(); 
            this.configurerProducteur();
            connect.start(); // on peut activer la connection. 
        } catch (javax.jms.JMSException jmse){
            jmse.printStackTrace();
        } catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
            
            //this.produire();
    }
    private void configurerProducteur() throws JMSException, NamingException{
    	// Dans ce programme, on decide que le producteur decouvre la queue (ce qui la cr��ra si le nom n'est pas encore utilis�) 
    	// et y accedera au cours d'1 session
    	sendSession = connect.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
    	Queue queue = (Queue) context.lookup("dynamicQueues/queueExo2");
    	sender = sendSession.createProducer(queue);
    }
    
    private void configurerConsommateur() throws JMSException, NamingException{
    	// Pour consommer, il faudra simplement ouvrir une session 
        receiveSession = connect.createSession(false,javax.jms.Session.AUTO_ACKNOWLEDGE);  
        // et dire dans cette session quelle queue(s) et topic(s) on acc�dera et dans quel mode
    	Queue queue = (Queue) context.lookup("dynamicQueues/queueExo2");
    	System.out.println("Nom de la queue " + queue.getQueueName());
        javax.jms.MessageConsumer qReceiver = receiveSession.createConsumer(queue);
        //MessageConsumer est typ� en QueueReceiver puisque on a pass� queue comme param.
        qReceiver.setMessageListener(this);       
    }
    
    private void produire(){
    	for (int i=1;i<=10;i++){
    		//Fabriquer un message
    		
    		//Poster ce message dans la queue
    		//Question1: fabriquer et poster le message via la console d'aministration
    	}
    }
	public static void main(String[] args) {
		(new ProdConsExo2()).configurer();
		

	}

	@Override
	public void onMessage(Message message) {
		// Methode permettant au consommateur de consommer effectivement chaque msg recu
		// via la queue
		System.out.println("Recu un message de la queue");
	}

}
