package minesweeper.fairerqueue;

/**
 * THIS CLASS IS FOR THE CHALLENGE PROBLEM, WHICH IS COMPLETELY OPTIONAL.
 * IGNORE IF YOU ARE NOT ATTEMPTING THE CHALLENGE PROBLEM.
 *   
 * FairerQueue is a variant of a queue that attempts to
 * have "more fair" service of queued items when multiple
 * clients can place items on it.
 * 
 * A FairerQueue *must* be thread safe.
 * 
 * IMPORTANT:
 * A compliant implementation will have no O(n) operations,
 * where n is the number of items on the queue.
 * A compliant implementation will use O(n) memory, not O(c)
 * memory, where n is the number of items on the queue and
 * c is the number of (possible) clients.  Assume c>>n.
 */
public interface FairerQueue<E, T> {
    /**
     * Check if the queue has any items.
     * 
     * @return True if the is nonempty, false otherwise.
     */
    public boolean hasElement();
    
    /**
     * Enqueue an item, associating it with the given tag.
     * 
     * The enqueueing behavior is different from a usual queue:
     * Conceptually, each item on the queue is tagged with which client placed the item on the 
     * queue.  A new item, arriving on the queue, checks to see if the item ahead of it belongs 
     * to a client that has an item already enqueued higher up in the queue.  If the item ahead 
     * does belong to a client with an already enqueued item, then the new item "jumps" in front  
     * of the item ahead of it.
     * This process continues until the new item can no longer jump ahead.
     * 
     * @param item the item to enqueue
     * @param tag the client tag for the item
     */
    public void enqueue(E element, T tag);
    
    /**
     * Get the item at the front of the queue.
     * 
     * Requires that the queue is nonempty.
     * 
     * @return the item at the front of the queue.
     */
    public E getItem();
}
