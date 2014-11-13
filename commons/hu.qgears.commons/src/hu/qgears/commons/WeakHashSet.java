package hu.qgears.commons;
  
/*
 * Copyright 2004 (C) TJDO.
 * All rights reserved.
 *
 * This software is distributed under the terms of the TJDO License version 1.0.
 * See the terms of the TJDO License in the documentation provided with this software.
 *
 * $Id: WeakHashSet.java,v 1.1 2004/08/09 23:53:35 jackknifebarber Exp $
 */



import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;


/**
 * A <tt>Set</tt> implementation with <em>weak elements</em>.
 * This class implements the <tt>Set</tt> interface, backed by a hash table with
 * weak keys (actually a <tt>WeakHashMap</tt> instance).
 * An element in a <tt>WeakHashSet</tt> will automatically be removed when it
 * is no longer in ordinary use.
 * More precisely, the presence of an element will not prevent it from being
 * discarded by the garbage collector, that is, made finalizable, finalized,
 * and then reclaimed.
 * When a element has been discarded it is effectively removed from the set,
 * so this class behaves somewhat differently than other <tt>Set</tt>
 * implementations.
 * <p>
 * The null element is supported.
 * This class has performance characteristics similar to those of the
 * <tt>HashSet</tt> class, and has the same efficiency parameters of
 * <em>initial capacity</em> and <em>load factor</em>.
 * <p>
 * Like most collection classes, this class is not synchronized.
 * A synchronized <tt>WeakHashSet</tt> may be constructed using the
 * <tt>Collections.synchronizedSet</tt> method.
 * <p>
 * This class is intended primarily for use with objects whose
 * <tt>equals</tt> methods test for object identity using the
 * <tt>==</tt> operator.
 * Once such an object is discarded it can never be recreated, so it is
 * impossible to do a lookup of that key in a <tt>WeakHashSet</tt> at some later
 * time and be surprised that its entry has been removed.
 * This class will work perfectly well with objects whose <tt>equals</tt>
 * methods are not based upon object identity, such as <tt>String</tt>
 * instances.
 * With such recreatable objects however, the automatic removal of
 * <tt>WeakHashSet</tt> elements that have been discarded may prove to be
 * confusing.
 * <p>
 * The behavior of the <tt>WeakHashSet</tt> class depends in part upon the
 * actions of the garbage collector, so several familiar (though not required)
 * <tt>Set</tt> invariants do not hold for this class.
 * Because the garbage collector may discard elements at any time, a
 * <tt>WeakHashSet</tt> may behave as though an unknown thread is silently
 * removing elements.
 * In particular, even if you synchronize on a <tt>WeakHashSet</tt> instance and
 * invoke none of its mutator methods, it is possible for the <tt>size</tt>
 * method to return smaller values over time, for the <tt>isEmpty</tt> method to
 * return <tt>false</tt> and then <tt>true</tt>, for the <tt>contains</tt>
 * method to return <tt>true</tt> and later <tt>false</tt> for a given object,
 * for the <tt>add</tt> method to return <tt>true</tt> and the <tt>remove</tt>
 * method to return <tt>false</tt> for an element that previously appeared to be
 * in the set, and for successive examinations of the set to yield successively
 * smaller numbers of elements.
 * <p>
 * Each element in a <tt>WeakHashSet</tt> is stored indirectly as the referent
 * of a weak reference.
 * Therefore an element will automatically be removed only after the weak
 * references to it, both inside and outside of the set, have been cleared by
 * the garbage collector.
 * <p>
 * The iterators returned by this class are <i>fail-fast</i>: if the set is
 * structurally modified at any time after the iterator is created, in any way
 * except through the iterator's own <tt>remove</tt> or <tt>add</tt> methods,
 * the iterator will throw a <tt>ConcurrentModificationException</tt>.
 * Thus, in the face of concurrent modification, the iterator fails quickly and
 * cleanly, rather than risking arbitrary, non-deterministic behavior at an
 * undetermined time in the future.
 * <p>
 * Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.
 * Fail-fast iterators throw <tt>ConcurrentModificationException</tt> on a
 * best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness:  <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i>
 *
 *
 * @author  <a href="mailto:mmartin5@austin.rr.com">Mike Martin</a> (borrowing
 *          liberally from java.util.HashSet)
 * @version $Revision: 1.1 $
 */

