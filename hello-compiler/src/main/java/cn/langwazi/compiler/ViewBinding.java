package cn.langwazi.compiler;


/**
 * Created by xyin on 2018/2/1.
 * 定义一个需要绑定的view，该view至少有id和field.
 */

final class ViewBinding {

    private final Id id;
    private final FieldViewBinding fieldBinding;

    ViewBinding(Id id, FieldViewBinding fieldBinding) {
        this.id = id;
        this.fieldBinding = fieldBinding;
    }

    FieldViewBinding getFieldBinding() {
        return fieldBinding;
    }

    Id getId() {
        return id;
    }

    static final class Builder {

        private final Id id;
        FieldViewBinding fieldBinding;

        Builder(Id id) {
            this.id = id;
        }

        void setFieldBinding(FieldViewBinding fieldBinding) {
            if (this.fieldBinding != null) {
                throw new AssertionError();
            }
            this.fieldBinding = fieldBinding;
        }

        ViewBinding build() {
            return new ViewBinding(id, fieldBinding);
        }

    }

}
