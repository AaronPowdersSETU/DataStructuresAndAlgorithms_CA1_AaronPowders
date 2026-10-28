package com.campsite.datastructures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class MyLinkedListTest {

    private MyLinkedList<String> list;

    // Runs before every test, so each test starts with a fresh empty list.
    @BeforeEach
    void setUp() {
        list = new MyLinkedList<>();
    }

    @Test
    void newListIsEmpty() {
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    @Test
    void addPutsItemsInOrder() {
        list.add("A");
        list.add("B");
        list.add("C");
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("C", list.get(2));
    }

    @Test
    void removeWorksForHeadMiddleAndMissingItems() {
        list.add("A");
        list.add("B");
        list.add("C");

        assertTrue(list.remove("A"));      // head
        assertEquals("B", list.get(0));
        assertTrue(list.remove("C"));      // end
        assertFalse(list.remove("Z"));     // not in the list
        assertEquals(1, list.size());
    }

    @Test
    void getWithBadIndexThrowsException() {
        list.add("A");
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(5));
    }

    @Test
    void forEachLoopVisitsEveryItem() {
        list.add("A");
        list.add("B");
        String joined = "";
        for (String s : list) {
            joined += s;
        }
        assertEquals("AB", joined);
    }

    @Test
    void fromCommaTextSplitsAndTrims() {
        MyLinkedList<String> facilities = MyLinkedList.fromCommaText(" WiFi, Pool ,,Laundry");
        assertEquals(3, facilities.size());
        assertEquals("Pool", facilities.get(1));
        assertTrue(facilities.containsIgnoreCase("wifi"));
    }

    @Test
    void clearEmptiesTheList() {
        list.add("A");
        list.clear();
        assertTrue(list.isEmpty());
    }
}
