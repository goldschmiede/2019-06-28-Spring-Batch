package com.anderscore.goldschmiede.springbatch.samples.tool;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.builder.FlowBuilder;

public class DumpAsPlantUml {
    private final PrintStream out = System.out;

    @Test
    void dump() {
        dump(FlowBuilder.TransitionBuilder.class);
    }

    private void dump(Class<?> clazz) {
        if (clazz.isInterface()) {
            out.print("interface");
        } else {
            out.print("class");
        }
        out.print(" ");
        out.print(clazz.getSimpleName());
        out.println(" {");
        dumpPublicMethods(clazz);
        out.println("}");
    }

    private void dumpPublicMethods(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                dumpMethod(method);
            }
        }
    }

    private void dumpMethod(Method method) {
        out.print("    ");
        out.print(method.getName());
        out.print("(");
        boolean first = true;
        for (Parameter parameter : method.getParameters()) {
            if (first) {
                first = false;
            } else {
                out.print(", ");
            }

            out.print(parameter.getName());
            out.print(": ");
            out.print(parameter.getType().getSimpleName());
        }
        out.print(")");
        Class<?> returnType = method.getReturnType();
        if (returnType != Void.TYPE) {
            out.print(": ");
            out.print(returnType.getSimpleName());
        }
        out.println();
    }
}
