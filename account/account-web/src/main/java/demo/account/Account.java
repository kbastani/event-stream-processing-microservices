package demo.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import demo.domain.BaseEntity;
import demo.event.AccountEvent;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * The {@link Account} domain object contains information related to
 * a user's account. The status of an account is event sourced using
 * events logged to the {@link AccountEvent} collection attached to
 * this resource.
 *
 * @author kbastani
 */
@Entity
public class Account extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String firstName;
    private String lastName;
    private String email;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<AccountEvent> events = new HashSet<>();

    @Enumerated(value = EnumType.STRING)
    private AccountStatus status;

    public Account() {
        status = AccountStatus.ACCOUNT_CREATED;
    }

    public Account(String firstName, String lastName, String email) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    @JsonIgnore
    public Long getAccountId() {
        return id;
    }

    public void setAccountId(Long id) {
        this.id = id;
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

    @JsonIgnore
    public Set<AccountEvent> getEvents() {
        return events;
    }

    public void setEvents(Set<AccountEvent> events) {
        this.events = events;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", events=" + events +
                ", status=" + status +
                "} " + super.toString();
    }
}
