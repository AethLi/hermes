package cn.aethli.hermes.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SSEMessage {
    private String messageId;
    private String content;
    private Long sendTime = System.currentTimeMillis();

    public SSEMessage(String messageId, String content) {
        this.messageId = messageId;
        this.content = content;
    }
}
