package com.meekdev.maudio.internal.events;

import com.meekdev.maudio.api.events.AudioEvent;
import com.meekdev.maudio.api.events.AudioListener;
import com.meekdev.maudio.api.events.AudioTrigger;
import com.meekdev.maudio.internal.AudioManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    private final AudioManager audioManager;
    private final JavaPlugin plugin;
    private final Map<String, List<RegisteredListener>> listeners = new ConcurrentHashMap<>();
    private final Map<Class<?>, Set<Method>> cachedMethods = new ConcurrentHashMap<>();

    public EventBus(AudioManager audioManager, JavaPlugin plugin) {
        this.audioManager = audioManager;
        this.plugin = plugin;
    }

    public void registerListeners(Object listener) {
        if (listener == null) return;

        Class<?> clazz = listener.getClass();
        Set<Method> methods = findAnnotatedMethods(clazz);

        for (Method method : methods) {
            registerMethod(listener, method);
        }
    }

    public void unregisterListeners(Object listener) {
        if (listener == null) return;

        for (List<RegisteredListener> listenerList : listeners.values()) {
            listenerList.removeIf(registeredListener -> registeredListener.getOwner() == listener);
        }
    }

    public boolean fireEvent(AudioEvent event) {
        if (event == null || event.isCancelled()) return false;

        String eventName = event.getName();
        if (eventName == null || eventName.isEmpty()) return false;

        List<RegisteredListener> eventListeners = listeners.get(eventName);
        if (eventListeners == null || eventListeners.isEmpty()) return false;

        boolean handled = false;

        for (RegisteredListener registeredListener : eventListeners) {
            if (event.isCancelled() && !registeredListener.isIgnoringCancelled()) {
                continue;
            }

            try {
                registeredListener.execute(event);
                handled = true;
            } catch (Exception e) {
                plugin.getLogger().severe("Error firing audio event: " + e.getMessage());
            }
        }

        return handled;
    }

    private Set<Method> findAnnotatedMethods(Class<?> clazz) {
        return cachedMethods.computeIfAbsent(clazz, cls -> {
            Set<Method> methods = ConcurrentHashMap.newKeySet();

            for (Method method : cls.getDeclaredMethods()) {
                if (method.isAnnotationPresent(AudioListener.class) ||
                        method.isAnnotationPresent(AudioTrigger.class)) {
                    methods.add(method);
                }
            }

            Class<?> superClass = cls.getSuperclass();
            if (superClass != null && !superClass.equals(Object.class)) {
                methods.addAll(findAnnotatedMethods(superClass));
            }

            return methods;
        });
    }

    private void registerMethod(Object instance, Method method) {
        if (method.isAnnotationPresent(AudioListener.class)) {
            registerAudioListener(instance, method);
        } else if (method.isAnnotationPresent(AudioTrigger.class)) {
            registerAudioTrigger(instance, method);
        }
    }

    private void registerAudioListener(Object instance, Method method) {
        AudioListener annotation = method.getAnnotation(AudioListener.class);
        if (annotation == null) return;

        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length != 1 || !AudioEvent.class.isAssignableFrom(paramTypes[0])) {
            plugin.getLogger().warning("Invalid AudioListener method signature: " + method);
            return;
        }

        RegisteredListener registeredListener = new RegisteredListener(
                instance,
                method,
                annotation.priority(),
                annotation.ignoreCancelled()
        );

        for (String eventName : annotation.value()) {
            if (eventName == null || eventName.isEmpty()) continue;

            List<RegisteredListener> eventListeners = listeners.computeIfAbsent(
                    eventName, k -> new CopyOnWriteArrayList<>()
            );

            eventListeners.add(registeredListener);

            eventListeners.sort(Comparator.comparingInt(
                    listener -> listener.getPriority().getValue()
            ));
        }
    }

    private void registerAudioTrigger(Object instance, Method method) {
        AudioTrigger annotation = method.getAnnotation(AudioTrigger.class);
        if (annotation == null) return;

        String eventName = annotation.value();
        if (eventName == null || eventName.isEmpty()) return;

        RegisteredListener registeredListener = new RegisteredListener(
                instance,
                method,
                annotation.priority(),
                annotation.ignoreCancelled()
        );

        List<RegisteredListener> eventListeners = listeners.computeIfAbsent(
                eventName, k -> new CopyOnWriteArrayList<>()
        );

        eventListeners.add(registeredListener);

        eventListeners.sort(Comparator.comparingInt(
                listener -> listener.getPriority().getValue()
        ));
    }

    private static class RegisteredListener {
        private final Object owner;
        private final Method method;
        private final AudioEvent.Priority priority;
        private final boolean ignoreCancelled;

        RegisteredListener(Object owner, Method method, AudioEvent.Priority priority, boolean ignoreCancelled) {
            this.owner = owner;
            this.method = method;
            this.priority = priority;
            this.ignoreCancelled = ignoreCancelled;
            this.method.setAccessible(true);
        }

        Object getOwner() {
            return owner;
        }

        AudioEvent.Priority getPriority() {
            return priority;
        }

        boolean isIgnoringCancelled() {
            return ignoreCancelled;
        }

        void execute(AudioEvent event) throws Exception {
            method.invoke(owner, event);
        }
    }
}