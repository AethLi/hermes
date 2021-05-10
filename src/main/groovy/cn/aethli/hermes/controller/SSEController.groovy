package cn.aethli.hermes.controller

import cn.aethli.hermes.Bo.ReleaseBo
import cn.aethli.hermes.model.SSEMessage
import cn.aethli.hermes.subscribe.SSEManager
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.ObjectUtils
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

import javax.annotation.Resource
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream

@RestControllerAdvice
@RequestMapping("sync/sse")
class SSEController {
    @Resource
    ObjectMapper defaultMapper

    @GetMapping(value = "subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<?> sseSubscribe(@RequestParam String themeName, @RequestParam String id) {
        Flux.interval(Duration.ofSeconds(1))
                .map({
                    seq ->
                        Queue<SSEMessage> messageQueue = SSEManager.readMessageByThemeId(themeName, id)
                        if (!messageQueue.isEmpty()) {
                            String messageData = defaultMapper.writeValueAsString(messageQueue.remove())
                            ServerSentEvent
                                    .builder()
                                    .event("message")
                                    .id(seq as String)
                                    .data(messageData)
                                    .build()
                        }else {
                            ServerSentEvent.builder().build()
                        }

                })
    }

    @GetMapping(value = "test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> flux() {
        Flux<String> result = Flux.fromStream(
                IntStream.range(1, 200).mapToObj({ i ->
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                    }
                    return "flux data--" + i;
                })
        )
        return result
    }

    @PostMapping(value = "release")
    ResponseEntity<ObjectUtils.Null> sseRelease(@RequestBody ReleaseBo releaseBo) {
        SSEManager.releaseMessageById(releaseBo.getThemeName(), releaseBo.getId(), new SSEMessage(UUID.randomUUID().toString(), releaseBo.getContent(), new Date()))
        ResponseEntity.ok(null)
    }
}
