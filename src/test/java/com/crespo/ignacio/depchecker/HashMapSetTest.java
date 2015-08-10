package com.crespo.ignacio.depchecker;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class HashMapSetTest extends TestCase {

    private HashMapSet<String, Integer> mHashMapSet;

    @Override
    @Before
    protected void setUp() throws Exception {
        super.setUp();
        mHashMapSet = new HashMapSet<String, Integer>();
    }

    @Test
    public void testEmptyDefault() {
        assertEquals(0, mHashMapSet.get("anykey").size());
    }

    @Test
    public void testAdd() {
        mHashMapSet.add("key", 5);
        assertEquals(1, mHashMapSet.size());
        assertEquals(5, mHashMapSet.get("key").iterator().next().intValue());
        assertEquals("key", mHashMapSet.keySet().iterator().next());
    }
}
