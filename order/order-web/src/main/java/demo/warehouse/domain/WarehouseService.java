package demo.warehouse.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.domain.Service;
import demo.order.domain.Order;
import demo.warehouse.exception.WarehouseNotFoundException;
import org.apache.log4j.Logger;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Service
public class WarehouseService extends Service<Warehouse, Long> {

    private final Logger log = Logger.getLogger(this.getClass());
    private final RestTemplate restTemplate;

    public WarehouseService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Warehouse get(Long warehouseId) {
        Warehouse result;
        try {
            result = restTemplate.getForObject(new UriTemplate("http://warehouse-web/v1/warehouses/{id}")
                    .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                    .expand(warehouseId), Warehouse.class);
        } catch (RestClientResponseException ex) {
            log.error("Get warehouse failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    @Override
    public Warehouse create(Warehouse warehouse) {
        Warehouse result;
        try {
            result = restTemplate.postForObject(new UriTemplate("http://warehouse-web/v1/warehouses").expand(),
                    warehouse, Warehouse.class);
        } catch (RestClientResponseException ex) {
            log.error("Create warehouse failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    @Override
    public Warehouse update(Warehouse warehouse) {
        Warehouse result;
        try {
            result = restTemplate.exchange(new RequestEntity<>(warehouse, HttpMethod.PUT,
                    new UriTemplate("http://warehouse-web/v1/warehouses/{id}")
                            .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .expand(warehouse.getIdentity())), Warehouse.class).getBody();
        } catch (RestClientResponseException ex) {
            log.error("Update warehouse failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    @Override
    public boolean delete(Long warehouseId) {
        try {
            restTemplate.delete(new UriTemplate("http://warehouse-web/v1/warehouses/{id}")
                    .with("id", TemplateVariable.VariableType.PATH_VARIABLE).expand(warehouseId));
        } catch (RestClientResponseException ex) {
            log.error("Delete warehouse failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return true;
    }

    /**
     * Finds a {@link Warehouse} that is able to fulfill the supplied {@link Order}.
     *
     * @param order is the {@link Order} containing line items used to find a {@link Warehouse} to fulfill the order
     * @return a {@link Warehouse} that is able to fulfill the order
     * @throws WarehouseNotFoundException if no {@link Warehouse} has the inventory to fill the {@link Order}
     */
    public Warehouse findWarehouseWithInventory(Order order) throws WarehouseNotFoundException {
        Warehouse result;
        try {
            ResponseEntity<Warehouse> response = restTemplate
                    .postForEntity("http://warehouse-web/v1/warehouses/search/findWarehouseWithInventory", order,
                            Warehouse.class);

            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new WarehouseNotFoundException("Could not find warehouse with available inventory for order");
            } else {
                result = response.getBody();
            }
        } catch (RestClientResponseException ex) {
            log.error("Find warehouse failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Warehouse reserveInventory(Warehouse warehouse, Order order) throws WarehouseNotFoundException {
        Warehouse result;

        Traverson traverson = new Traverson(URI.create(warehouse.getLink("self").getHref()), MediaTypes.HAL_JSON);
        URI reserveUri = URI.create(traverson.follow("commands", "reserveOrder").asLink().getHref());

        // Convert URI to load-balanced href
        String reserveHref = URI.create(reserveUri.toString().replace(reserveUri.getHost(), "warehouse-web"))
                .toString();

        try {
            ResponseEntity<Warehouse> response = restTemplate.postForEntity(reserveHref, order, Warehouse.class);

            if (response.getStatusCode().is4xxClientError()) {
                throw new HttpClientErrorException(response.getStatusCode(), "Could not reserve inventory for order");
            } else {
                result = response.getBody();
            }
        } catch (RestClientResponseException ex) {
            log.error("Reserve inventory command failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    private String getHttpStatusMessage(RestClientResponseException ex) {
        Map<String, String> errorMap = new HashMap<>();
        try {
            errorMap = new ObjectMapper()
                    .readValue(ex.getResponseBodyAsString(), errorMap
                            .getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return errorMap.getOrDefault("message", null);
    }
}
