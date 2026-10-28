package com.campsite.datastructures;
import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class MyLinkedList<T> implements Iterable<T>, Serializable {

    private Node<T> head;   // first node in the list (null if the list is empty)
    private int size;       // how many items are in the list

    // Creates an empty list.
    public MyLinkedList() {
        head = null;
        size = 0;
    }

    // Adds an item to the end of the list.
    public void add(T item) {
        Node<T> newNode = new Node<>(item);

        if (head == null) {
            // List is empty, so the new node becomes the head.
            head = newNode;
        } else {
            // Walk to the last node, then link the new node after it.
            Node<T> current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        size++;
    }

    // Returns the item at the given position (0 = first)
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of range");
        }
        Node<T> current = head;
        // Move forward "index" times.
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }

    // Removes the first occurrence of the given item. Returns true if something was removed.
    public boolean remove(T item) {
        if (head == null) {
            return false;
        }

        // Special case: the item is the head.
        if (head.data.equals(item)) {
            head = head.next;
            size--;
            return true;
        }

        // Otherwise look ahead one node at a time.
        Node<T> current = head;
        while (current.next != null) {
            if (current.next.data.equals(item)) {
                // Skip over the node being removed.
                current.next = current.next.next;
                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    // Returns true if the item is in the list.
    public boolean contains(T item) {
        Node<T> current = head;
        while (current != null) {
            if (current.data.equals(item)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    // Returns the number of items in the list.
    public int size() {
        return size;
    }

    // Returns true if the list has no items.
    public boolean isEmpty() {
        return size == 0;
    }

    // Removes everything from the list.
    public void clear() {
        head = null;
        size = 0;
    }


    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> current = head;  // start at the first node.

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                T data = current.data;
                current = current.next;  // move along to the next node.
                return data;
            }
        };
    }

    // Returns the list as text.
    @Override
    public String toString() {
        String result = "";
        Node<T> current = head;
        while (current != null) {
            result = result + current.data;
            if (current.next != null) {
                result = result + ", ";
            }
            current = current.next;
        }
        return result;
    }


    public static MyLinkedList<String> fromCommaText(String text) {
        MyLinkedList<String> list = new MyLinkedList<>();
        if (text == null) {
            return list;
        }

        String word = "";
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ',') {
                // End of a word, add it if it is not blank.
                if (!word.trim().isEmpty()) {
                    list.add(word.trim());
                }
                word = "";
            } else {
                word = word + c;
            }
        }
        // Add the last word (there is no comma after it)
        if (!word.trim().isEmpty()) {
            list.add(word.trim());
        }
        return list;
    }


    public boolean containsIgnoreCase(String text) {
        Node<T> current = head;
        while (current != null) {
            if (current.data.toString().equalsIgnoreCase(text)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }
}
