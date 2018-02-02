package cn.langwazi.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

/**
 * 定义一个成员.
 */
final class FieldViewBinding {
    private final String name;
    //该成员属性的类型名
    private final TypeName type;

    FieldViewBinding(String name, TypeName type) {
        this.name = name;
        this.type = type;
    }

    String getName() {
        return name;
    }

    TypeName getType() {
        return type;
    }

    ClassName getRawType() {
        if (type instanceof ParameterizedTypeName) {
            return ((ParameterizedTypeName) type).rawType;
        }
        return (ClassName) type;
    }

}
