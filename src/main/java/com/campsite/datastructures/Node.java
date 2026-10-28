package com.campsite.datastructures;
import java.io.Serializable;

/**
 * One "link" in the linked list.
 * Each node holds one piece of data and a reference to the next node.
 */
public class Node<T> implements Serializable {

    T data;        // the item stored in this node
    Node<T> next;  // the next node in the list (null if this is the last one)

    public Node(T data) {
        this.data = data;
        this.next = null;
    }
}
