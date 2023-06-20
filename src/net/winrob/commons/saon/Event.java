package net.winrob.commons.saon;

/**
 * Defines an event abstraction. Implementations define event behavior at dispatch from an {@link EventDispatcher}.
 * 
 * @author Winter Roberts
 */
public abstract class Event {
	
	private boolean canceled = false;
	private boolean cancelable = false;
	
	public Event() {
		
	}
	
	/**
	 * @param cancelable Whether or not this event can be canceled during propagation.
	 */
	public Event(boolean cancelable) {
		this.cancelable = cancelable;
	}
	
	/**
	 * Enqueues this event to be dispatched by {@link EventWorker} threads owned by the dispatcher.
	 * 
	 * @param dispatcher The {@link EventDispatcher} that will delegate the work for this event.
	 */
	public void dispatch(EventDispatcher dispatcher) {
		dispatcher.enqueue(this);
	}
	
	/**
	 * Forces this event to be dispatched immediately and without (further) thread delegation.
	 * <p>
	 * WARNING: Chained uses of this method in {@link EventWorker} threads may prevent queued event execution.
	 * 
	 * @param dispatcher The {@link EventDispatcher} that will propagate this event.
	 */
	public void dispatchImmediately(EventDispatcher dispatcher) {
		dispatcher.propagate(this);
		if (!isCanceled()) run();
	}
	
	/**
	 * Runs this event.
	 * @return True if this event executed successfully, false otherwise.
	 */
	protected abstract boolean run();
	
	/**
	 * Attempts to cancel this event.
	 * @return True if this event was canceled by this or an earlier operation, false otherwise.
	 */
	public boolean cancel() {
		canceled = true;
		return isCanceled();
	}
	
	/**
	 * @return True if this event has been canceled, false otherwise.
	 */
	public boolean isCanceled() {
		return cancelable && canceled;
	}
	
	/**
	 * @return True if the propagation of this event can be canceled, false otherwise.
	 */
	public boolean isCancelable() {
		return cancelable;
	}

}
