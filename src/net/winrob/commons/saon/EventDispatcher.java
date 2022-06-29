package net.winrob.commons.saon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

public class EventDispatcher {
	
	private Queue<Event> eventQueue;
	private ThreadFactory factory;
	
	private Set<EventListener> listeners;
	private Map<Class<? extends Event>, Map<EventListener, Set<Method>>> listenerMethods; 
	
	private int numThreads;
	
	private boolean running = true;
	
	public EventDispatcher() {
		this("Saon");
	}
	
	public EventDispatcher(int numThreads) {
		this("Saon", numThreads);
	}
	
	public EventDispatcher(String application) {
		this(application, 0);
	}
	
	public EventDispatcher(String application, int numThreads) {
		factory = new SaonThreadFactory(application);
		eventQueue = new LinkedList<>();
		listeners = new HashSet<>();
		listenerMethods = new HashMap<>();
		numThreads = (numThreads < 1 || numThreads > 80) ? Runtime.getRuntime().availableProcessors() : numThreads;
		this.numThreads = numThreads;
		for (int i = 0; i < numThreads; i++) {
			factory.newThread(new EventWorker(this)).start();
		}
	}
	
	public synchronized void addEventListener(EventListener listener) {
		if (listeners.contains(listener)) return;
		listeners.add(listener);
		listener.collectHandlers(listenerMethods);
	}
	
	protected void propagate(Event e) {
		Class<?> propagateClass = e.getClass();
		do {
			Map<EventListener, Set<Method>> methods = listenerMethods.get(propagateClass);
			if (methods == null) continue;
			for (Entry<EventListener, Set<Method>> entry : methods.entrySet()) {
				EventListener listener = entry.getKey();
				if (entry.getValue() == null) continue;
				for (Method m : entry.getValue()) {
					try {
						m.setAccessible(true);
						m.invoke(listener, e);
					} catch (IllegalAccessException e1) {
						e1.printStackTrace();
					} catch (IllegalArgumentException e1) {
						e1.printStackTrace();
					} catch (InvocationTargetException e1) {
						e1.printStackTrace();
					}
				}
			}
		} while ((propagateClass = propagateClass.getSuperclass()) != Event.class);
	}
	
	protected boolean enqueue(Event e) {
		if (running) {
			synchronized (eventQueue) {
				eventQueue.add(e);
				eventQueue.notify();
			}
			return true;
		}
		return false;
	}
	
	protected Event getNext() throws InterruptedException {
		synchronized (eventQueue) {
			while (running && eventQueue.isEmpty()) eventQueue.wait();
			return running ? eventQueue.poll() : null;
		}
	}
	
	public int getNumThreads() {
		return numThreads;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public boolean shutdown() {
		if (running) {
			synchronized (eventQueue) {
				running  = false;
				eventQueue.notifyAll();
			}
			return true;
		}
		return false;
	}
	
	protected class SaonThreadFactory implements ThreadFactory {
		
		private int threadCount;
		private String name;
		
		public SaonThreadFactory(String name) {
			this.name = name;
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			synchronized(this) {
				t.setName(name + "-EventWorker-" + threadCount);
				threadCount++;
			}
			return t;
		}
		
	}

}
