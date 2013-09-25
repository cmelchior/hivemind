package dk.ilios.hivemind.utils;

import java.util.LinkedList;

public class LimitedBuffer<E> extends LinkedList<E> {
    private int limit;

    public LimitedBuffer(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean add(E e) {
        super.addFirst(e);
        if (size() > limit) {
            super.removeLast();
        }
        return true;
    }
}