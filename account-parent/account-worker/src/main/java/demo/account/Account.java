package demo.account;

import demo.domain.BaseEntity;
import demo.event.AccountEvent;

/**
 * The {@link Account} domain object contains information related to
 * a user's account. The status of an account is event sourced using
 * events logged to the {@link AccountEvent} collection attached to
 * this resource.
 *
 * @author kbastani
 */
public class Account extends BaseEntity {
    private Long userId;
    private String accountNumber;
    private Boolean defaultAccount;
    private AccountStatus status;

    public Account() {
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

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Account{" +
                "userId=" + userId +
                ", accountNumber='" + accountNumber + '\'' +
                ", defaultAccount=" + defaultAccount +
                "} " + super.toString();
    }
}
