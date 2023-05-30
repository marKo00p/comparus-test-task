package de.comparus.opensource.longmap;

import org.junit.jupiter.api.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@DisplayName("LongMapImpl Test")
class LongMapImplTest {
private LongMapImpl<String> longMap = new LongMapImpl<>();
    private LongMapImpl.Node<?>[] getInternalTable(LongMapImpl<?> map) throws Exception {
        Field tableField = LongMapImpl.class.getDeclaredField("table");
        tableField.setAccessible(true);
        return (LongMapImpl.Node<?>[]) tableField.get(map);
    }

    @BeforeEach
    void setUp() {
        longMap.put(833,"hashMap");
        longMap.put(33,"linkedList");
        longMap.put(333,"arrayList");
    }

    @AfterEach
    void tearDown() {
        longMap.clear();
    }
    @Test
    @DisplayName("LongMapImpl has a field 'table' which is an array of nodes")
    public void testGetInternalTable() throws Exception {
        LongMapImpl<String> map = new LongMapImpl<>();
        LongMapImpl.Node<?>[] table = getInternalTable(map);
        assertNotNull(table);
    }
    @Test
    @DisplayName("A nested class Node exists")
    void nodeClassExists() {
        List<String> nestedClassList = Arrays.stream(LongMapImpl.class.getDeclaredClasses())
                .map(Class::getSimpleName)
                .collect(Collectors.toList());

        assertAll("Nested classes",
                () -> assertTrue(nestedClassList.contains("Node"))
        );
    }
    @Test
    @DisplayName("A default constructor capacity")
    void  defaultConstructor() throws Exception {
        Constructor<?> initialConstructor = LongMapImpl.class.getDeclaredConstructor();
        initialConstructor.setAccessible(true);
        LongMapImpl<?> longMap = (LongMapImpl<?>) initialConstructor.newInstance();
        LongMapImpl.Node<?>[] table = getInternalTable(longMap);

        assertEquals(16, table.length);
    }

    @Test
    @DisplayName("An additional constructor accepts an initial array size")
    void constructorWithTableCapacity() throws Exception{
        Constructor<?> constructor = LongMapImpl.class.getConstructor(int.class);

        LongMapImpl<?> longMap = (LongMapImpl<?>) constructor.newInstance(32);
        LongMapImpl.Node<?>[] table = getInternalTable(longMap);

        assertEquals(32, table.length);
    }
    @Test
    @DisplayName("Add new value with the exiting key")
    void putAnotherValueWithTheExistingKey(){
        assertEquals(longMap.put(333, "array"), "arrayList");
    }

    @Test
    @DisplayName("An element with same key")
    void putElementWithTheSameKey() {
        assertEquals(longMap.get(833),"hashMap");
        longMap.put(833,"dequeue");
        assertEquals(longMap.get(833),"dequeue");
    }

    @Test
    @DisplayName("An million element")
    void putMillionElements() {
        int millionOfElements = 1_000_000;
        longMap = new LongMapImpl<>(millionOfElements);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < millionOfElements; i++) {
            longMap.put(i, "value");
        }
        long endTime = System.currentTimeMillis();
        System.out.println(longMap.size());
        System.out.println("Time taken: " + (endTime - startTime) + "ms");
    }
    @Test
    @DisplayName("Doesn't exists key")
    void getElementWhenKeyDoesNotExists() {
        Object foundValue = longMap.get(2);
        assertNull(foundValue);
    }
    @Test
    @DisplayName("Get returns a corresponding value by the given key")
    void get() {
        assertEquals("arrayList", longMap.get(333));
    }
    @Test
    @DisplayName("remove element")
    void remove() {
        assertEquals(3, longMap.size());
        longMap.remove(833);
        assertEquals(2, longMap.size());
    }
    @Test
    @DisplayName("returns null if key doesn't exists")
    void removeWhenKeyDoesNotExists() {
        Object result = longMap.remove(233);
        assertNull(result);
    }
    @Test
    @DisplayName("returns false when there are some elements")
    void isEmpty() {
        assertFalse(longMap.isEmpty());
    }
    @Test
    @DisplayName("returns true if it doesn't contain elements")
    void isEmptyWhenThereIsNoElements() {
        longMap.clear();
        assertTrue(longMap.isEmpty());
    }

    @Test
    @DisplayName("returns true if key exists")
    void containsKey() {
        assertTrue(longMap.containsKey(833));
    }
    @Test
    @DisplayName("returns false if key doesn't exists")
    void doesnt_containsKey() {
        assertFalse(longMap.containsKey(3));
    }
    @Test
    @DisplayName("returns true if value exists")
    void containsValue() {
        assertTrue(longMap.containsValue("arrayList"));
    }
    @Test
    @DisplayName("returns false if value doesn't exists")
    void doesnt_containsValue() {
        assertFalse(longMap.containsValue("list"));
    }
    @Test
    @DisplayName("returns null if value doesn't exists")
    void containsValueWhenItDoesNotExist() {
        assertFalse(longMap.containsValue("666"));
    }
    @Test
    @DisplayName("returns all keys")
    void keys() {
        long[] expected = {833, 33, 333};
        assertArrayEquals(expected, longMap.keys());
    }
    @Test
    @DisplayName("returns all values")
    void values() {
        String[] expected = {"hashMap", "linkedList", "arrayList"};
        assertArrayEquals(expected, longMap.values());
    }
    @Test
    @DisplayName("size returns the number of nodes in the table")
    void size() {
        assertEquals(longMap.size(), 3);
    }

    @Test
    @DisplayName("delete all elements")
    void clear() {
        assertEquals(3, longMap.size());
        longMap.clear();
        assertTrue(longMap.isEmpty());
        assertEquals(0, longMap.size());
    }
}
