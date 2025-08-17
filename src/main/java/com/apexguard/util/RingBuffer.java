package com.apexguard.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Iterator;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * Thread-safe ring buffer implementation with atomic operations
 * @param <T> The type of elements stored in the buffer
 */
public final class RingBuffer<T> implements Iterable<T> {
    private final int capacity;
    private final AtomicInteger head;
    private final AtomicInteger tail;
    private final AtomicInteger size;
    private final AtomicReferenceArray<T> buffer;
    
    public RingBuffer(int capacity) {
        this.capacity = capacity;
        this.head = new AtomicInteger(0);
        this.tail = new AtomicInteger(0);
        this.size = new AtomicInteger(0);
        this.buffer = new AtomicReferenceArray<>(capacity);
    }
    
    /**
     * Adds an element to the buffer, overwriting the oldest element if full
     * @param element The element to add
     * @return true if the element was added successfully
     */
    public boolean offer(T element) {
        if (element == null) return false;
        
        int currentTail = tail.get();
        int nextTail = (currentTail + 1) % capacity;
        
        // If buffer is full, advance head
        if (size.get() >= capacity) {
            head.getAndIncrement();
            if (head.get() >= capacity) {
                head.set(0);
            }
        } else {
            size.incrementAndGet();
        }
        
        buffer.set(currentTail, element);
        tail.set(nextTail);
        return true;
    }
    
    /**
     * Adds an element to the buffer, waiting if necessary for space
     * @param element The element to add
     * @return true if the element was added successfully
     */
    public boolean add(T element) {
        while (!offer(element)) {
            // Wait for space to become available
            Thread.yield();
        }
        return true;
    }
    
    /**
     * Retrieves and removes the head of the buffer
     * @return The head element, or null if the buffer is empty
     */
    public T poll() {
        if (size.get() == 0) return null;
        
        int currentHead = head.get();
        T element = buffer.get(currentHead);
        
        if (element != null) {
            buffer.set(currentHead, null);
            head.set((currentHead + 1) % capacity);
            size.decrementAndGet();
        }
        
        return element;
    }
    
    /**
     * Retrieves but does not remove the head of the buffer
     * @return The head element, or null if the buffer is empty
     */
    public T peek() {
        if (size.get() == 0) return null;
        return buffer.get(head.get());
    }
    
    /**
     * Gets the element at the specified index
     * @param index The index of the element to retrieve
     * @return The element at the specified index, or null if invalid
     */
    public T get(int index) {
        if (index < 0 || index >= size.get()) return null;
        
        int actualIndex = (head.get() + index) % capacity;
        return buffer.get(actualIndex);
    }
    
    /**
     * Sets the element at the specified index
     * @param index The index where to set the element
     * @param element The element to set
     * @return true if the element was set successfully
     */
    public boolean set(int index, T element) {
        if (index < 0 || index >= size.get() || element == null) return false;
        
        int actualIndex = (head.get() + index) % capacity;
        buffer.set(actualIndex, element);
        return true;
    }
    
    /**
     * Returns the current number of elements in the buffer
     * @return The size of the buffer
     */
    public int size() {
        return size.get();
    }
    
    /**
     * Returns the capacity of the buffer
     * @return The capacity of the buffer
     */
    public int capacity() {
        return capacity;
    }
    
    /**
     * Checks if the buffer is empty
     * @return true if the buffer is empty
     */
    public boolean isEmpty() {
        return size.get() == 0;
    }
    
    /**
     * Checks if the buffer is full
     * @return true if the buffer is full
     */
    public boolean isFull() {
        return size.get() >= capacity;
    }
    
    /**
     * Clears all elements from the buffer
     */
    public void clear() {
        for (int i = 0; i < capacity; i++) {
            buffer.set(i, null);
        }
        head.set(0);
        tail.set(0);
        size.set(0);
    }
    
    /**
     * Converts the buffer to an array
     * @return An array containing all elements in the buffer
     */
    @SuppressWarnings("unchecked")
    public T[] toArray() {
        T[] array = (T[]) new Object[size.get()];
        for (int i = 0; i < size.get(); i++) {
            array[i] = get(i);
        }
        return array;
    }
    
    /**
     * Converts the buffer to a list
     * @return A list containing all elements in the buffer
     */
    public List<T> toList() {
        List<T> list = new ArrayList<>(size.get());
        for (int i = 0; i < size.get(); i++) {
            T element = get(i);
            if (element != null) {
                list.add(element);
            }
        }
        return list;
    }
    
    /**
     * Creates a stream from the buffer elements
     * @return A stream of the buffer elements
     */
    public Stream<T> stream() {
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED),
            false
        );
    }
    
    /**
     * Creates a parallel stream from the buffer elements
     * @return A parallel stream of the buffer elements
     */
    public Stream<T> parallelStream() {
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED),
            true
        );
    }
    
    @Override
    public Iterator<T> iterator() {
        return new RingBufferIterator();
    }
    
    private class RingBufferIterator implements Iterator<T> {
        private int currentIndex = 0;
        private final int currentSize = size.get();
        
        @Override
        public boolean hasNext() {
            return currentIndex < currentSize;
        }
        
        @Override
        public T next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            return get(currentIndex++);
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove operation not supported");
        }
    }
    
    /**
     * Adds all elements from a collection to the buffer
     * @param collection The collection to add
     * @return true if all elements were added successfully
     */
    public boolean addAll(Collection<? extends T> collection) {
        boolean allAdded = true;
        for (T element : collection) {
            if (!offer(element)) {
                allAdded = false;
            }
        }
        return allAdded;
    }
    
    /**
     * Checks if the buffer contains the specified element
     * @param element The element to check for
     * @return true if the element is found in the buffer
     */
    public boolean contains(T element) {
        if (element == null) return false;
        
        for (int i = 0; i < size.get(); i++) {
            T current = get(i);
            if (element.equals(current)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the last element in the buffer without removing it
     * @return The last element, or null if the buffer is empty
     */
    public T peekLast() {
        if (size.get() == 0) return null;
        
        int lastIndex = (tail.get() - 1 + capacity) % capacity;
        return buffer.get(lastIndex);
    }
    
    /**
     * Returns the last element in the buffer and removes it
     * @return The last element, or null if the buffer is empty
     */
    public T pollLast() {
        if (size.get() == 0) return null;
        
        int lastIndex = (tail.get() - 1 + capacity) % capacity;
        T element = buffer.get(lastIndex);
        
        if (element != null) {
            buffer.set(lastIndex, null);
            tail.set(lastIndex);
            size.decrementAndGet();
        }
        
        return element;
    }
}