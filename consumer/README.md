Assuming you’re run the producer at least once, you should have messages on your queue ready to consume. 
When you run the consumer you will see a flood of messages. 

$ mvn clean compile exec:java -Dexec.mainClass=net.timico.messaging.App

This process will not terminate, and will sit there waiting forever for messages – press CTRL-C to terminate. 