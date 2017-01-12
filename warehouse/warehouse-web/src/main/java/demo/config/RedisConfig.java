package demo.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({ "docker", "cloud", "development" })
public class RedisConfig {

    @Bean
    public RedissonClient redissonClient() {
        return Redisson.create();
    }
}
