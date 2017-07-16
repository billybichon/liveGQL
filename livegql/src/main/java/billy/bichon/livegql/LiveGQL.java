package billy.bichon.livegql;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * Created by billy on 12/07/2017.
 * <p>
 * Implementation of a graphql websocket client using the graph-ql protocol.
 */
@ClientEndpoint(subprotocols = {"graphql-ws"}, encoders = {MessageServer.Encoder.class}, decoders = {MessageClient.Decoder.class})
public class LiveGQL {

    // messsage type
    private static final String GQL_CONNECTION_INIT = "connection_init";
    private static final String GQL_CONNECTION_ACK = "connection_ack";
    private static final String GQL_CONNECTION_ERROR = "connection_error";
    private static final String GQL_CONNECTION_KEEP_ALIVE = "ka";
    private static final String GQL_CONNECTION_TERMINATE = "connection_terminate";
    private static final String GQL_START = "start";
    private static final String GQL_DATA = "data";
    private static final String GQL_ERROR = "error";
    private static final String GQL_COMPLETE = "complete";
    private static final String GQL_STOP = "stop";

    private LiveGQLListener listener;

    // websockets
    private Session session;

    /**
     * @param url      The url of the websocket server.
     * @param listener A {@link LiveGQLListener} which will be used to handle event.
     */
    public LiveGQL(String url, LiveGQLListener listener) {
        this.listener = listener;

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            container.connectToServer(this, URI.create(url));
        } catch (Exception e) {
            this.listener.onError(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialize the connection with the graphql server.
     */
    private void initServer() {
        MessageServer message = new MessageServer(null, null, GQL_CONNECTION_INIT);
        this.sendMessage(message);
    }

    /**
     * Subscribe to an event base on the query
     *
     * @param query The query to subscribe
     * @param tag   The tag associated with this query
     */
    public Subscription subscribe(String query, String tag) {
        MessageServer message = new MessageServer(new PayloadServer(query, null, null), tag, GQL_START);
        this.sendMessage(message);
        return new Subscription(query, tag);
    }

    /**
     * Subscribe to this subscription
     *
     * @param subscription
     */
    public void subscribe(Subscription subscription) {
        this.subscribe(subscription.getQuery(), subscription.getTag());
    }

    /**
     * Subscribe to all the subscriptions
     *
     * @param subscriptions
     */
    public void subscribeAll(Collection<Subscription> subscriptions) {
        for (Subscription subscription : subscriptions) {
            this.subscribe(subscription.getQuery(), subscription.getTag());
        }
    }

    /**
     * Unsubscribe to a subscription by his tag
     *
     * @param tag The tag of the subscription
     */
    public void unsubscribe(String tag) {
        MessageServer message = new MessageServer(null, tag, GQL_STOP);
        this.sendMessage(message);
    }

    /**
     * Unsubscribe to a subscription
     *
     * @param subscription The subscription to unsubscribe
     */
    public void unsubscribe(Subscription subscription) {
        this.unsubscribe(subscription.getTag());
    }

    /**
     * unsubscribe to all the subscriptions contains inside the parameter
     *
     * @param subscriptions The list of subscriptions to unsubscribe
     */
    public void unsubscribeAll(Collection<Subscription> subscriptions) {
        for (Subscription subscription : subscriptions) {
            this.unsubscribe(subscription.getTag());
        }
    }

    /**
     * Close the connection with the graphgl server
     */
    public void closeConnection() {
        MessageServer message = new MessageServer(null, null, GQL_CONNECTION_TERMINATE);
        this.sendMessage(message);
        try {
            if (session != null && session.isOpen())
                session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.session = null;
    }

    @OnOpen
    public void onOpen(Session session) {
        this.listener.onConnectionOpen();
        this.session = session;
        this.initServer();
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        if (closeReason.getCloseCode().getCode() != 1000)
            this.listener.onError(closeReason.getReasonPhrase());
        this.listener.onConnectionClose();
    }

    @OnMessage
    public void onMessage(MessageClient message) {
        switch (message.type) {
            case GQL_CONNECTION_ACK:
                this.listener.onConnectionOpen();
                break;
            case GQL_CONNECTION_ERROR:
                this.listener.onError(message.payload.data.toString());
                break;
            case GQL_DATA:
                this.listener.onMessageReceived(new Gson().toJson(message.payload.data), message.id);
                break;
        }
    }

    @OnError
    public void onError(Throwable e) {
        this.listener.onError(e.toString());
    }

    private void sendMessage(MessageServer message) {
        try {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendObject(message);
            }
        } catch (IOException e) {
            listener.onError(e.getMessage());
            e.printStackTrace();
        } catch (EncodeException e) {
            e.printStackTrace();
        }
    }
}
