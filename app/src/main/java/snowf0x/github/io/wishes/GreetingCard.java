package snowf0x.github.io.wishes;

import java.util.ArrayList;

public class GreetingCard {
    private ArrayList<String> pages;
    private boolean showAnimation;
    private boolean backgroundMusic;
    private String musicURI, coverURI;
    private String message,title,id;
    public GreetingCard(){}
    public GreetingCard(ArrayList<String> pages, boolean showAnimation, boolean backgroundMusic, String musicURI, String coverURI, String message, String title) {
        this.pages = pages;
        this.showAnimation = showAnimation;
        this.backgroundMusic = backgroundMusic;
        this.musicURI = musicURI;
        this.coverURI = coverURI;
        this.message = message;
        this.title = title;
    }

    public ArrayList<String> getPages() {
        return pages;
    }

    public void setPages(ArrayList<String> pages) {
        this.pages = pages;
    }

    public boolean isShowAnimation() {
        return showAnimation;
    }

    public void setShowAnimation(boolean showAnimation) {
        this.showAnimation = showAnimation;
    }

    public boolean isBackgroundMusic() {
        return backgroundMusic;
    }

    public void setBackgroundMusic(boolean backgroundMusic) {
        this.backgroundMusic = backgroundMusic;
    }

    public String getCoverURI() {
        return (coverURI);
    }

    public void setCoverURI(String coverURI) {
        this.coverURI = coverURI;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMusicURI() {
        return musicURI;
    }

    public void setMusicURI(String musicURI) {
        this.musicURI = musicURI;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
