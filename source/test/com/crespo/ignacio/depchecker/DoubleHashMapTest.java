package com.crespo.ignacio.depchecker;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class DoubleHashMapTest extends TestCase {

    private static final Integer VALUE_ONE = Integer.valueOf(1);
    private static final String KEY_ONE = "one";
    private DoubleHashMap<String, Integer> doubleMap;

    @Override
    @Before
    protected void setUp() throws Exception {
        super.setUp();
        doubleMap = new DoubleHashMap<String, Integer>();
    }

    @Test
    public void testPut() {
        doubleMap.put(KEY_ONE, VALUE_ONE);
        assertEquals(VALUE_ONE, doubleMap.get(KEY_ONE));
        assertEquals(KEY_ONE, doubleMap.getKeyFromValue(VALUE_ONE));
    }
}
