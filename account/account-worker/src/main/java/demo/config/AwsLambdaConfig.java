package demo.config;

import amazon.aws.AWSLambdaConfigurerAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.function.LambdaFunctions;
import demo.util.LambdaUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("lambda")
public class AwsLambdaConfig {

    @Bean
    public LambdaFunctions lambdaInvoker(AWSLambdaConfigurerAdapter configurerAdapter) {
        return configurerAdapter
                .getFunctionInstance(LambdaFunctions.class);
    }

    @Bean
    public LambdaUtil lambdaUtil(ObjectMapper objectMapper) {
        return new LambdaUtil(objectMapper);
    }
}
