package kr.hs.dgsw;

import java.io.Serializable;

enum ChatMessageType {
    MESSAGE, LOGOUT // 2개만 담을 수 있음 (MESSAGE, LOGOUT)
}

public class ChatMessage implements Serializable { // 자바의 객체를 외부로 보낼때 바이트 단위로 쪼개야하기 때문에 = 직렬화 (Serializable)
    private ChatMessageType type;
    private String message; // message 담을 필드

    ChatMessage(ChatMessageType type, String message) {
        this.type = type;
        this.message = message;
    }

    ChatMessageType getType() {
        return type;
    }

    String getMessage() {
        return message;
    }
}
