package netty.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by gigemjt on 10/19/14.
 */
/**
 * Tags a POJO as being a WebSocket class.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value =
        { ElementType.TYPE })
public @interface NettyWebSocket
{
    int inputBufferSize() default -2;

    int maxBinaryMessageSize() default -2;

    int maxIdleTime() default -2;

    int maxTextMessageSize() default -2;
}
