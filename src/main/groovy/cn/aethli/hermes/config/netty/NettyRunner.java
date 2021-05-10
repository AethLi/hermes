package cn.aethli.hermes.config.netty;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author 93162
 **/
@Component
@Order(2)
@Slf4j
public class NettyRunner implements CommandLineRunner {
    @Resource
    NettyServer nettyServer;

    @Override
    public void run(String... args) {
        //直接跑
        Thread thread = new Thread(() -> {
            try {
                nettyServer.start();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
        thread.start();
    }
}
