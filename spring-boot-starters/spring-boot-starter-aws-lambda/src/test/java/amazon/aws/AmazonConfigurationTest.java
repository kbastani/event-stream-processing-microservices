package amazon.aws;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

import static junit.framework.TestCase.assertNotNull;

public class AmazonConfigurationTest {

    private AnnotationConfigApplicationContext context;

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void defaultAdapter() {
        load(EmptyConfiguration.class,
                "amazon.aws.access-key-id=AJGLDLSXKDFLS",
                "amazon.aws.access-key-secret=XSDFSDFLKKHASDFJALASDF");

        AWSLambdaConfigurerAdapter amazonS3Template = this.context.getBean(AWSLambdaConfigurerAdapter.class);
        assertNotNull(amazonS3Template);
    }

    @Configuration
    static class EmptyConfiguration {
    }

    private void load(Class<?> config, String... environment) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        EnvironmentTestUtils.addEnvironment(applicationContext, environment);
        applicationContext.register(config);
        applicationContext.register(AmazonAutoConfiguration.class);
        applicationContext.refresh();
        this.context = applicationContext;
    }
}
