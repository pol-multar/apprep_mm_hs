package fr.unice.polytech.si4.apprep.test;

import java.io.Serializable;
/**
 * @author Maxime
 * @version 22/05/2015.
 */

public class EventMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    private int messageId;
    private String messageText = "";

    public EventMessage(int id, String messageText) {
        this.messageId = id;
        this.messageText = messageText;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
    public String toString(){
        return "Message Id = "+getMessageId()+" ; Message Text = "+getMessageText();
    }

}
