package cn.langwazi.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import cn.langwazi.annotations.BindView;

@AutoService(Processor.class)
public class HelloProcessor extends AbstractProcessor {

    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<TypeElement, BindingSet> bindingMap = findAndParseTargets(roundEnv);
        for (Map.Entry<TypeElement, BindingSet> entry : bindingMap.entrySet()) {
            //生成代码
            BindingSet binding = entry.getValue();
            JavaFile javaFile = binding.brewJava();
            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private Map<TypeElement, BindingSet> findAndParseTargets(RoundEnvironment roundEnv) {
        Map<TypeElement, BindingSet.Builder> builderMap = new LinkedHashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(BindView.class)) {
            try {
                //解析每一个BindView注解
                parseBindView(element, builderMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //遍历builderMap并开始build
        Deque<Map.Entry<TypeElement, BindingSet.Builder>> entries =
                new ArrayDeque<>(builderMap.entrySet());
        Map<TypeElement, BindingSet> bindingMap = new LinkedHashMap<>();
        while (!entries.isEmpty()) {
            Map.Entry<TypeElement, BindingSet.Builder> entry = entries.removeFirst();
            TypeElement type = entry.getKey();
            BindingSet.Builder builder = entry.getValue();
            bindingMap.put(type, builder.build());
        }

        return bindingMap;
    }

    private void parseBindView(Element element, Map<TypeElement, BindingSet.Builder> builderMap) {
        //获取封装此元素的最里层元素
        //enclosingElement此处可以理解成：用来表示element(某个注解)所在的java类(MainActivity)的元素.
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        //判断是否存已经存在enclosingElement(例如MainActivity)的构建
        BindingSet.Builder builder = builderMap.get(enclosingElement);
        if (builder == null) {
            builder = BindingSet.newBuilder(enclosingElement);
        }

        //获取信息
        TypeMirror elementType = element.asType();

        //获取控件id
        int id = element.getAnnotation(BindView.class).value();

        //名字
        Name simpleName = element.getSimpleName();
        String name = simpleName.toString();
        TypeName type = TypeName.get(elementType);

        //放入id和field
        builder.addField(new Id(id), new FieldViewBinding(name, type));

        builderMap.put(enclosingElement, builder);

    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(BindView.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


}
