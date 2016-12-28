package demo.domain;

import lombok.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Link;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.function.Consumer;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ProviderTests.class, ProviderTests.EmptyConfiguration.class, ProviderTests.EmptyProvider.class, ProviderTests.EmptyService.class, ProviderTests.EmptyAction.class})
public class ProviderTests {

    private AnnotationConfigApplicationContext context;

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Before
    public void setup() {
        load(EmptyProvider.class);
        assertNotNull(context);
    }

    @Test
    public void testGetProviderReturnsProvider() {
        assertNotNull(new EmptyAggregate().getProvider(EmptyProvider.class));
    }

    @Test
    public void testGetServiceReturnsService() {
        EmptyProvider provider = new EmptyAggregate().getProvider(EmptyProvider.class);
        assertNotNull(provider.getEmptyService());
    }

    @Test
    public void testGetActionReturnsAction() {
        EmptyProvider provider = new EmptyAggregate().getProvider(EmptyProvider.class);
        EmptyService service = provider.getEmptyService();
        assertNotNull(service.getAction(EmptyAction.class));
    }

    @Test
    public void testProcessCommandChangesStatus() {
        EmptyAggregate aggregate = new EmptyAggregate(0L, AggregateStatus.CREATED);
        EmptyProvider provider = new EmptyAggregate().getProvider(EmptyProvider.class);
        EmptyService service = provider.getEmptyService();
        EmptyAction emptyAction = service.getAction(EmptyAction.class);
        emptyAction.getConsumer().accept(aggregate);
        assertEquals(aggregate.getStatus(), AggregateStatus.PROCESSED);
    }

    @Test
    public void testProcessEmptyAggregateChangesStatus() {
        EmptyAggregate aggregate = new EmptyAggregate(0L, AggregateStatus.CREATED);
        aggregate.emptyAction();
        assertEquals(aggregate.getStatus(), AggregateStatus.PROCESSED);
    }

    @Test
    public void testAggregateProducesCommandLinks() {
        EmptyAggregate aggregate = new EmptyAggregate(0L, AggregateStatus.CREATED);
        Assert.notEmpty(aggregate.getLinks());
        Assert.notEmpty(aggregate.getCommands().getLinks());
    }

    private void load(Class<?> provider, String... environment) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        EnvironmentTestUtils.addEnvironment(applicationContext, environment);
        applicationContext.register(EmptyConfiguration.class, provider, EmptyService.class, EmptyAction.class, EmptyController.class);
        applicationContext.refresh();
        this.context = applicationContext;
    }


    @Configuration
    public static class EmptyConfiguration {
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class EmptyAggregate extends Aggregate<Long> {
        @NonNull
        private Long id;
        @NonNull
        private AggregateStatus status;

        @Command(controller = EmptyController.class, method = "emptyAction")
        public void emptyAction() {
            EmptyProvider emptyProvider = this.getProvider();
            emptyProvider.getEmptyService()
                    .getAction(EmptyAction.class)
                    .getConsumer()
                    .accept(this);
        }

        @Override
        public Link getId() {
            return new Link("example.com").withSelfRel();
        }

        @Override
        Long getIdentity() {
            return this.id;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class EmptyProvider extends Provider<EmptyAggregate> {
        private final EmptyService emptyService;

        public Service<? extends EmptyAggregate> getDefaultService() {
            return emptyService;
        }
    }

    public static class EmptyService extends Service<EmptyAggregate> {
        public EmptyAggregate getEmptyAggregate(Long id) {
            return new EmptyAggregate(id, AggregateStatus.CREATED);
        }
    }

    public static class EmptyAction extends Action<EmptyAggregate> {

        public Consumer<EmptyAggregate> getConsumer() {
            return a -> a.setStatus(AggregateStatus.PROCESSED);
        }
    }

    @RestController
    @RequestMapping("/v1")
    public static class EmptyController {

        private EmptyProvider provider;

        public EmptyController(EmptyProvider provider) {
            this.provider = provider;
        }

        @RequestMapping(value = "/empty/{id}", method = RequestMethod.GET)
        public EmptyAggregate emptyAction(@PathVariable("id") Long id, @RequestParam("q") String query) {
            return provider.getEmptyService().getEmptyAggregate(id);
        }
    }

    public enum AggregateStatus {
        CREATED,
        PROCESSED
    }
}