package demo.payment;

/**
 * The {@link PaymentCommand} represents an action that can be performed to an
 * {@link Payment} aggregate. Commands initiate an action that can mutate the state of
 * an payment entity as it transitions between {@link PaymentStatus} values.
 *
 * @author kbastani
 */
public enum PaymentCommand {
    CONNECT_ORDER,
    PROCESS_PAYMENT
}
