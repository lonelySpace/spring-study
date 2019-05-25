package com.study.spring.bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义的beanFactory，用来模拟spring解决循环依赖
 */
public class CustomBeanFactory implements BeanFactory {
    /**
     * 缓存bean的配置信息
     */
    private static final Map<String, BeanConfig> beanDefinitionCache = new ConcurrentHashMap<String, BeanConfig>(256);

    /**
     * 缓存单例bean
     */
    private static final Map<String, Object> singltonBeanCache = new ConcurrentHashMap<String, Object>(256);

    /**
     * 用来解决单例bean的循环依赖的
     */
    private static final Map<String, Object> earlySingltonBeanCache = new ConcurrentHashMap<String, Object>(256);

    /**
     * 正在创建中的beanName
     */
    private static final Set<String> creatingBeanNameSet = new HashSet<String>(256);

    static {
        // 初始化bean的配置信息
        Map<String, String> personDenpendency = new HashMap<String, String>(1);
        // person配置
        personDenpendency.put("car", "car");
        BeanConfig personConfig = new BeanConfig.Bulider()
                .setId("person")
                .setClassName("com.study.spring.bean.Person")
                .setDependecy(personDenpendency)
                .build();
        beanDefinitionCache.put("person", personConfig);
        // car配置
        Map<String, String> carDependency = new HashMap<String, String>(1);
        carDependency.put("person", "person");
        BeanConfig carConfig = new BeanConfig.Bulider()
                .setId("car")
                .setClassName("com.study.spring.bean.Car")
                .setDependecy(carDependency)
                .build();
        beanDefinitionCache.put("car", carConfig);
        // student配置
        Map<String, String> studentDependency = new HashMap<String, String>(1);
        studentDependency.put("teacher", "teacher");
        BeanConfig studenConfig = new BeanConfig.Bulider()
                .setId("student")
                .setClassName("com.study.spring.bean.Student")
                .setDependecy(studentDependency)
                .build();
        beanDefinitionCache.put("student", studenConfig);
        // teacher 配置
        BeanConfig teacherConfig = new BeanConfig.Bulider()
                .setId("teacher")
                .setClassName("com.study.spring.bean.Teacher")
                .build();
        beanDefinitionCache.put("teacher", teacherConfig);
    }

    /**
     * 就简单实现下这个方法了
     *
     * @param name
     * @return
     * @throws BeansException
     */
    public Object getBean(String name) throws BeansException {
        return doGetBean(name);
    }

    public <T> T getBean(String s, Class<T> aClass) throws BeansException {
        return null;
    }

    public Object getBean(String s, Object... objects) throws BeansException {
        return null;
    }

    public <T> T getBean(Class<T> aClass) throws BeansException {
        return null;
    }

    public <T> T getBean(Class<T> aClass, Object... objects) throws BeansException {
        return null;
    }

    public <T> ObjectProvider<T> getBeanProvider(Class<T> aClass) {
        return null;
    }

    public <T> ObjectProvider<T> getBeanProvider(ResolvableType resolvableType) {
        return null;
    }

    public boolean containsBean(String s) {
        return false;
    }

    public boolean isSingleton(String s) throws NoSuchBeanDefinitionException {
        return false;
    }

    public boolean isPrototype(String s) throws NoSuchBeanDefinitionException {
        return false;
    }

    public boolean isTypeMatch(String s, ResolvableType resolvableType) throws NoSuchBeanDefinitionException {
        return false;
    }

    public boolean isTypeMatch(String s, Class<?> aClass) throws NoSuchBeanDefinitionException {
        return false;
    }

    public Class<?> getType(String s) throws NoSuchBeanDefinitionException {
        return null;
    }

    public String[] getAliases(String s) {
        return new String[0];
    }

    /**
     * 获取bean实例
     *
     * @param name
     * @return
     */
    private Object doGetBean(String name) {
        if (singltonBeanCache.containsKey(name)) {
            return singltonBeanCache.get(name);
        }
        try {
            createBean(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return singltonBeanCache.get(name);
    }

    /**
     * 创建bean实例
     *
     * @param name
     */
    private synchronized void createBean(String name) throws Exception {
        if (creatingBeanNameSet.contains(name)) {
            throw new Exception(name + "正在创建中");
        }
        creatingBeanNameSet.add(name);
        if (!beanDefinitionCache.containsKey(name)) {
            throw new Exception("bean[" + name + "]为定义");
        }
        BeanConfig beanConfig = beanDefinitionCache.get(name);
        Class clazz = Class.forName(beanConfig.getClassName());
        Constructor constructor = clazz.getConstructor();
        // 创建一个空的实例
        Object instance = constructor.newInstance();
        earlySingltonBeanCache.put(beanConfig.getId(), instance);
        // 获取属性实例
        Map<String, String> dependency = beanConfig.getDependency();
        if (dependency != null && !dependency.isEmpty()) {
            for (Map.Entry<String, String> entry : dependency.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                Object attrInstance = earlySingltonBeanCache.remove(value);
                if (attrInstance == null) {
                    attrInstance = getBean(value);
                }
                Method method = clazz.getMethod("set" + upperCaseFirst(key), attrInstance.getClass());
                method.invoke(instance, attrInstance);
            }
        }
        singltonBeanCache.put(beanConfig.getId(), instance);
        creatingBeanNameSet.remove(beanConfig.getId());
        earlySingltonBeanCache.remove(beanConfig.getId());
    }

    private String upperCaseFirst(String str) {
        char[] chars = str.toCharArray();
        chars[0] = (char) (chars[0] - 32);
        return new String(chars);
    }

    public static void main(String[] args) {
        CustomBeanFactory beanFactory = new CustomBeanFactory();
//        Teacher teacher = (Teacher) beanFactory.getBean("teacher");
//        System.out.println(teacher);
//        Student student = (Student) beanFactory.getBean("student");
//        System.out.println(student);
        Person person = (Person) beanFactory.getBean("person");
        System.out.println(person);
        System.out.println(CustomBeanFactory.beanDefinitionCache);
        System.out.println(CustomBeanFactory.creatingBeanNameSet);
        System.out.println(CustomBeanFactory.earlySingltonBeanCache);
        System.out.println(CustomBeanFactory.singltonBeanCache);
    }
}
