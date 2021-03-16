package com.lemick;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.icongmbh.oss.maven.plugin.javassist.ClassTransformer;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import javassist.*;
import javassist.build.JavassistBuildException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.*;

import java.util.*;

public class JacksonSubTypesTransformer extends ClassTransformer {

    public static final String ATTR_DISCRIMINATOR_PROPERTY = "discriminatorProperty";
    public static final String ATTR_DISCRIMINATOR_MAPPING = "discriminatorMapping";

    @Override
    public boolean shouldTransform(final CtClass candidateClass) {
        return candidateClass.hasAnnotation(JsonSubTypes.class) && candidateClass.hasAnnotation(JsonTypeInfo.class);
    }

    @Override
    public void applyTransformations(CtClass classToTransform) throws JavassistBuildException {
        try {
            ConstPool constPool = classToTransform.getClassFile().getConstPool();
            AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) classToTransform.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);

            Map<String, String> subTypes = extractSubTypes(annotationsAttribute);
            String typePropertyName = extractTypePropertyName(annotationsAttribute);

            Annotation schemaAnnotation = new Annotation(Schema.class.getName(), constPool);

            StringMemberValue discriminatorProperty = new StringMemberValue(typePropertyName, constPool);
            schemaAnnotation.addMemberValue(ATTR_DISCRIMINATOR_PROPERTY, discriminatorProperty);

            ArrayMemberValue discriminatorMapping = constructDiscriminatorMappings(constPool, subTypes);
            schemaAnnotation.addMemberValue(ATTR_DISCRIMINATOR_MAPPING, discriminatorMapping);

            annotationsAttribute.addAnnotation(schemaAnnotation);
            classToTransform.getClassFile().addAttribute(annotationsAttribute);
        } catch (Exception e) {
            throw new JavassistBuildException(e);
        }
    }

    private ArrayMemberValue constructDiscriminatorMappings(ConstPool constPool, Map<String, String> subTypes) {
        List<AnnotationMemberValue> discriminatorMappingsList = new ArrayList<>();
        subTypes.forEach((clazz, name) -> {
            AnnotationMemberValue annotationMemberValue = new AnnotationMemberValue(constPool);
            Annotation discriminatorMapping = new Annotation(DiscriminatorMapping.class.getName(), constPool);
            discriminatorMapping.addMemberValue("value", new StringMemberValue(name, constPool));
            discriminatorMapping.addMemberValue("schema", new ClassMemberValue(clazz, constPool));
            annotationMemberValue.setValue(discriminatorMapping);
            discriminatorMappingsList.add(annotationMemberValue);
        });
        ArrayMemberValue discriminatorMapping = new ArrayMemberValue(constPool);
        discriminatorMapping.setValue(discriminatorMappingsList.toArray(AnnotationMemberValue[]::new));
        return discriminatorMapping;
    }

    private String extractTypePropertyName(AnnotationsAttribute annotationsAttribute) {
        Annotation jsonSubTypeInfo = annotationsAttribute.getAnnotation(JsonTypeInfo.class.getName());
        MemberValue memberValue = jsonSubTypeInfo.getMemberValue("property");
        return StringMemberHolder.fetchStringMemberValue(memberValue);
    }

    private Map<String, String> extractSubTypes(AnnotationsAttribute annotationsAttribute) {
        Map<String, String> subtypes = new HashMap<>();
        Annotation jsonSubTypes = annotationsAttribute.getAnnotation(JsonSubTypes.class.getName());
        MemberValue valuesSubTypes = jsonSubTypes.getMemberValue("value");
        valuesSubTypes.accept(new MemberValueVisitorAdapter() {
            @Override
            public void visitArrayMemberValue(ArrayMemberValue arrayMemberValue) {
                for (MemberValue memberValue : arrayMemberValue.getValue()) {
                    memberValue.accept(new MemberValueVisitorAdapter() {
                        @Override
                        public void visitAnnotationMemberValue(AnnotationMemberValue annotationMemberValue) {
                            Annotation subtypeAnnotation = annotationMemberValue.getValue();
                            String clazz = StringMemberHolder.fetchStringMemberValue(subtypeAnnotation.getMemberValue("value"));
                            String name = StringMemberHolder.fetchStringMemberValue(subtypeAnnotation.getMemberValue("name"));
                            subtypes.put(clazz, name);
                        }
                    });
                }
            }
        });
        return subtypes;
    }

    @Override
    public void configure(final Properties properties) {
    }
}