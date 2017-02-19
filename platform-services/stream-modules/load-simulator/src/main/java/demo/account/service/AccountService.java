package demo.account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.account.domain.Account;
import demo.order.domain.Order;
import org.apache.log4j.Logger;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class AccountService {

    private final Logger log = Logger.getLogger(this.getClass());
    private final RestTemplate restTemplate;

    public AccountService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Account get(Long accountId) {
        Account result;
        try {
            result = restTemplate.getForObject(new UriTemplate("http://account-web/v1/accounts/{id}")
                    .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                    .expand(accountId), Account.class);
        } catch (RestClientResponseException ex) {
            log.error("Get account failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Account create(Account account) {
        Account result;
        try {
            result = restTemplate.postForObject(new UriTemplate("http://account-web/v1/accounts").expand(),
                    account, Account.class);
        } catch (RestClientResponseException ex) {
            log.error("Create account failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Account update(Account account) {
        Account result;
        try {
            result = restTemplate.exchange(new RequestEntity<>(account, HttpMethod.PUT,
                    new UriTemplate("http://account-web/v1/accounts/{id}")
                            .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .expand(account.getIdentity())), Account.class).getBody();
        } catch (RestClientResponseException ex) {
            log.error("Update account failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public boolean delete(Long accountId) {
        try {
            restTemplate.delete(new UriTemplate("http://account-web/v1/accounts/{id}")
                    .with("id", TemplateVariable.VariableType.PATH_VARIABLE).expand(accountId));
        } catch (RestClientResponseException ex) {
            log.error("Delete account failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return true;
    }

    public Order postOrder(Long accountId, Order order) {
        Order result;
        try {
            result = restTemplate.postForObject(new UriTemplate(String
                            .format("http://account-web/v1/accounts/%s/commands/postOrder", accountId)).expand(),
                    order, Order.class);
        } catch (RestClientResponseException ex) {
            log.error("Post order to account has failed", ex);
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
