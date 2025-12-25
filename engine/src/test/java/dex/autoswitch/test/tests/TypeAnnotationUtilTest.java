package dex.autoswitch.test.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Map;

import dex.autoswitch.config.util.TypeAnnotationUtil;
import io.leangen.geantyref.TypeToken;
import org.junit.jupiter.api.Test;

class TypeAnnotationUtilTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    private @interface TestAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    private @interface TestAnnotation2 {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @Repeatable(TestRepeatableHolder.class)
    private @interface TestRepeatable {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    private @interface TestRepeatableHolder {
        TestRepeatable[] value();
    }

    @Test
    void testCopyAnnotationsToCollection() {
        var source = new TypeToken<@TestAnnotation String>() {}.getAnnotatedType();
        var expected = new TypeToken<List<@TestAnnotation String>>() {}.getAnnotatedType();
        var targetToken = new TypeToken<List<String>>() {};

        var result = TypeAnnotationUtil.copyTypeAnnotations(source, targetToken);

        // For Collections, annotations should be merged onto the first type argument
        assertEquals(0, result.getAnnotations().length, "Top level (List) should have no annotations");

        assertInstanceOf(AnnotatedParameterizedType.class, result);
        var typeArgs = ((AnnotatedParameterizedType) result)
                .getAnnotatedActualTypeArguments();

        assertNotNull(typeArgs[0].getAnnotation(TestAnnotation.class), "The String type argument should have the annotation");
        // Use toString as geantry does not fully implement the needed methods for equals to work
        assertEquals(expected.toString(), result.toString(), "The type annotation should be copied to the first type argument");
    }

    @Test
    void testCopyAnnotationsToCollectionWithMerging() {
        var source = new TypeToken<@TestAnnotation String>() {}.getAnnotatedType();
        var expected = new TypeToken<List<@TestAnnotation @TestAnnotation2("meh") String>>() {}.getAnnotatedType();
        var targetToken = new TypeToken<List<@TestAnnotation2("meh") String>>() {};

        var result = TypeAnnotationUtil.copyTypeAnnotations(source, targetToken);

        // For Collections, annotations should be merged onto the first type argument
        assertEquals(0, result.getAnnotations().length, "Top level (List) should have no annotations");

        assertInstanceOf(AnnotatedParameterizedType.class, result);
        var typeArgs = ((java.lang.reflect.AnnotatedParameterizedType) result)
                .getAnnotatedActualTypeArguments();

        assertNotNull(typeArgs[0].getAnnotation(TestAnnotation.class), "The String type argument should have @TestAnnotation");
        assertNotNull(typeArgs[0].getAnnotation(TestAnnotation2.class), "The String type argument should have @TestAnnotation2");
        assertEquals(2, typeArgs[0].getAnnotations().length, "Should have exactly 2 annotations");

        assertEquals(expected.getType(), result.getType(), "The underlying types should match");
    }

    @Test
    void testCopyAnnotationsToNonCollectionParameterizedType() {
        var source = new TypeToken<@TestAnnotation String>() {}.getAnnotatedType();
        var expected = new TypeToken<@TestAnnotation Map<String, String>>() {}.getAnnotatedType();
        // Map is not a Collection
        var targetToken = new TypeToken<java.util.Map<String, String>>() {};

        var result = TypeAnnotationUtil.copyTypeAnnotations(source, targetToken);

        // For non-collections, annotations stay at the top level
        assertNotNull(result.getAnnotation(TestAnnotation.class), "Top level (Map) should have the annotation");

        assertInstanceOf(AnnotatedParameterizedType.class, result);
        var typeArgs = ((java.lang.reflect.AnnotatedParameterizedType) result)
                .getAnnotatedActualTypeArguments();
        assertEquals(0, typeArgs[0].getAnnotations().length, "Key type argument should not be annotated");
        assertEquals(0, typeArgs[1].getAnnotations().length, "Value type argument should not be annotated");
        // Use toString as geantry does not fully implement the needed methods for equals to work
        assertEquals(expected.toString(), result.toString(), "The type annotation should be copied to the top-level Map");
    }

    @Test
    void testCopyAnnotationsToSimpleClass() {
        var source = new TypeToken<@TestAnnotation2("test") String>() {}.getAnnotatedType();
        var expected = new TypeToken<@TestAnnotation2("test") String>() {}.getAnnotatedType();
        var targetToken = TypeToken.get(String.class);

        var result = TypeAnnotationUtil.copyTypeAnnotations(source, targetToken);

        assertNotNull(result.getAnnotation(TestAnnotation2.class), "Simple class should have the annotation");
        assertEquals(String.class, result.getType());
        assertEquals(expected, result, "The type annotation should be copied to the class");
    }

