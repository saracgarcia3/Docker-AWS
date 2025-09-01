package ServidorWeb;

public class MicroSpringBoot {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        BeanContainer container = new BeanContainer();
        WebRouter router = new WebRouter();

        if (args.length >= 1) {
            String className = args[0];
            Class<?> c = Class.forName(className);
            Object bean = container.getOrCreate(c);
            router.register(bean);
        } else {
            for (Class<?> c : new ClasspathScanner().findRestControllers()) {
                Object bean = container.getOrCreate(c);
                router.register(bean);
            }
        }

        if (args.length >= 2) {
            try { port = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
        }

        HttpServer.start(port, router);
    }
}
