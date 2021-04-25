package net.k1ra.FEMBOY_server;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PageParams {
    public JSONObject out = new JSONObject();
    public Map<String, String> params = new HashMap<>();
    public boolean closed = false;
    public String user_IP = "";
}

