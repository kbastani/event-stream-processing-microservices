package demo.account.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.UriTemplate;

import java.util.ArrayList;
import java.util.List;

public class Account {
    private Long id;
    private List<AccountEvent> accountEvents = new ArrayList<>();
    private String firstName;
    private String lastName;
    private String email;
    private AccountStatus status;

    public Account() {
    }

    public Account(String firstName, String lastName, String email, AccountStatus status) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.status = status;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    @JsonIgnore
    public List<AccountEvent> getEvents() {
        return accountEvents;
    }

    @JsonProperty("accountId")
    public Long getIdentity() {
        return id;
    }

    public void setIdentity(Long id) {
        this.id = id;
    }

    /**
     * Returns the {@link Link} with a rel of {@link Link#REL_SELF}.
     */
    public Link getId() {
        return new Link(new UriTemplate("http://account-web/v1/accounts/{id}").with("id", TemplateVariable.VariableType
                .PATH_VARIABLE)
                .expand(getIdentity())
                .toString()).withSelfRel();
    }
}
