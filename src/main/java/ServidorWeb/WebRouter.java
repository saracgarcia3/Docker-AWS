package ServidorWeb;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class WebRouter {
    public static class Route {
        final Object bean;
        final Method method;
        final String produces;
        Route(Object bean, Method method, String produces) {
            this.bean = bean; this.method = method; this.produces = produces;
        }
    }

    private final Map<String, Route> routes = new HashMap<>();

    public void register(Object bean) {
        for (Method m : bean.getClass().getMethods()) {
            GetMapping gm = m.getAnnotation(GetMapping.class);
            if (gm == null) continue;
            if (m.getReturnType() != String.class) {
                System.out.println("Ignorado (solo String): " + m);
                continue;
            }
            String path = normalize(gm.value());
            routes.put(path, new Route(bean, m, gm.produces()));
            System.out.println("Route: " + path + " -> " + bean.getClass().getSimpleName() + "." + m.getName());
        }
    }

    public Route match(String path) { return routes.get(normalize(path)); }

    public Object invoke(Route r, Request req) throws Exception {
        Parameter[] params = r.method.getParameters();
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            Class<?> pt = params[i].getType();
            if (pt == Request.class) { args[i] = req; continue; }
            RequestParam rp = getAnnotation(params[i], RequestParam.class);
            if (rp != null) {
                String name = rp.value();
                String val = req.getValue(name);
                if (val == null || val.isEmpty()) val = rp.defaultValue();
                args[i] = val; // soportado: String
            } else {
                args[i] = null;
            }
        }
        return r.method.invoke(r.bean, args);
    }

    private static <A extends Annotation> A getAnnotation(Parameter p, Class<A> ann) {
        for (Annotation a : p.getAnnotations()) if (ann.isInstance(a)) return ann.cast(a);
        return null;
    }

    private String normalize(String p) {
        if (p == null || p.isEmpty()) return "/";
        if (!p.startsWith("/")) p = "/" + p;
        if (p.length() > 1 && p.endsWith("/")) p = p.substring(0, p.length() - 1);
        return p;
    }
}
