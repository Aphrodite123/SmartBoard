package com.aphrodite.smartboard.utils;

import com.aphrodite.framework.utils.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

public class DataTransferUtils {
    public static Object[] splitArray(ArrayList<Integer> ary, int subSize) {
        if (ObjectUtils.isEmpty(ary)) {
            return null;
        }

        int count = ary.size() % subSize == 0 ? ary.size() / subSize : ary.size() / subSize + 1;
        List<List<Integer>> subAryList = new ArrayList<List<Integer>>();
        for (int i = 0; i < count; i++) {
            int index = i * subSize;
            List<Integer> list = new ArrayList<Integer>();
            int j = 0;
            while (j < subSize && index < ary.size()) {
                list.add(ary.get(index++));
                j++;
            }
            subAryList.add(list);
        }

        Object[] subAry = new Object[subAryList.size()];
        for (int i = 0; i < subAryList.size(); i++) {
            List<Integer> subList = subAryList.get(i);
            int[] subAryItem = new int[subList.size()];
            for (int j = 0; j < subList.size(); j++) {
                subAryItem[j] = subList.get(j).intValue();
            }
            subAry[i] = subAryItem;
        }
        return subAry;
    }
}
