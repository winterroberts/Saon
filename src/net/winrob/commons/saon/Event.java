package net.winrob.commons.saon;

public abstract class Event {
	
	private boolean canceled = false;
	private boolean cancelable = false;
	
	public Event() {
		
	}
	
	public Event(boolean cancelable) {
		this.cancelable = cancelable;
	}
	
	public void dispatch(EventDispatcher dispatcher) {
		dispatcher.enqueue(this);
		// StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
		// System.out.println("Dispatching " + (this.getClass().isAnonymousClass() ? this.getClass().getSuperclass().getSimpleName() : this.getClass().getSimpleName()) + " spawned by " + trace.getFileName() + ":" + trace.getLineNumber());
	}
	
	public void dispatchImmediately(EventDispatcher dispatcher) {
		dispatcher.propagate(this);
		if (!isCanceled()) run();
	}
	
	protected abstract boolean run();
	
	public boolean cancel() {
		canceled = true;
		return isCanceled();
	}
	
	public boolean isCanceled() {
		return cancelable && canceled;
	}
	
	public boolean isCancelable() {
		return cancelable;
	}

}
