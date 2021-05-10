package cn.aethli.hermes.subscribe

import cn.aethli.hermes.model.SSEMessage

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * @author 93162*      */
class SSEManager {
    private static final Map<String, Map<String, Queue<SSEMessage>>> THEME_ID_MESSAGE = new ConcurrentHashMap<>()

    static synchronized Map<String, Queue<SSEMessage>> addTheme(String themeName) {
        Map<String, Queue<SSEMessage>> idMessageMap = THEME_ID_MESSAGE.get(themeName)
        if (idMessageMap != null) {
            idMessageMap
        } else {
            Map<String, Queue<SSEMessage>> themeMap = new ConcurrentHashMap<>()
            THEME_ID_MESSAGE.put(themeName, themeMap)
            themeMap
        }
    }

    static synchronized Queue<SSEMessage> addId(String themeName, String id) {
        Map<String, Queue<SSEMessage>> idMessageMap = addTheme(themeName)
        Queue<SSEMessage> idMessageQueue = idMessageMap.get(id)
        if (idMessageQueue != null) {
            idMessageQueue
        } else {
            Queue<SSEMessage> sseMessageQueue = new ConcurrentLinkedDeque<SSEMessage>()
            idMessageMap.put(id, sseMessageQueue)
            sseMessageQueue
        }
    }

    static void releaseMessageById(String themeName, String id, SSEMessage message) {
        Queue<SSEMessage> messageList = addId(themeName, id)
        messageList.offer(message)
    }

    static Queue<SSEMessage> readMessageByThemeId(String themeName, String id) {
        addId(themeName, id)
    }
}
