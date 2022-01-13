package wegrus.clubwebsite.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisServer;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Profile({"test", "local"})
@Configuration
public class EmbeddedRedisConfig {

    @Value("${spring.redis.url}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    private RedisServer redisServer;

    @PostConstruct
    public void setRedisServer() throws IOException {
        redisServer = new RedisServer(host, port);
    }
}
