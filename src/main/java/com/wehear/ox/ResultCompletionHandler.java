package com.wehear.ox;

import org.json.JSONObject;

public interface ResultCompletionHandler {
    void onResult(boolean success, JSONObject response, Exception exception);
}
