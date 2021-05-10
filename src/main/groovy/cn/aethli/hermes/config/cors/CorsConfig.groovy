package cn.aethli.hermes.config.cors

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer


@Configuration
@EnableScheduling
class CorsConfig {


    @Bean
    WebFluxConfigurer corsConfigurer() {
        return new WebFluxConfigurer() {
            @Override
            void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("*")
                        .allowCredentials(true)
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .maxAge(3600)
            }
        }
    }
}