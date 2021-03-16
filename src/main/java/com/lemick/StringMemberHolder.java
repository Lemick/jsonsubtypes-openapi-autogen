package com.lemick;

import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;

/**
 * Utility visitor giving access to simple String variables
 */
public class StringMemberHolder extends MemberValueVisitorAdapter {

    private String value;

    public StringMemberHolder() {
    }

    public static String fetchStringMemberValue(MemberValue memberValue) {
        StringMemberHolder stringMemberHolder = new StringMemberHolder();
        memberValue.accept(stringMemberHolder);
        return stringMemberHolder.getValue();
    }

    @Override
    public void visitStringMemberValue(StringMemberValue stringMemberValue) {
        value = stringMemberValue.getValue();
    }

    @Override
    public void visitClassMemberValue(ClassMemberValue classMemberValue) {
        value = classMemberValue.getValue();
    }

    public String getValue() {
        return value;
    }
}
