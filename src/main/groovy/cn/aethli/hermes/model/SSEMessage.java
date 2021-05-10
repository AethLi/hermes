package cn.aethli.hermes.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class SSEMessage {
    private String messageId;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date sendTime = new Date();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date releaseTime;

    public SSEMessage(String messageId, String content, Date releaseTime) {
        this.messageId = messageId;
        this.content = content;
        this.releaseTime = releaseTime;
    }
}
