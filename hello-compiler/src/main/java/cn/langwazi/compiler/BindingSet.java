package cn.langwazi.compiler;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import static com.google.auto.common.MoreElements.getPackage;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Created by xyin on 2018/1/31.
 * 封装生成代码逻辑，一个该对象将会生成一个对应的文件.
 */

final class BindingSet {

    private static final ClassName VIEW = ClassName.get("android.view", "View");
    private static final ClassName UI_THREAD =
            ClassName.get("android.support.annotation", "UiThread");
    private static final String VIEW_TYPE = "android.view.View";


    private final TypeName targetTypeName;
    private final ClassName bindingClassName;
    private final boolean isFinal;

    private final ImmutableList<ViewBinding> viewBindings;

    private BindingSet(TypeName targetTypeName, ClassName bindingClassName, boolean isFinal
            , ImmutableList<ViewBinding> viewBindings) {
        this.isFinal = isFinal;
        this.targetTypeName = targetTypeName;
        this.bindingClassName = bindingClassName;
        this.viewBindings = viewBindings;
    }


    JavaFile brewJava() {
        return JavaFile.builder(bindingClassName.packageName(), createType())
                .addFileComment("Generated code from Hello APT. Do not modify!")
                .build();
    }

    private TypeSpec createType() {
        //创建一个类
        TypeSpec.Builder result = TypeSpec.classBuilder(bindingClassName.simpleName())
                .addModifiers(PUBLIC);
        if (isFinal) {
            result.addModifiers(FINAL);
        }
        //添加一个属性
        result.addField(targetTypeName, "target", PRIVATE);
        //添加一个参数的构造方法
        result.addMethod(createBindingConstructorForActivity());
        //添加两个参数的构造方法
        result.addMethod(createBindingConstructor());

        return result.build();
    }

    private MethodSpec createBindingConstructor() {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addAnnotation(UI_THREAD)
                .addModifiers(PUBLIC);

        constructor.addParameter(targetTypeName, "target");
        constructor.addParameter(VIEW, "source");

        constructor.addStatement("this.target = target");
        constructor.addCode("\n");

        //添加绑定id部分的逻辑代码
        for (ViewBinding binding : viewBindings) {
            addViewBinding(constructor, binding);
        }

        return constructor.build();
    }

    //绑定控件id
    private void addViewBinding(MethodSpec.Builder result, ViewBinding binding) {

        //获取file
        FieldViewBinding fieldBinding = binding.getFieldBinding();

        //设置到一个javapoet CodeBlock
        CodeBlock.Builder builder = CodeBlock.builder()
                .add("target.$L = ", fieldBinding.getName());

        boolean requiresCast = requiresCast(fieldBinding.getType());

        if (!requiresCast) {
            //直接findViewById
            builder.add("source.findViewById($L)", binding.getId().code);
        } else {
            //添加类型强转
            //findViewById
            builder.add("($T)", fieldBinding.getRawType());
            builder.add("source.findViewById($L)", binding.getId().code);
        }
        result.addStatement("$L", builder.build());
    }

    //是否需要转换
    private static boolean requiresCast(TypeName type) {
        return !VIEW_TYPE.equals(type.toString());
    }

    private MethodSpec createBindingConstructorForActivity() {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addAnnotation(UI_THREAD)
                .addModifiers(PUBLIC)
                .addParameter(targetTypeName, "target");
        builder.addStatement("this(target, target.getWindow().getDecorView())");
        return builder.build();
    }

    static Builder newBuilder(TypeElement enclosingElement) {

        //获取元素类的信息
        TypeMirror typeMirror = enclosingElement.asType();
        TypeName targetType = TypeName.get(typeMirror);
        if (targetType instanceof ParameterizedTypeName) {
            targetType = ((ParameterizedTypeName) targetType).rawType;
        }
        String packageName = getPackage(enclosingElement).getQualifiedName().toString();


        String className = enclosingElement.getQualifiedName().toString().substring(
                packageName.length() + 1).replace('.', '$');

        System.out.println("packageName" + packageName);
        System.out.println("className" + className);

        ClassName bindingClassName = ClassName.get(packageName, className + "_HelloBinding");
        boolean isFinal = enclosingElement.getModifiers().contains(Modifier.FINAL);
        return new Builder(targetType, bindingClassName, isFinal);
    }

    static final class Builder {

        private final TypeName targetTypeName;
        private final ClassName bindingClassName;
        private final boolean isFinal;

        private final Map<Id, ViewBinding.Builder> viewIdMap = new LinkedHashMap<>();


        private Builder(TypeName targetTypeName, ClassName bindingClassName, boolean isFinal) {
            this.targetTypeName = targetTypeName;
            this.bindingClassName = bindingClassName;
            this.isFinal = isFinal;
        }

        void addField(Id id, FieldViewBinding binding) {
            getOrCreateViewBindings(id).setFieldBinding(binding);
        }

        private ViewBinding.Builder getOrCreateViewBindings(Id id) {
            ViewBinding.Builder viewId = viewIdMap.get(id);
            if (viewId == null) {
                viewId = new ViewBinding.Builder(id);
                viewIdMap.put(id, viewId);
            }
            return viewId;
        }


        BindingSet build() {
            ImmutableList.Builder<ViewBinding> viewBindings = ImmutableList.builder();
            for (ViewBinding.Builder builder : viewIdMap.values()) {
                viewBindings.add(builder.build());
            }
            return new BindingSet(targetTypeName, bindingClassName, isFinal, viewBindings.build());
        }

    }

}
