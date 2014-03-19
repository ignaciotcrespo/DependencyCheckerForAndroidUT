package com.crespo.ignacio.depchecker;

import java.util.HashMap;

public class DoubleHashMap<K, V> extends HashMap<K, V> {

    private final HashMap<V, K> mValues = new HashMap<V, K>();

    @Override
    public V put(final K key, final V value) {
        mValues.put(value, key);
        return super.put(key, value);
    }

    public K getKeyFromValue(final V value) {
        return mValues.get(value);
    }

}
