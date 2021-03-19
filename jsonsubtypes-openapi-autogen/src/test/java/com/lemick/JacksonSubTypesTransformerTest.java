package com.lemick;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.build.JavassistBuildException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

public class JacksonSubTypesTransformerTest {

    @InjectMocks
    JacksonSubTypesTransformer model;

    /**
     * Test Classes
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Guitar.class, name = "guitar"),
            @JsonSubTypes.Type(value = Drums.class, name = "drums")
    })
    static class Instrument {
    }

    static class Guitar extends Instrument {
    }

    static class Drums extends Instrument {
    }

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);
    }

    static void initClassPath() {
        ClassPool.getDefault().insertClassPath(new ClassClassPath(Instrument.class));
    }

    @Test
    public void _shouldTransform_candidate() throws NotFoundException {
        initClassPath();
        CtClass candidateClass = ClassPool.getDefault().get(Instrument.class.getName());
        assertTrue(model.shouldTransform(candidateClass));
    }

    @Test
    public void _shouldTransform_not_candidate() throws NotFoundException {
        initClassPath();
        CtClass notCandidateClass = ClassPool.getDefault().get(Guitar.class.getName());
        assertFalse(model.shouldTransform(notCandidateClass));
    }

    @Test
    public void _applyTransformations() throws NotFoundException, JavassistBuildException {
        initClassPath();
        CtClass candidateClass = ClassPool.getDefault().get(Instrument.class.getName());
        model.applyTransformations(candidateClass);

        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) candidateClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        Annotation actualAnnotation = annotationsAttribute.getAnnotation(Schema.class.getName());
        assertNotNull(actualAnnotation, "annotation was added");
        assertNotNull(annotationsAttribute.getAnnotation(JsonSubTypes.class.getName()), "jackson annotations still exist");
        assertNotNull(annotationsAttribute.getAnnotation(JsonTypeInfo.class.getName()), "jackson annotations still exist");

        MemberValue discriminatorProperty = actualAnnotation.getMemberValue(JacksonSubTypesTransformer.ATTR_DISCRIMINATOR_PROPERTY);
        assertEquals("type", StringMemberHolder.fetchStringMemberValue(discriminatorProperty), "annotation property type is present");

        MemberValue discriminatorMapping = actualAnnotation.getMemberValue(JacksonSubTypesTransformer.ATTR_DISCRIMINATOR_MAPPING);
        discriminatorMapping.accept(new MemberValueVisitorAdapter() {
            @Override
            public void visitArrayMemberValue(ArrayMemberValue arrayMemberValue) {
                MemberValue[] memberValues = arrayMemberValue.getValue();
                assertDiscriminatorProperty(memberValues[0], "guitar", "Guitar");
                assertDiscriminatorProperty(memberValues[1], "drums", "Drums");
            }
        });
    }

    void assertDiscriminatorProperty(MemberValue memberValue, String type, String className) {
        memberValue.accept(new MemberValueVisitorAdapter() {
            @Override
            public void visitAnnotationMemberValue(AnnotationMemberValue annotationMemberValue) {
                assertEquals(DiscriminatorMapping.class.getName(), annotationMemberValue.getValue().getTypeName());
                String actualSchema = StringMemberHolder.fetchStringMemberValue(annotationMemberValue.getValue().getMemberValue("schema"));
                assertTrue(actualSchema.contains(className));

                String actualName = StringMemberHolder.fetchStringMemberValue(annotationMemberValue.getValue().getMemberValue("value"));
                assertEquals(type, actualName);
            }
        });

    }
}
