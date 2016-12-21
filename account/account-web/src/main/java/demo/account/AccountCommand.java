package demo.account;

/**
 * The {@link AccountCommand} represents an action that can be performed to an
 * {@link Account} aggregate. Commands initiate an action that can mutate the state of
 * an account entity as it transitions between {@link AccountStatus} values.
 *
 * @author kbastani
 */
public enum AccountCommand {
    CONFIRM_ACCOUNT,
    ACTIVATE_ACCOUNT,
    SUSPEND_ACCOUNT,
    ARCHIVE_ACCOUNT
}