public class WeakHashSet<E> extends AbstractSet<E> implements Set<E>
{
    /* Dummy value to associate with an Object in the backing Map. */
    private static final Object PRESENT = new Object();

    private final WeakHashMap<E,Object> map;


    /**
     * Constructs a new, empty set; the backing <tt>WeakHashMap</tt> instance has
     * default initial capacity (16) and load factor (0.75).
     */
    public WeakHashSet()
    {
        map = new WeakHashMap<E, Object>();
    }


    /**
     * Constructs a new set containing the elements in the specified
     * collection.  The <tt>WeakHashMap</tt> is created with default load factor
     * (0.75) and an initial capacity sufficient to contain the elements in
     * the specified collection.
     *
     * @param c the collection whose elements are to be placed into this set.
     * @throws NullPointerException   if the specified collection is null.
     */

    public WeakHashSet(Collection<E> c)
    {
        map = new WeakHashMap<E, Object>(Math.max((int)(c.size() / .75f) + 1, 16));
        addAll(c);
    }


    /**
     * Constructs a new, empty set; the backing <tt>WeakHashMap</tt> instance has
     * the specified initial capacity and the specified load factor.
     *
     * @param      initialCapacity   the initial capacity of the hash map.
     * @param      loadFactor        the load factor of the hash map.
     * @throws     IllegalArgumentException if the initial capacity is less
     *             than zero, or if the load factor is nonpositive.
     */

    public WeakHashSet(int initialCapacity, float loadFactor)
    {
        map = new WeakHashMap<E, Object>(initialCapacity, loadFactor);
    }


    /**
     * Constructs a new, empty set; the backing <tt>WeakHashMap</tt> instance has
     * the specified initial capacity and default load factor, which is
     * <tt>0.75</tt>.
     *
     * @param      initialCapacity   the initial capacity of the hash table.
     * @throws     IllegalArgumentException if the initial capacity is less
     *             than zero.
     */

    public WeakHashSet(int initialCapacity)
    {
        map = new WeakHashMap<E, Object>(initialCapacity);
    }


    /**
     * Returns an iterator over the elements in this set.  The elements
     * are returned in no particular order.
     *
     * @return an Iterator over the elements in this set.
     * @see "java.util.ConcurrentModificationException"
     */

    public Iterator<E> iterator()
    {
        return map.keySet().iterator();
    }


    /**
     * Returns the number of elements in this set (its cardinality).
     *
     * @return the number of elements.
     */

    public int size()
    {
        return map.size();
    }


    /**
     * Indicates whether the set is empty.
     *
     * @return <code>true</code> if the set contains no elements.
     */

    public boolean isEmpty()
    {
        return map.isEmpty();
    }


    /**
     * Indicates whether the set contains the specified element.
     *
     * @param o the element to specify.
     * @return <code>true</code> if the set contains the specified element.
     */

    public boolean contains(Object o)
    {
        return map.containsKey(o);
    }


    /**
     * Adds the specified element to the set if it is not already
     * present.
     *
     * @param o the element to be added.
     * @return <code>true</code> if the set did not already contain the specified
     * element.
     */

    public boolean add(E o)
    {
        return map.put(o, PRESENT) == null;
    }


    /**
     * Removes the specified element from the set if it is present.
     *
     * @param o the element to be removed.
     * @return <code>true</code> if the set contained the specified element.
     */

    public boolean remove(Object o)
    {
        return map.remove(o) == PRESENT;
    }


    /**
     * Removes all of the elements from the set.
     */

    public void clear()
    {
        map.clear();
    }
}