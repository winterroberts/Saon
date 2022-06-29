package net.winrob.commons.saon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class EventListener {
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	protected @interface EventHandler {
		
	}
	
	@SuppressWarnings("unchecked")
	public final void collectHandlers(Map<Class<? extends Event>, Map<EventListener, Set<Method>>> listeners) {
		Method[] methods = getClass().getDeclaredMethods();
		for (Method method : methods) {
			if (method.getAnnotation((Class<EventHandler>) EventHandler.class) != null
					&& method.getParameterTypes().length == 1
					&& isInvokable(method)) {
				Class<?> e = method.getParameterTypes()[0];
				if (Event.class.isAssignableFrom(e)) {
					if (!listeners.containsKey(e)) {
						listeners.put((Class<? extends Event>) e, new HashMap<>());
					}
					if (!listeners.get(e).containsKey(this)) listeners.get(e).put(this, new HashSet<>());
					listeners.get(e).get(this).add(method);
				}
			}
		}
	}
	
	private static boolean isInvokable(Method m) {
		int mods = m.getModifiers();
		return !Modifier.isAbstract(mods) && !Modifier.isStatic(mods) && (Modifier.isPublic(mods) || Modifier.isProtected(mods));
	}

}
