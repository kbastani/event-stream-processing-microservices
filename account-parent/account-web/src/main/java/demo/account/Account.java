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
    private Long userId;
    private String accountNumber;
    private Boolean defaultAccount;

    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private Set<AccountEvent> events = new HashSet<>();

    @Enumerated(value = EnumType.STRING)
    private AccountStatus status;

    public Account() {
        status = AccountStatus.ACCOUNT_CREATED;
    }

    public Account(String accountNumber, Boolean defaultAccount, AccountStatus status) {
        this.accountNumber = accountNumber;
        this.defaultAccount = defaultAccount;
        this.status = status;
    }

    @JsonIgnore
    public Long getAccountId() {
        return id;
    }

    public void setAccountId(Long id) {
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
                ", userId=" + userId +
                ", accountNumber='" + accountNumber + '\'' +
                ", defaultAccount=" + defaultAccount +
                ", status=" + status +
                "} " + super.toString();
    }
}
