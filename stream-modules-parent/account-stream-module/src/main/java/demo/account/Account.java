package demo.account;

import demo.domain.BaseEntity;

public class Account extends BaseEntity {
    private Long userId;
    private String accountNumber;
    private Boolean defaultAccount;
    private AccountEventStatus status;

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

    public AccountEventStatus getStatus() {
        return status;
    }

    public void setStatus(AccountEventStatus status) {
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
