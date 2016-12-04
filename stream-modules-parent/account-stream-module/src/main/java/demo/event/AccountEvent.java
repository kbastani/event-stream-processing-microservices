package demo.event;

import demo.account.AccountEventStatus;
import demo.account.AccountEventType;
import demo.domain.BaseEntity;

public class AccountEvent extends BaseEntity {

    private AccountEventType type;
    private AccountEventStatus status;

    public AccountEvent() {
    }

    public AccountEvent(AccountEventType type, AccountEventStatus status) {
        this.type = type;
        this.status = status;
    }

    public AccountEventType getType() {
        return type;
    }

    public void setType(AccountEventType type) {
        this.type = type;
    }

    public AccountEventStatus getStatus() {
        return status;
    }

    public void setStatus(AccountEventStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AccountEvent{" +
                "type=" + type +
                ", status=" + status +
                "} " + super.toString();
    }
}
