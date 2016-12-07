package demo.event;

import demo.domain.BaseEntity;

public class AccountEvent extends BaseEntity {

    private AccountEventType type;

    public AccountEvent() {
    }

    public AccountEvent(AccountEventType type) {
        this.type = type;
    }

    public AccountEventType getType() {
        return type;
    }

    public void setType(AccountEventType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "AccountEvent{" +
                "type=" + type +
                "} " + super.toString();
    }
}
