package netty.annotation;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by gigemjt on 10/19/14.
 */
public class AnnotationLoader {

    static final Reflections reflections =
            isProduction() ? Reflections.collect() : new Reflections("");

    public static void main(final String args[]) {
        final Reflections reflections =
                isProduction() ? Reflections.collect() : new Reflections("");

        final Set<Class<?>> annotated =
                reflections.getTypesAnnotatedWith(WebSocket.class);

        // http://stackoverflow.com/questions/19813097/is-it-possible-to-use-reflections-maven-to-scan-for-classes-inside-jars-in-web-i
        System.out.println(annotated);
    }

    public static List<Method> getMethodsAnnotatedWith(final Class<?> type, final Class<? extends Annotation> annotation) {
        final List<Method> methods = new ArrayList<Method>();
        Class<?> klass = type;
        while (klass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
            // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
            final List<Method> allMethods = new ArrayList<Method>(Arrays.asList(klass.getDeclaredMethods()));
            for (final Method method : allMethods) {
                if (annotation == null || method.isAnnotationPresent(annotation)) {
                    Annotation annotInstance = method.getAnnotation(annotation);
                    // TODO process annotInstance
                    methods.add(method);
                }
            }
            // move to the upper class in the hierarchy in search for more methods
            klass = klass.getSuperclass();
        }
        return methods;
    }

    public static Method getMethodAnnotatedWith(final Class<?> type, final Class<? extends Annotation> annotation) {
        return getMethodsAnnotatedWith(type, annotation).get(0);
    }

    public static boolean isProduction() {
        return false;
    }
}
