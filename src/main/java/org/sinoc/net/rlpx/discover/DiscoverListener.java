package org.sinoc.net.rlpx.discover;

/**
 * Allows to handle discovered nodes state changes
 */
public interface DiscoverListener {

    /**
     * Invoked whenever a new node appeared which meets criteria specified
     * in the {@link NodeManager#addDiscoverListener} method
     */
    void nodeAppeared(NodeHandler handler);

    /**
     * Invoked whenever a node stops meeting criteria.
     */
    void nodeDisappeared(NodeHandler handler);

    class Adapter implements DiscoverListener {
        public void nodeAppeared(NodeHandler handler) {}
        public void nodeDisappeared(NodeHandler handler) {}
    }
}
