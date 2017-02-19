package demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Component
public class ImportResources implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${server.port}")
    private String port;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        URI baseUri = URI.create("http://localhost:" + port);
        DataFlowTemplate dataFlowTemplate = new DataFlowTemplate(baseUri);
        try {
            // Import event stream apps
            dataFlowTemplate.appRegistryOperations()
                    .importFromResource(new URL(baseUri.toURL(), "app.properties").toString(), false);

            // Import RabbitMQ stream apps
            dataFlowTemplate.appRegistryOperations()
                    .importFromResource("http://bit.ly/stream-applications-kafka-maven", false);

            // Deploy a set of event stream definitions
            List<StreamApp> streams = Arrays.asList(
                    new StreamApp("account-event-stream",
                            "account-web > :account-stream"),
                    new StreamApp("account-event-processor",
                            ":account-stream > account-worker"),
                    new StreamApp("account-event-counter",
                            ":account-stream > field-value-counter --field-name=type --name=account-events"),
                    new StreamApp("order-event-stream",
                            "order-web > :order-stream"),
                    new StreamApp("order-event-processor",
                            ":order-stream > order-worker"),
                    new StreamApp("order-event-counter",
                            ":order-stream > field-value-counter --field-name=type --name=order-events"),
                    new StreamApp("payment-event-stream",
                            "payment-web > :payment-stream"),
                    new StreamApp("payment-event-processor",
                            ":payment-stream > payment-worker"),
                    new StreamApp("payment-event-counter",
                            ":payment-stream > field-value-counter --field-name=type --name=payment-events"),
                    new StreamApp("warehouse-event-stream",
                            "warehouse-web > :warehouse-stream"),
                    new StreamApp("warehouse-event-processor",
                            ":warehouse-stream > warehouse-worker"),
                    new StreamApp("warehouse-event-counter",
                            ":warehouse-stream > field-value-counter --field-name=type --name=warehouse-events"),
                    new StreamApp("account-load-simulator", "time --time-unit=SECONDS --initial-delay=60 " +
                            "--fixed-delay=30 | " +
                            "load-simulator --domain=ACCOUNT --operation=CREATE > :load-log"),
                    new StreamApp("inventory-load-simulator", "time --time-unit=SECONDS --initial-delay=60 " +
                            "--fixed-delay=1 | " +
                            "load-simulator --domain=INVENTORY --operation=CREATE --range=10 > :load-log"),
                    new StreamApp("order-load-simulator", "time --time-unit=SECONDS --initial-delay=80 " +
                            "--fixed-delay=5 | " +
                            "load-simulator --command=POST_ORDER --domain=ACCOUNT --operation=CREATE --range=5 > :load-log"),
                    new StreamApp("account-counter",
                            ":account-stream > counter --name-expression=payload.type.toString()"),
                    new StreamApp("order-counter",
                            ":order-stream > counter --name-expression=payload.type.toString()"),
                    new StreamApp("warehouse-counter",
                            ":warehouse-stream > counter --name-expression=payload.type.toString()"));

            // Deploy the streams in parallel
            streams.parallelStream()
                    .forEach(stream -> dataFlowTemplate.streamOperations()
                            .createStream(stream.getName(), stream.getDefinition(), true));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    static class StreamApp {
        private String name;
        private String definition;

        public StreamApp(String name, String definition) {
            this.name = name;
            this.definition = definition;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }
    }
}
