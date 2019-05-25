package com.study.spring.bean;

import java.util.Map;

public class BeanConfig {
    /**
     * bean的id
     */
    private String id;
    /**
     * bean的全类名
     */
    private String className;
    /**
     * bean的依赖关系
     */
    private Map<String, String> dependency;

    private BeanConfig(Bulider bulider) {
        this.id = bulider.id;
        this.className = bulider.className;
        this.dependency = bulider.dependecy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Map<String, String> getDependency() {
        return dependency;
    }

    public void setDependency(Map<String, String> dependency) {
        this.dependency = dependency;
    }

    @Override
    public String toString() {
        return "BeanConfig{" +
                "id='" + id + '\'' +
                ", className='" + className + '\'' +
                ", dependency=" + dependency +
                '}';
    }

    public static class Bulider {

        private String id;

        private String className;

        private Map<String, String> dependecy;

        public Bulider setId(String id) {
            this.id = id;
            return this;
        }

        public Bulider setClassName(String className) {
            this.className = className;
            return this;
        }

        public Bulider setDependecy(Map<String, String> dependecy) {
            this.dependecy = dependecy;
            return this;
        }

        public BeanConfig build() {
            return new BeanConfig(this);
        }
    }
}
