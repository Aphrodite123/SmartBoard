package com.aphrodite.smartboard.model.network.inter;

import java.util.List;

public interface IResponseListener {
    void result(String method, List<String> list, Object object);
}
