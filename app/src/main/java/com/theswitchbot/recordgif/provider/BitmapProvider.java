package com.theswitchbot.recordgif.provider;

import android.graphics.Bitmap;

import com.theswitchbot.recordgif.inter.IProviderExpand;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author wuquan
 */
public class BitmapProvider implements IProviderExpand<Bitmap> {
    private final List<Bitmap> adapter;
    private int index = 0;
    private final Queue<Bitmap> queue;
    public BitmapProvider(List<Bitmap> bitmapList) {
        this.adapter = bitmapList;
        queue = new LinkedList<>();
        queue.addAll(adapter);
    }

    @Override
    public void prepare() {
    }

    @Override
    public void finish() {
    }

    @Override
    public void finishItem(Bitmap item) {
    }

    @Override
    public boolean hasNext() {
        return index < adapter.size();
    }

    @Override
    public int size() {
        return adapter.size();
    }

    @Override
    public Bitmap next() {
        index++;
        return queue.poll();
    }
}