    @Test
    void testCopyAnnotationsToArray() {
        var source = new TypeToken<@TestAnnotation String>() {}.getAnnotatedType();
        var expected = new TypeToken<@TestAnnotation String[]>() {}.getAnnotatedType();
        var targetToken = TypeToken.get(String[].class);

        var result = TypeAnnotationUtil.copyTypeAnnotations(source, targetToken);

        assertNotNull(result.getAnnotation(TestAnnotation.class), "Array type should have the annotation");
        assertEquals(String[].class, result.getType(), "Type should be String[]");
        assertEquals(0, ((java.lang.reflect.AnnotatedArrayType) result).getAnnotatedGenericComponentType().getAnnotations().length, "Component type (String) should not be annotated");
    }

    @Test
    void testNullParameters() {
        var source = new TypeToken<@TestAnnotation String>() {}.getAnnotatedType();
        var token = TypeToken.get(String.class);

        assertThrows(NullPointerException.class, () -> TypeAnnotationUtil.copyTypeAnnotations((AnnotatedType) null, token));
        assertThrows(NullPointerException.class, () -> TypeAnnotationUtil.copyTypeAnnotations(source, null));
    }

    @Test
    void testWildcardArgumentReceivesAnnotationOnWildcard() {
        var source = new TypeToken<@TestAnnotation String>() {}.getAnnotatedType();
        var expected = new TypeToken<List<@TestAnnotation ? extends String>>() {}.getAnnotatedType();
        var targetToken = new TypeToken<List<? extends String>>() {};

        var result = TypeAnnotationUtil.copyTypeAnnotations(source, targetToken);

        assertInstanceOf(AnnotatedParameterizedType.class, result);
        var arg = ((java.lang.reflect.AnnotatedParameterizedType) result)
                .getAnnotatedActualTypeArguments()[0];
        // The implementation attaches the annotation to the wildcard type-usage (the argument),
        // not necessarily to the wildcard's bound; ensure the wildcard argument itself carries the annotation.
        assertInstanceOf(java.lang.reflect.AnnotatedWildcardType.class, arg);
        assertNotNull(arg.getAnnotation(TestAnnotation.class),
                "Wildcard argument should carry the annotation after copy (annotation applied to wildcard usage)");
        // Use toString as geantry does not fully implement the needed methods for equals to work
        assertEquals(expected.toString(), result.toString(), "The type annotation should be copied to the wildcard of List");
    }

    @Test
    void testRepeatableAnnotationsMergeToCollection() {
        var source = new TypeToken<@TestRepeatable("a") @TestRepeatable("b") String>() {}.getAnnotatedType();
        var expected = new TypeToken<List<@TestRepeatable("a") @TestRepeatable("b") String>>() {}.getAnnotatedType();
        var targetToken = new TypeToken<List<String>>() {};

        var result = TypeAnnotationUtil.copyTypeAnnotations(source, targetToken);

        assertInstanceOf(AnnotatedParameterizedType.class, result);
        var arg = ((AnnotatedParameterizedType) result).getAnnotatedActualTypeArguments()[0];
        var repeats = arg.getAnnotationsByType(TestRepeatable.class);
        assertEquals(2, repeats.length, "Should have two repeatable annotations on the String type-usage");
        assertEquals(expected.toString(), result.toString(), "Repeatable annotations should be present on the element type");
    }

    @Test
    void testCopyAnnotationsToArrayOfPrimitives() {
        var source = new TypeToken<@TestAnnotation Integer>() {}.getAnnotatedType();
        var expected = new TypeToken<int @TestAnnotation []>() {}.getAnnotatedType();
        var targetToken = TypeToken.get(int[].class);

        var result = TypeAnnotationUtil.copyTypeAnnotations(source, targetToken);

        assertInstanceOf(AnnotatedArrayType.class, result);
        assertNotNull(result.getAnnotation(TestAnnotation.class), "Top-level primitive array should receive the annotation");
        // Have to hardcode string due to toString differences
        assertEquals("int @dex.autoswitch.test.tests.TypeAnnotationUtilTest.TestAnnotation() []", result.toString(),
                "Annotated primitive array should match expected");
    }

    @Test
    void testWildcardLowerBoundReceivesAnnotationOnWildcard() {
        var source = new TypeToken<@TestAnnotation String>() {}.getAnnotatedType();
        var expected = new TypeToken<List<@TestAnnotation ? super String>>() {}.getAnnotatedType();
        var targetToken = new TypeToken<List<? super String>>() {};

        var result = TypeAnnotationUtil.copyTypeAnnotations(source, targetToken);

        assertInstanceOf(AnnotatedParameterizedType.class, result);
        var arg = ((AnnotatedParameterizedType) result).getAnnotatedActualTypeArguments()[0];
        assertInstanceOf(java.lang.reflect.AnnotatedWildcardType.class, arg);
        assertNotNull(arg.getAnnotation(TestAnnotation.class), "Wildcard lower-bound argument should carry the annotation after copy");
        assertEquals(expected.toString(), result.toString(), "Wildcard lower-bound should match expected annotated form");
    }
}