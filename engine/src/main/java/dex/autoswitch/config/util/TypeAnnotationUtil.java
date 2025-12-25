package dex.autoswitch.config.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;

public final class TypeAnnotationUtil {
    private TypeAnnotationUtil() {}

    public static boolean hasAnnotation(AnnotatedType type, Class<? extends Annotation> annotation) {
        return Arrays.stream(type.getAnnotations()).anyMatch(a -> a.annotationType() == annotation);
    }

    /**
     * Creates a {@link TypeToken} representing {@code destination} with type-use annotations
     * copied from {@code source}.
     *
     * <p>Annotations are <em>merged</em> within the source itself:
     * <ul>
     *   <li>If {@code source} is parameterized, annotations already present on its
     *       type arguments are preserved.</li>
     *   <li>Annotations are never inferred from {@link Class} or {@link TypeToken} because
     *       they do not carry type-use annotation state.</li>
     * </ul>
     *
     * <p>Application rules:
     * <ul>
     *   <li>If {@code destination} represents a {@link Collection}, annotations are applied to the
     *       first type argument.</li>
     *   <li>Otherwise annotations are applied to the top-level type.</li>
     * </ul>
     *
     * @param source source token with type-use annotations to copy from
     * @param destination target token to apply annotations to
     * @return new TypeToken with annotations from source applied to destination's type structure
     */
    @SuppressWarnings("unchecked")
    public static <T> TypeToken<T> copyTypeAnnotations(TypeToken<?> source, TypeToken<T> destination) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(destination, "destination");

        var result = copyTypeAnnotations(source.getAnnotatedType(), destination);
        return (TypeToken<T>) TypeToken.get((Type) result);
    }

    /**
     * Creates an {@link AnnotatedType} representing {@code token} with type-use annotations
     * copied from {@code sourceAnnotations}.
     *
     * <p>Annotations are <em>merged</em> within the source itself:
     * <ul>
     *   <li>If {@code sourceAnnotations} is parameterized, annotations already present on its
     *       type arguments are preserved.</li>
     *   <li>Annotations are never inferred from {@link Class} or {@link TypeToken} because
     *       they do not carry type-use annotation state.</li>
     * </ul>
     *
     * <p>Application rules:
     * <ul>
     *   <li>If {@code token} represents a {@link Collection}, annotations are applied to the
     *       first type argument.</li>
     *   <li>Otherwise annotations are applied to the top-level type.</li>
     * </ul>
     *
     * @param source annotated source type to copy from
     * @param token target token
     * @return annotated representation of {@code token}
     */
    public static AnnotatedType copyTypeAnnotations(AnnotatedType source, TypeToken<?> token) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(token, "token");

        var targetAnnotated = token.getAnnotatedType();
        var target = targetAnnotated.getType();

        // Extract source annotations
        var sourceTop = source.getAnnotations();
        var sourceArg = sourceTop;

        if (source instanceof AnnotatedParameterizedType apt) {
            var args = apt.getAnnotatedActualTypeArguments();
            if (args.length > 0 && args[0].getAnnotations().length > 0) {
                sourceArg = merge(sourceTop, args[0].getAnnotations());
            }
        }

        if (target instanceof ParameterizedType param) {
            var raw = (Class<?>) param.getRawType();
            var argCount = param.getActualTypeArguments().length;

            var argAnnos = new Annotation[argCount][];
            Arrays.fill(argAnnos, new Annotation[0]);

            // If target already has annotations on its arguments, should preserve them
            if (targetAnnotated instanceof AnnotatedParameterizedType apt) {
                var targetArgs = apt.getAnnotatedActualTypeArguments();
                for (int i = 0; i < targetArgs.length; i++) {
                    argAnnos[i] = targetArgs[i].getAnnotations();
                }
            }

            if (Collection.class.isAssignableFrom(raw) && argCount > 0) {
                argAnnos[0] = merge(sourceArg, argAnnos[0]);
                // Preserve existing top-level annotations on the target collection
                var targetTop = targetAnnotated.getAnnotations();
                return TypeFactory.parameterizedAnnotatedType(param, targetTop, argAnnos);
            }
            return TypeFactory.parameterizedAnnotatedType(param, sourceTop, argAnnos);
        }

        if (!(target instanceof Class<?> clazz)) {
            throw new IllegalArgumentException("Unsupported type: " + target);
        }

        return TypeFactory.annotatedClass(clazz, sourceTop);
    }

    private static Annotation[] merge(Annotation[] a, Annotation[] b) {
        Map<Class<? extends Annotation>, Annotation> map = new LinkedHashMap<>();
        for (Annotation x : a) {
            map.put(x.annotationType(), x);
        }
        for (Annotation x : b) {
            map.putIfAbsent(x.annotationType(), x);
        }
        return map.values().toArray(new Annotation[0]);
    }
}
