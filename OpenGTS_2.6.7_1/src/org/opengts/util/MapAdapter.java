// ----------------------------------------------------------------------------
// Copyright 2007-2020, GeoTelematic Solutions, Inc.
// All rights reserved
// ----------------------------------------------------------------------------
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ----------------------------------------------------------------------------
// Description:
//  Accelerometer information container
// ----------------------------------------------------------------------------
// Change History:
//  2018/09/10  GTS Development Team
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.util;

import java.lang.*;
import java.util.*;
import java.math.*;

/**
*** Map adapter
**/

public abstract class MapAdapter<K,V>
    implements Map<K,V>
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private Set<K> keySet = new HashSet<K>(); // empty

    /**
    *** Constructor
    **/
    public MapAdapter()
    {
        super();
    }

    /**
    *** Constructor
    **/
    public MapAdapter(Set<K> ks)
    {
        this();
        this.keySet = (ks != null)? ks : new HashSet<K>();
    }

    /**
    *** Constructor
    **/
    public MapAdapter(K ks[])
    {
        this(ListTools.toSet(ks, new HashSet<K>()));
    }

    // ------------------------------------------------------------------------
    // Map interface

    /**
    *** Gets a value to which a specified value is mapped
    *** @param key The key whose associated value is to be returned
    *** @return The value to which the specified key is mapped, or
    ***         null if this map contains no mapping for the key
    **/
    public abstract V get(Object key);

    /**
    *** Returns a Set view of the keys contained in this map
    *** @return A Set view of the keys contained in this map
    **/
    public Set<K> keySet()
    {
        return (this.keySet != null)? this.keySet : new HashSet<K>();
    }

    // ------------------------------------------------------------------------

    /**
    *** Removes all of the mappings from this map 
    **/
    public int size()
    {
        return this.keySet().size();
    }

    /**
    *** Returns true if the map is empty
    *** @return Ture if the map is empty
    **/
    public boolean isEmpty()
    {
        return this.keySet().isEmpty();
    }

    /**
    *** Returns true if this map contains a mapping for the specified key
    *** @param key key whose presence in this map is to be tested
    *** @return True if this map contains a mapping for the specified key
    **/
    public boolean containsKey(Object key)
    {
        return this.keySet().contains(key);
    }

    /**
    *** Returns true if this map maps one or more keys to the
    *** specified value
    *** @param value value whose presence in this map is to be tested
    *** @return True if this map maps one or more keys to the
    ***         specified value
    **/
    public boolean containsValue(Object value)
    {
        for (K k : this.keySet()) {
            V v = this.get(k);
            if (v == null) {
                if (value == null) {
                    return true;
                }
            } else
            if (v.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
    *** Returns a Collection view of the values contained in this map
    *** @return A Collection view of the values contained in this map
    **/
    public Collection<V> values()
    {
        Collection<V> values = new Vector<V>();
        for (K k : this.keySet()) {
            V v = this.get(k);
            values.add(v);
        }
        return values;
    }

    /**
    *** Returns a Set view of the mappings contained in this map.
    *** (Note: this call should not be used, but a partial implementation is provided)
    *** @return A Set view of the mappings contained in this map.
    **/
    public Set<Map.Entry<K,V>> entrySet()
    {
        Set<Map.Entry<K,V>> meSet = new HashSet<Map.Entry<K,V>>();
        for (final K k : this.keySet()) {
            meSet.add(new Map.Entry<K,V>() {
                public K getKey() { return k; }
                public V getValue() { return MapAdapter.this.get(k); }
                public V setValue(V value) { throw new UnsupportedOperationException(); }
            });
        }
        return meSet;
    }

    // ------------------------------------------------------------------------

    /**
    *** Associates a specied value with a specified key
    *** @param key key with which the specified value is to be associated
    *** @param value value to be associated with the specified key
    *** @return The previous value associated with <code>key</code>, or null
    **/
    public V put(K key, V value)
    {
        // -- write operations not supported (may be overridden)
        throw new UnsupportedOperationException();
    }
    
    /**
    *** Removes the mapping for a key from this map if it is present
    *** @param key The key whose value will be removed
    *** @return The previous value associated with <code>key</key>, or null
    **/
    public V remove(Object key)
    {
        // -- write operations not supported (may be overridden)
        throw new UnsupportedOperationException();
    }

    /**
    *** Copies all of the mappings from the specified map to this map
    *** @param t map whose mappings will be stored in this map
    **/
    public void putAll(Map<? extends K, ? extends V> t)
    {
        // -- write operations not supported (may be overridden)
        throw new UnsupportedOperationException();
    }

    /**
    *** Removes all of the mappings from this map 
    **/
    public void clear()
    {
        // -- write operations not supported (may be overridden)
        throw new UnsupportedOperationException();
    }

}
