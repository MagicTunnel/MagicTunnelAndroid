package net.magictunnel.core;

/**
 * Class that represents an Iodine exception.
 * @author Vitaly
 *
 */
public class IodineException extends Exception {

    /** Serial number. */
    private static final long serialVersionUID = -4144280992818031303L;

    /** The resource id of the message. */
    private int mMsgResId;


    /**
     * Creates a new Iodine exception with the specified message id.
     * @param msgResId The message id.
     */
    public IodineException(final int msgResId) {
        mMsgResId = msgResId;
    }

    /**
     * Get the message id.
     * @return The message id of the exception.
     */
    public final int getMessageId() {
        return mMsgResId;
    }
}
