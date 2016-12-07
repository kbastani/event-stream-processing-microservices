package demo.account;

/**
 * The {@link AccountEventStatus} describes the state of an {@link Account}.
 * The aggregate state of a {@link Account} is sourced from attached domain
 * events in the form of {@link demo.event.AccountEvent}.
 */
public enum AccountEventStatus {
    ACCOUNT_CREATED,
    ACCOUNT_PENDING,
    ACCOUNT_CONFIRMED,
    ACCOUNT_ACTIVE,
    ACCOUNT_SUSPENDED,
    ACCOUNT_ARCHIVED
}
