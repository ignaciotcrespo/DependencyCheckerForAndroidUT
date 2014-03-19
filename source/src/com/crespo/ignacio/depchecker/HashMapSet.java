package com.crespo.ignacio.depchecker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class HashMapSet<K, V> {

    HashMap<K, Set<V>> map = new HashMap<K, Set<V>>();

    public Set<V> get(final K key) {
        final Set<V> set = map.get(key);
        if (set == null) {
            final Set<V> emptySet = new HashSet<V>();
            map.put(key, emptySet);
            return emptySet;
        }
        return set;
    }

    public boolean add(final K key, final V value) {
        final Set<V> set = get(key);
        return set.add(value);
    }

    public int size() {
        return map.size();
    }

    public Set<K> keySet() {
        return new HashSet<K>(map.keySet());
    }

    public Collection<Set<V>> values() {
        return new ArrayList<Set<V>>(map.values());
    }

}
