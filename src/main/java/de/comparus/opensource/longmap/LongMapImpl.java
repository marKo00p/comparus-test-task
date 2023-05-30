package de.comparus.opensource.longmap;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class LongMapImpl<V> implements LongMap<V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private int size;
    private int capacity;
    private final float loadFactor;
    private Node<V>[] table;
    public LongMapImpl() {
        this(DEFAULT_CAPACITY);
    }
    @SuppressWarnings("unchecked")
    public LongMapImpl(int initialCapacity) {
        verifyCapacity(initialCapacity);
        this.capacity = initialCapacity;
        this.table = (Node<V>[]) new Node[capacity];
        this.loadFactor = DEFAULT_LOAD_FACTOR;
    }
    @SuppressWarnings("unchecked")
    public LongMapImpl(int initialCapacity, float loadFactor) {
        verifyCapacity(initialCapacity);
        this.loadFactor = loadFactor;
        this.table =(Node<V>[])  new Node[capacity];
    }

    private void verifyCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity (table array size) must be positive");
        }
    }
    public V put(long key, V value) {
        int index = hash(key);
        Node<V> node = table[index];
        if (node == null) {
            table[index] = new Node<>(key, value);
            size++;
        } else {
            Optional<Node<V>> existingNode = Stream.iterate(node, Objects::nonNull, n -> n.next)
                    .filter(n -> n.key == key)
                    .findFirst();
            if (existingNode.isPresent()) {
                V prevValue = existingNode.get().getValue();
                existingNode.get().value = value;
                return prevValue;
            } else {
                Optional<Node<V>> optionalLastNode = Stream.iterate(node, Objects::nonNull, n -> n.next)
                        .reduce((a, b) -> b);
                if (optionalLastNode.isPresent()) {
                    Node<V> lastNode = optionalLastNode.get();
                    lastNode.next = new Node<>(key, value);
                    size++;
                }
            }
        }
        resizeIfNeeded();
        return null;
    }


    public V get(long key) {
        Node<V> newNode = table[hash(key)];
        return Stream.iterate(newNode, Objects::nonNull, Node::getNext)
                .filter(node -> node.getKey() == key)
                .map(Node::getValue)
                .findFirst()
                .orElse(null);
    }

    public V remove(long key) {
        int index = hash(key);
        Node<V> node = table[index];
        if (node == null) {
            return null;
        }
        if (node.key == key) {
            table[index] = node.next;
            size--;
            return node.value;
        }
        Node<V> prev = node;
        node = node.next;
        Optional<Node<V>> optional = Stream.iterate(node, Objects::nonNull, Node::getNext)
                .filter(n -> n.key == key)
                .findFirst();
        if (optional.isPresent()) {
            Node<V> found = optional.get();
            prev.setNext(found.getNext());
            size--;
            return found.getValue();
        }
        return null;
    }


    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsKey(long key) {
        int index = hash(key);
        return Arrays.stream(new Node[] {table[index]})
                .anyMatch(node -> node != null && node.getKey() == key);
    }

    public boolean containsValue(V value) {
        return Arrays.stream(table)
                .filter(Objects::nonNull)
                .flatMap(node -> Stream.iterate(node, Objects::nonNull, Node::getNext))
                .anyMatch(node -> node.getValue().equals(value));
    }

    public long[] keys() {
        return Arrays.stream(table)
                .filter(Objects::nonNull)
                .flatMap(node -> Stream.iterate(node, Objects::nonNull, Node::getNext))
                .mapToLong(Node::getKey)
                .toArray();
    }

    @SuppressWarnings("unchecked")
    public V[] values() {
        return Arrays.stream(table)
                .filter(Objects::nonNull)
                .flatMap(node -> Stream.iterate(node, Objects::nonNull, Node::getNext))
                .map(Node::getValue)
                .toArray(size -> (V[]) new Object[size]);
    }

    public long size() {
        return size;
    }

    public void clear() {
        size = 0;
        Arrays.stream(table).forEach(node -> node = null);
    }
    private int hash(long key) {
        return (int) key % capacity;
    }

    private void resizeIfNeeded() {
        float load = (float) size / capacity;
        if (load > loadFactor) {
            capacity *= 2;
            Node<V>[] oldTable = table;
            table =  new Node[capacity];
            size = 0;
            Arrays.stream(oldTable)
                    .filter(Objects::nonNull)
                    .flatMap(n -> Stream.iterate(n, Objects::nonNull, x -> x.next))
                    .forEach(n -> put(n.key, n.value));
        }
    }


    static class Node<V> {
        private final long key;
        private V value;
        private Node<V> next;

        public Node(long key, V value) {
            this.key = key;
            this.value = value;
            this.next = null;
        }

        public long getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public Node<V> getNext() {
            return next;
        }

        public void setNext(Node<V> next) {
            this.next = next;
        }

        @Override
        public String toString() {
            return "key = " + key +
                    " value = " + value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node)) return false;
            Node<?> node = (Node<?>) o;
            return key == node.key && value.equals(node.value) && Objects.equals(next, node.next);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value, next);
        }
    }
}
