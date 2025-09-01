package ServidorWeb;

public class BeanContainer {
    private final java.util.Map<Class<?>, Object> singletons = new java.util.HashMap<>();

    public <T> T getOrCreate(Class<T> type) {
        try {
            if (!singletons.containsKey(type)) {
                if (!type.isAnnotationPresent(RestController.class))
                    throw new IllegalArgumentException("Falta @RestController: " + type.getName());
                T instance = type.getDeclaredConstructor().newInstance();
                singletons.put(type, instance);
            }
            return type.cast(singletons.get(type));
        } catch (Exception e) {
            throw new RuntimeException("No se pudo instanciar " + type.getName(), e);
        }
    }
}
