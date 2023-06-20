package net.winrob.commons.saon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.Normalizer.Form;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Defines an event listener abstraction. Implementations define listener methods and behaviors.
 * 
 * @author Winter Roberts
 */
public abstract class EventListener {
	
	/**
	 * Used to annotate listener methods. 
	 * <p>
	 * Listener methods accept an {@link Event} derived object and optional {@link EventDispatcher} object; They are of the form #(Event) or #(Event, EventDispatcher).
	 * 
	 * @author Winter Roberts
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	protected @interface EventHandler {
		
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Collects all correctly-formed {@link EventHandler} annotated listener methods defined in this listener into a provided map.
	 * 
	 * @param listeners A map provided by the {@link EventDispatcher} that added this listener.
	 */
	protected final void collectHandlers(Map<Class<? extends Event>, Map<EventListener, Set<Method>>> listeners) {
		Method[] methods = getClass().getDeclaredMethods();
		for (Method method : methods) {
			if (method.getAnnotation((Class<EventHandler>) EventHandler.class) != null
					&& (method.getParameterTypes().length == 1 || (method.getParameterTypes().length == 2 && method.getParameterTypes()[2].isAssignableFrom(EventDispatcher.class)))
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
	
	/**
	 * Checks whether a method can be invoked directly and without bypassing security settings.
	 * <p>
	 * Specifically checks that the method is not abstract, not static, and not private.
	 * 
	 * @param m The method to be checked.
	 * @return True if the method can be invoked, false otherwise.
	 */
	private static boolean isInvokable(Method m) {
		int mods = m.getModifiers();
		return !Modifier.isAbstract(mods) && !Modifier.isStatic(mods) && (Modifier.isPublic(mods) || Modifier.isProtected(mods));
	}

}
