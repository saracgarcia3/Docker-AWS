package ServidorWeb;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request {
    private final String method;
    private final String rawPath;
    private final String path;
    private final Map<String, List<String>> queryParams = new HashMap<>();

    public Request(String method, String rawPath) {
        this.method = method;
        this.rawPath = rawPath != null ? rawPath : "/";
        int q = this.rawPath.indexOf('?');
        if (q >= 0) {
            this.path = this.rawPath.substring(0, q);
            String query = this.rawPath.substring(q + 1);
            parseQuery(query, queryParams);
        } else {
            this.path = this.rawPath;
        }
    }

    private static void parseQuery(String query, Map<String, List<String>> out) {
        if (query == null || query.isEmpty()) return;
        for (String pair : query.split("&")) {
            if (pair.isEmpty()) continue;
            String key, val;
            int eq = pair.indexOf('=');
            if (eq >= 0) {
                key = pair.substring(0, eq);
                val = pair.substring(eq + 1);
            } else {
                key = pair;
                val = "";
            }
            key = decode(key);
            val = decode(val);
            out.computeIfAbsent(key, k -> new ArrayList<>()).add(val);
        }
    }

    private static String decode(String s) {
        String plusFixed = s.replace("+", " ");
        return URLDecoder.decode(plusFixed, StandardCharsets.UTF_8);
    }

    public String getMethod() { return method; }
    public String getRawPath() { return rawPath; }
    public String getPath() { return path; }

    public String getValue(String name) {
        List<String> vals = queryParams.get(name);
        return (vals == null || vals.isEmpty()) ? null : vals.get(0);
    }

    public List<String> getValues(String name) {
        List<String> vals = queryParams.get(name);
        return vals == null ? Collections.emptyList() : Collections.unmodifiableList(vals);
    }
}
