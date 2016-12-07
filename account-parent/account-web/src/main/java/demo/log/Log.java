package demo.log;

import demo.account.AccountEventAction;
import demo.domain.BaseEntity;

import javax.persistence.*;

/**
 * The {@link Log} entity is used to track {@link demo.event.AccountEvent} actions.
 */
@Entity
public class Log extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private AccountEventAction action;

    public Log() {
    }

    public Log(AccountEventAction action) {
        this.action = action;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AccountEventAction getAction() {
        return action;
    }

    public void setAction(AccountEventAction action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "Log{" +
                "id=" + id +
                ", action=" + action +
                "} " + super.toString();
    }
}
