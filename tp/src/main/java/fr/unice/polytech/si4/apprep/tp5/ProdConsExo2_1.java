import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.activemq.ActiveMQConnectionFactory;


public class ProdConsExo2_1 implements javax.jms.MessageListener{

    private javax.jms.Connection connect = null;
    private javax.jms.Session sendSession = null;
    private javax.jms.Session receiveSession = null;
    private javax.jms.MessageProducer sender = null;
    private javax.jms.Queue queue = null;

    private void configurer() {
        
        try
        {	// Create a connection.
            javax.jms.ConnectionFactory factory;
            factory = new ActiveMQConnectionFactory("user", "password", "tcp://localhost:61616");
            connect = factory.createConnection ("user", "password");
            // ce programme est donc en mesure d'accéder au broker ActiveMQ, avec connecteur tcp (openwire)
            // Si le producteur et le consommateur étaient codés séparément, ils auraient eu ce même bout de code
            
            this.configurerProducteur();
            this.configurerConsommateur();
            connect.start(); // on peut activer la connection. 
            
        } catch (javax.jms.JMSException jmse){
            jmse.printStackTrace();
        }
            
            //this.produire();
    }
    private void configurerProducteur() throws JMSException{
    	// Dans ce programme, on decide que le producteur crée la queue

        //La queue etant crée, il peut y accéder en mode producteur, au sein d'une session
    	sendSession = connect.createSession(false,javax.jms.Session.AUTO_ACKNOWLEDGE);
        queue = sendSession.createQueue ("queueExo2");
        sender = sendSession.createProducer(queue);
    }
    
    private void configurerConsommateur() throws JMSException{
    	// Pour consommer, il faudra simplement ouvrir une session 
        receiveSession = connect.createSession(false,javax.jms.Session.AUTO_ACKNOWLEDGE);  
        javax.jms.MessageConsumer qReceiver = receiveSession.createConsumer(queue);
        qReceiver.setMessageListener(this);
        // Now that 'receive' setup is complete, start the Connection
       
    }
    private void produire(){
    	for (int i=1;i<=10;i++){
    		//Fabriquer un message
    		
    		//Poster ce message dans la queue
    		//Question1: fabriquer et poster le message via la console d'aministration
    	}
    }
	public static void main(String[] args) {
		(new ProdConsExo2_1()).configurer();
		

	}

	@Override
	public void onMessage(Message message) {
		// Methode permettant au consommateur de consommer effectivement chaque msg recu
		// via la queue
		System.out.println("Recu un message de la queue");
	}

}
