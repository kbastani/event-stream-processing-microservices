package demo.account;

/**
 * The {@link AccountStatus} describes the state of an {@link Account}.
 * The aggregate state of a {@link Account} is sourced from attached domain
 * events in the form of {@link demo.event.AccountEvent}.
 *
 * @author kbastani
 */
public enum AccountStatus {
    ACCOUNT_CREATED,
    ACCOUNT_PENDING,
    ACCOUNT_CONFIRMED,
    ACCOUNT_ACTIVE,
    ACCOUNT_SUSPENDED,
    ACCOUNT_ARCHIVED
}
