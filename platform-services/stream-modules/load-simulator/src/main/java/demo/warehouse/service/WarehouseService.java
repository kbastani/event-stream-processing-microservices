package demo.warehouse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.inventory.domain.Inventory;
import demo.warehouse.domain.Warehouse;
import org.apache.log4j.Logger;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class WarehouseService {

    private final Logger log = Logger.getLogger(this.getClass());
    private final RestTemplate restTemplate;

    public WarehouseService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

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

    public List<Inventory> addInventory(List<Inventory> inventory, Long warehouseId) {
        List<Inventory> result = new ArrayList<>();
        try {
            inventory.parallelStream().forEach(item -> {
                result.add(restTemplate.postForObject(new UriTemplate(String
                        .format("http://warehouse-web/v1/warehouses/%s/inventory", warehouseId))
                        .expand(), item, Inventory.class));
            });
        } catch (RestClientResponseException ex) {
            log.error("Add warehouse inventory failed", ex);
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
