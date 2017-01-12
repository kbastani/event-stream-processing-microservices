package com.example;

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
                    .importFromResource("http://bit.ly/stream-applications-rabbit-maven", false);

            // Deploy a set of event stream definitions
            List<StreamApp> streams = Arrays.asList(
                    new StreamApp("account-stream",
                            "account-web: account-web | account-worker: account-worker"),
                    new StreamApp("order-stream",
                            "order-web: order-web | order-worker: order-worker"),
                    new StreamApp("payment-stream",
                            "payment-web: payment-web | payment-worker: payment-worker"),
                    new StreamApp("warehouse-stream",
                            "warehouse-web: warehouse-web | warehouse-worker: warehouse-worker"));

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
