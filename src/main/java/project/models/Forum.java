package project.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Forum {
    private String slug;
    private String title;
    private String user;



    ///******
    private long id;
    private long posts;
    private long threads;
    @JsonCreator
    public Forum(//
            @JsonProperty("id") long id,
            //*****
            @JsonProperty("slug") String slug,
            @JsonProperty("title") String title,
            @JsonProperty("user") String user,
            ///*****
            @JsonProperty("postCount") long postCount,
            @JsonProperty("threadCount") long threadCount
    ) {
        this.slug = slug;
        this.title = title;
        this.user = user;

        //*****
        this.id = id;
        this.posts = postCount;
        this.threads = threadCount;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String master) {
        this.user = master;
    }
////////*******
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPosts() {
        return posts;
    }

    public void setPosts(long posts) {
        this.posts = posts;
    }

    public long getThreads() {
        return threads;
    }

    public void setThreads(long threads) {
        this.threads = threads;
    }

}
