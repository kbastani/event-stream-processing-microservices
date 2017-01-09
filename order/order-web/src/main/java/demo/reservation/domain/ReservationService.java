package demo.reservation.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.domain.Service;
import org.apache.log4j.Logger;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Service
public class ReservationService extends Service<Reservation, Long> {

    private final Logger log = Logger.getLogger(this.getClass());
    private final RestTemplate restTemplate;

    public ReservationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Reservation get(Long reservationId) {
        Reservation result;
        try {
            result = restTemplate.getForObject(new UriTemplate("http://warehouse-web/v1/reservations/{id}")
                    .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                    .expand(reservationId), Reservation.class);
        } catch (RestClientResponseException ex) {
            log.error("Get reservation failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    @Override
    public Reservation create(Reservation reservation) {
        Reservation result;
        try {
            result = restTemplate.postForObject(new UriTemplate("http://warehouse-web/v1/reservations").expand(),
                    reservation, Reservation.class);
        } catch (RestClientResponseException ex) {
            log.error("Create reservation failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    @Override
    public Reservation update(Reservation reservation) {
        Reservation result;
        try {
            result = restTemplate.exchange(new RequestEntity<>(reservation, HttpMethod.PUT,
                    new UriTemplate("http://warehouse-web/v1/reservations/{id}")
                            .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .expand(reservation.getIdentity())), Reservation.class).getBody();
        } catch (RestClientResponseException ex) {
            log.error("Update reservation failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    @Override
    public boolean delete(Long reservationId) {
        try {
            restTemplate.delete(new UriTemplate("http://warehouse-web/v1/reservations/{id}")
                    .with("id", TemplateVariable.VariableType.PATH_VARIABLE).expand(reservationId));
        } catch (RestClientResponseException ex) {
            log.error("Delete reservation failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return true;
    }

    public Reservations findReservationsByOrderId(Long orderId) {
        Reservations result;
        try {
            result = restTemplate
                    .getForObject(new UriTemplate("http://warehouse-web/v1/reservations/search/findReservationsByOrderId")
                            .with("orderId", TemplateVariable.VariableType.REQUEST_PARAM)
                            .expand(orderId), Reservations.class);
        } catch (RestClientResponseException ex) {
            log.error("Find reservations failed", ex);
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
