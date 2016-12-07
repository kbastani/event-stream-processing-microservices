package demo.account;

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
 */
@Entity
public class Account extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private String accountNumber;
    private Boolean defaultAccount;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<AccountEvent> events = new HashSet<>();

    @Transient
    private AccountEventStatus status;

    public Account() {
        status = AccountEventStatus.ACCOUNT_CREATED;
    }

    public Account(String accountNumber, Boolean defaultAccount, AccountEventStatus status) {
        this.accountNumber = accountNumber;
        this.defaultAccount = defaultAccount;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Boolean getDefaultAccount() {
        return defaultAccount;
    }

    public void setDefaultAccount(Boolean defaultAccount) {
        this.defaultAccount = defaultAccount;
    }

    public Set<AccountEvent> getEvents() {
        return events;
    }

    public void setEvents(Set<AccountEvent> events) {
        this.events = events;
    }

    public AccountEventStatus getStatus() {
        return status;
    }

    public void setStatus(AccountEventStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", userId=" + userId +
                ", accountNumber='" + accountNumber + '\'' +
                ", defaultAccount=" + defaultAccount +
                ", status=" + status +
                "} " + super.toString();
    }
}
