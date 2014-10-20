package netty.annotation;

import org.reflections.Reflections;

import java.util.Set;

/**
 * Created by gigemjt on 10/19/14.
 */
public class AnnotationLoader {
    public static void main(final String args[]) {
        final Reflections reflections =
                isProduction() ? Reflections.collect() : new Reflections("");

        final Set<Class<?>> annotated =
                reflections.getTypesAnnotatedWith(NettyWebSocket.class);

        System.out.println(annotated);
    }

    public static boolean isProduction() {
        return false;
    }
}
