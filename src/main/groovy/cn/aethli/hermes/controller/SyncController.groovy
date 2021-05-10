package cn.aethli.hermes.controller

import cn.aethli.hermes.Bo.ReleaseBo
import cn.aethli.hermes.model.SSEMessage
import cn.aethli.hermes.subscribe.SSEManager
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.ObjectUtils
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

import javax.annotation.Resource
import java.util.stream.Stream

@RestControllerAdvice
@RequestMapping("sync")
class SyncController {
    @Resource
    ObjectMapper defaultMapper

    @GetMapping(value = "sse/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> sseSubscribe(@RequestParam String themeName, @RequestParam String id) {
        Queue<SSEMessage> messageQueue = SSEManager.readMessageByThemeId(themeName, id)
        if (messageQueue == null) {
            return Flux.empty()
        }
        Stream<SSEMessage> messageStream = messageQueue.stream()
        Flux<String> result = Flux.fromStream(messageStream.map({ it -> defaultMapper.writeValueAsString(it) }))
        result
    }

    @PostMapping(value = "sse/release")
    ResponseEntity<ObjectUtils.Null> sseRelease(@RequestBody ReleaseBo releaseBo) {
        SSEManager.releaseMessageById(releaseBo.getThemeName(), releaseBo.getId(), new SSEMessage(UUID.randomUUID().toString(), releaseBo.getContent(), new Date()))
        ResponseEntity.ok(null)
    }
}
