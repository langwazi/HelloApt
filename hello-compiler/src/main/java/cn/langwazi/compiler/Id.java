package cn.langwazi.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

/**
 * 定义一个id.
 */
final class Id {

    final int value;
    //javapoet中的代码块
    final CodeBlock code;

    Id(int value) {
        this.value = value;
        this.code = CodeBlock.of("$L", value);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Id && value == ((Id) o).value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Please use value or code explicitly");
    }
}
