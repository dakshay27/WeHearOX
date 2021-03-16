package com.wehear.ox;

import java.util.Comparator;

public class UserPhiComparator implements Comparator<User> {
    @Override
    public int compare(User o1, User o2) {
        int i1 = o1.getPHI();
        int i2 = o2.getPHI();
        return i1 > i2 ? -1
                : i1 < i2 ? 1
                : 0;

    }


}
