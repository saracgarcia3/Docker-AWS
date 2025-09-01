package ServidorWeb;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ClasspathScanner {
    public List<Class<?>> findRestControllers() {
        try {
            List<Class<?>> result = new ArrayList<>();
            Enumeration<URL> roots = Thread.currentThread().getContextClassLoader().getResources("");
            while (roots.hasMoreElements()) {
                URL u = roots.nextElement();
                if (!"file".equals(u.getProtocol())) continue;
                File root = new File(u.toURI());
                result.addAll(scanRoot(root, root));
            }
            return result.stream()
                    .filter(c -> c.isAnnotationPresent(RestController.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Scan error", e);
        }
    }

    private List<Class<?>> scanRoot(File root, File dir) {
        List<Class<?>> out = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null) return out;
        for (File f : files) {
            if (f.isDirectory()) out.addAll(scanRoot(root, f));
            else if (f.getName().endsWith(".class")) {
                String fqcn = toClassName(root, f);
                try { out.add(Class.forName(fqcn)); } catch (Throwable ignored) {}
            }
        }
        return out;
    }

    private String toClassName(File root, File cls) {
        String absRoot = root.getAbsolutePath();
        String absCls = cls.getAbsolutePath();
        String rel = absCls.substring(absRoot.length() + 1).replace(File.separatorChar, '.');
        return rel.substring(0, rel.length() - ".class".length());
    }
}
