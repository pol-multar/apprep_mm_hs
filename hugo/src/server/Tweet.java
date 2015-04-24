package server;

import java.util.ArrayList;
import java.util.List;

/**
 * The Tweets
 * 
 * @author Hugo
 *
 */
public class Tweet {
    protected int id;
    protected String author;
    protected String message;
    protected List<String> hashtags;

    public Tweet(int id, String author, String message) {
        this.id = id;
        this.author = author;
        this.message = message;
        hashtags = new ArrayList<String>();
        String[] words = message.split(" ");
        for(String word : words){
            if(word.substring(0, 1).equals("#")){
                hashtags.add(word);
            }
        }
    }
    
    @Override
    public String toString(){
        return "[n�" + id + "] " + author + " a tweet� : " + message;
    }

    // Getters & Setters

    public int getId() {
        return id;
    }
    
    public String getAuthor() {
        return author;
    }

    public String getAuthorSource() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getHashtags() {
        return hashtags;
    }
}
