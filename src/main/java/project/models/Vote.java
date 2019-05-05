package project.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Vote {

    private String nickname;
    private long voice;
    private long tid;


    @JsonCreator
    public Vote(
            @JsonProperty("nickname") String nickname,
            @JsonProperty("voice") long voice,
            @JsonProperty("tid") long tid
    ) {

        this.nickname = nickname;
        this.voice = voice;
        this.tid = tid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public long getVoice() {
        return voice;
    }

    public void setVoice(long voice) {
        this.voice = voice;
    }


    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }
}
