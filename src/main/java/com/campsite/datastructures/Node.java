package com.campsite.datastructures;
import java.io.Serializable;


public class Node<T> implements Serializable {

    T data;        // item stored in this node.
    Node<T> next;  // next node in the list (null if the last one)

    public Node(T data) {
        this.data = data;
        this.next = null;
    }
}
