package com.karhoo.uisdk.screen.rides;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.viewpager.widget.PagerAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class LayoutArrayPagerAdapter extends PagerAdapter {

    private final Page[] pages;

    private List<WeakReference<Refreshable>> refreshables;

    public LayoutArrayPagerAdapter(final Page[] pages) {
        this.pages = pages;
        this.refreshables = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return pages.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Context context = container.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup layout = (ViewGroup) inflater.inflate(pages[position].layout, container, false);
        container.addView(layout);
        if (layout instanceof Refreshable) {
            refreshables.add(new WeakReference<>((Refreshable) layout));
        }
        return layout;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pages[position].title;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public void refresh() {
        for (WeakReference<Refreshable> r : refreshables) {
            if (r.get() != null) {
                r.get().refresh();
            }
        }
    }

    public void setLoader(RidesLoading ridesLoading) {
        for (WeakReference<Refreshable> r : refreshables) {
            if (r.get() != null) {
                r.get().loader(ridesLoading);
            }
        }
    }

    public static class Page {

        private final String title;

        @LayoutRes
        private final int layout;

        public Page(String title, @LayoutRes int layout) {
            this.title = title;
            this.layout = layout;
        }

    }

    public interface Refreshable {
        void refresh();

        void loader(RidesLoading ridesLoading);
    }

}
