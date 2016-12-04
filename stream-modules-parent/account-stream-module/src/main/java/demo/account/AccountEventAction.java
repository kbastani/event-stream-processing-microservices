package demo.account;

/**
 * A {@link AccountEventAction} describes the type of action
 * that can be performed on a {@link demo.event.AccountEvent}.
 */
public enum AccountEventAction {
    CREATE_ACCOUNT,
    CONFIRM_ACCOUNT,
    ARCHIVE_ACCOUNT,
    SUSPEND_ACCOUNT,
    ACTIVATE_ACCOUNT
}
