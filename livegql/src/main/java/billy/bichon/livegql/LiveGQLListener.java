package billy.bichon.livegql;

/**
 * Created by billy on 12/07/2017.
 * <p>
 * Interface definition for a callback to be invoked when a view is clicked.
 */
public interface LiveGQLListener {

    /**
     * Called when the connection has been established
     */
    void onConnectionOpen();

    /**
     * Called when the connection has been cut
     * <p>
     * If an error occur upon closing the connection, an {@link #onError(String)} callback will be fire.
     */
    void onConnectionClose();

    /**
     * Called when a new message arrived.
     *
     * @param message The received message
     */
    void onMessageReceived(String message, String tag);

    /**
     * Called when an error occurred
     */
    void onError(String error);
}
