package com.aphrodite.smartboard.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import android.view.ViewGroup;

import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aphrodite on 2018/8/3.
 */
public class HomeViewPagerAdapter extends FragmentStatePagerAdapter {
    private List<BaseFragment> mFragments;

    public HomeViewPagerAdapter(FragmentManager fm) {
        super(fm);
        this.mFragments = new ArrayList<>();
    }

    public void setFragments(List<BaseFragment> fragments) {
        mFragments.clear();
        if (!ObjectUtils.isEmpty(fragments)) {
            mFragments.addAll(fragments);
        }

        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return ObjectUtils.isEmpty(mFragments) ? 0 : mFragments.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return PagerAdapter.POSITION_NONE;
    }



    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //如果注释这行，那么不管怎么切换，page都不会被销毁
        super.destroyItem(container, position, object);
    }
}
