package cn.aethli.hermes.routes

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse

import static org.springframework.web.reactive.function.server.RequestPredicates.POST
import static org.springframework.web.reactive.function.server.RouterFunctions.route
import static org.springframework.web.reactive.function.server.ServerResponse.ok

/**
 * @author 93162*    */
@Configuration
class SSERouter {
    @Bean
    RouterFunction<ServerResponse> indexRouter() {
        route(
                POST("sse/subscribe"),
                {
                    request ->
                        ok()
                                .contentType(MediaType.TEXT_EVENT_STREAM)
                                .bodyValue(null)
                })
    }
}
