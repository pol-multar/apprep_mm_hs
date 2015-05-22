package fr.unice.polytech.si4.apprep.serveur;

/**
 * The retweets
 * @author Hugo
 *
 */
public class ReTweet extends Tweet {
    private String authorSource;

    public ReTweet(int id, String author, Tweet t) {
        super(id, author, t.getMessage());
        authorSource = t.getAuthorSource();
    }

    @Override
    public String toString(){
        return "[n°" + id + "] " + author + " a retweeté : " + message + " (source : "+authorSource+")";
    }

    // Getters & Setters

    @Override
    public String getAuthorSource(){
        return authorSource;
    }
}
