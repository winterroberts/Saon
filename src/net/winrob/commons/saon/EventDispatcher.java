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

/**
 * Manages {@link EventWorker} threads, accepts and delegates new {@link Event} work.
 * 
 * @author Winter Roberts
 */
public class EventDispatcher {
	
	private Queue<Event> eventQueue;
	private ThreadFactory factory;
	
	private Set<EventListener> listeners;
	private Map<Class<? extends Event>, Map<EventListener, Set<Method>>> listenerMethods; 
	
	private int numThreads;
	
	private boolean running = true;
	
	/**
	 * Creates a default Saon dispatcher. This automatically allocates {@link EventWorker} threads equal to the number of logical processors available to the Java virtual machine. 
	 */
	public EventDispatcher() {
		this("Saon");
	}
	
	/**
	 * Creates a Saon dispatcher with the specified number of {@link EventWorker} threads.
	 * <p>
	 * NOTE: numThreads is only used when 1 < numThreads < 80; Otherwise this is equivalent to the zero-argument {@link #EventDispatcher()} constructor.
	 * 
	 * @param numThreads The number of worker threads this dispatcher should use.
	 */
	public EventDispatcher(int numThreads) {
		this("Saon", numThreads);
	}
	
	/**
	 * Creates a default dispatcher.
	 * 
	 * @param application The name of the dispatching application.
	 */
	public EventDispatcher(String application) {
		this(application, 0);
	}
	
	/**
	 * Creates a dispatcher with the specified number of {@link EventWorker} threads.
	 * <p>
	 * NOTE: numThreads is only used when 1 < numThreads < 0; Otherwise this is equivalent to the {@link #EventDispatcher(String)} constructor.
	 * 
	 * @param application
	 * @param numThreads
	 */
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
	
	/**
	 * Adds a listener to this dispatcher if it has not been.
	 * 
	 * @param listener The {@link EventListener} to be added.
	 */
	public synchronized void addEventListener(EventListener listener) {
		if (listeners.contains(listener)) return;
		listeners.add(listener);
		listener.collectHandlers(listenerMethods);
	}
	
	/**
	 * Propagates an event to all registered {@link EventListener} methods.
	 * 
	 * @param e The {@link Event} to be propagated.
	 */
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
						if (m.getParameterTypes().length == 2) m.invoke(listener, e, this);
						else m.invoke(listener, e);
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
	
	/**
	 * Adds an event to the queue, to be processed by an {@link EventWorker} later.
	 * 
	 * @param e The {@link Event} that should be queued.
	 * @return True if this dispatcher is running (able to queue event), false otherwise.
	 */
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
	
	/**
	 * NOTE: This is a blocking method! It will wait for an event on the queue or until this dispatcher stops running.
	 * 
	 * @return The {@link Event} at the front of the queue, or null if this dispatcher is not running.
	 * @throws InterruptedException If the calling thread is interrupted while waiting for an event.
	 */
	protected Event getNext() throws InterruptedException {
		synchronized (eventQueue) {
			while (running && eventQueue.isEmpty()) eventQueue.wait();
			return running ? eventQueue.poll() : null;
		}
	}
	
	/**
	 * @return The number of {@link EventWorker} threads this dispatcher delegates to.
	 */
	public int getNumThreads() {
		return numThreads;
	}
	
	/**
	 * @return True if this dispatcher is running (accepting and delegating), false otherwise.
	 */
	public boolean isRunning() {
		return running;
	}
	
	/**
	 * Stops this dispatcher (accepting and delegating), forcing all blocking calls to {@link #getNext()} to return null immediately.
	 * @return True if this dispatcher was shutdown (had been running), false otherwise.
	 */
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
	
	/**
	 * Creates new application {@link EventWorker} threads for this dispatcher.
	 * 
	 * @author Winter Roberts
	 */
	protected class SaonThreadFactory implements ThreadFactory {
		
		private int threadCount;
		private String name;
		
		/**
		 * @param name The application name.
		 */
		protected SaonThreadFactory(String name) {
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
