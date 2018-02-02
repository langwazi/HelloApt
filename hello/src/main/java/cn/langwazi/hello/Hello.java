package cn.langwazi.hello;

import android.app.Activity;
import android.support.annotation.CheckResult;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by xyin on 2018/2/1.
 * hello api.
 */

public class Hello {

    public static void bind(Activity target) {
        View sourceView = target.getWindow().getDecorView();
        createBinding(target, sourceView);
    }

    private static void createBinding(Activity target, View sourceView) {
        //获取class
        Class<?> targetClass = target.getClass();
        Constructor<?> constructor = findBindingConstructorForClass(targetClass);
        if (constructor == null) {
            return;
        }

        try {
            //反射实例化
            constructor.newInstance(target, sourceView);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to invoke " + constructor, e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Unable to invoke " + constructor, e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new RuntimeException("Unable to create binding instance.", cause);
        }

    }

    private static final Map<Class<?>, Constructor<?>> BINDINGS = new LinkedHashMap<>();

    @Nullable
    @CheckResult
    @UiThread
    private static Constructor<?> findBindingConstructorForClass(Class<?> cls) {
        Constructor<?> bindingCtor = BINDINGS.get(cls);
        if (bindingCtor != null) {
            return bindingCtor;
        }
        String clsName = cls.getName();
        if (clsName.startsWith("android.") || clsName.startsWith("java.")) {
            //过滤系统相关
            return null;
        }
        try {
            //通过类名获取
            Class<?> bindingClass = cls.getClassLoader().loadClass(clsName + "_HelloBinding");
            //noinspection unchecked
            bindingCtor = bindingClass.getConstructor(cls, View.class);
        } catch (ClassNotFoundException e) {
            bindingCtor = findBindingConstructorForClass(cls.getSuperclass());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to find binding constructor for " + clsName, e);
        }
        BINDINGS.put(cls, bindingCtor);
        return bindingCtor;
    }


}
