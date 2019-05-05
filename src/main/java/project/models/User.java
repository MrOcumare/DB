package project.models;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private String fullname;
    private String nickname;
    private String email;
    private String about;

    public User() {
        this.fullname = "";
        this.nickname = "";
        this.email = "";
        this.about = "";
    }

    @JsonCreator
    public User(
            @JsonProperty("fullname") String fullname,
            @JsonProperty("nickname") String nickname,
            @JsonProperty("email") String email,
            @JsonProperty("about") String about
    ) {
        this.nickname = nickname;
        this.about = about;
        this.email = email;
        this.fullname = fullname;
    }

    public String getFullname() {
        return fullname;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public String getAbout() {
        return about;
    }


    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAbout(String about) {
        this.about = about;
    }

}