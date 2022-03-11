package com.zedfalcon.guilds.helpers;

import java.util.Queue;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;

public abstract class Traversal<T> {
    private final Queue<T> toVisit;
    protected final Set<T> visited;
    private boolean terminated;

    public Traversal() {
        this.toVisit = new LinkedList<>();
        this.visited = new HashSet<>();
        this.terminated = false;
    }

    protected abstract Set<T> getSuccessors(T item);
    protected void onEach(T item) {}

    public void addToVisit(T item) {
        toVisit.add(item);
    }

    public void traverse() {
        while (toVisit.size() > 0 && !terminated) {
            T item = toVisit.remove();
            onEach(item);
            toVisit.addAll(getSuccessors(item));
            visited.add(item);
        }
    }

    public void terminate() {
        terminated = true;
    }

    public Set<T> getVisited() {
        return visited;
    }
}
