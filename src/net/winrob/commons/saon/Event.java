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
