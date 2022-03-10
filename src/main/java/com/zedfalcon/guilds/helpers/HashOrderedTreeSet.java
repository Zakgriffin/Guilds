package com.zedfalcon.guilds.helpers;

import java.util.Comparator;
import java.util.TreeSet;

public class HashOrderedTreeSet<E> extends TreeSet<E> {
    public HashOrderedTreeSet() {
        super(Comparator.comparingInt(Object::hashCode));
    }
}
