package com.acme.users.mgt.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.acme.users.mgt.annotations.MetricPoint;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationParameterValueList;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

@RunWith(MockitoJUnitRunner.class)
public class ClassGraphTest {

    @Test
    public void metricsPointTest() {
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo() // Scan classes, methods, fields, annotations
                .enableAnnotationInfo()
                .scan()) { // Start the scan
            for (ClassInfo clazz : scanResult.getClassesWithMethodAnnotation(MetricPoint.class)) {
                System.out.println(">>>>>>>>>>>>>>>>>>> " + clazz.getName());

                clazz.getMethodInfo().forEach(mi -> {
                    AnnotationInfo annotationInfo = mi.getAnnotationInfo(MetricPoint.class);
                    if (annotationInfo != null) {
                        System.out.println("/////////// " + mi.getName()); 
                        AnnotationParameterValueList annotationParameterValueList = annotationInfo.getParameterValues();
                        annotationParameterValueList.forEach(av -> {
                            System.out.println("++++++++++++++++ name=" + av.getName() + "; value=" + av.getValue());
                        });

                    }
                });
            }
        }
    }

}
