package com.theswitchbot.recordgif.inter;

/**
 * 提供数据方
 *
 * @param <T>
 */
public interface IProvider<T> {
    boolean hasNext();

    int size();

    T next();
}
