package billy.bichon.livegql;

/**
 * Created by billy on 16/07/2017.
 */
public class Subscription {

    private String query;
    private String tag;

    Subscription(String query, String tag) {
        this.query = query;
        this.tag = tag;
    }

    /**
     * Get the query associate with this subscription
     *
     * @return a query
     */
    public String getQuery() {
        return query;
    }

    /**
     * Return the tag associate to this subscription.
     *
     * @return a tag
     */
    public String getTag() {
        return tag;
    }
}
