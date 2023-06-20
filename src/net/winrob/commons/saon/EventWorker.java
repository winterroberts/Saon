package net.winrob.commons.saon;

/**
 * Executes new queued {@link Event} work dispatched by a delegating {@link EventDispatcher}.
 * 
 * @author Winter Roberts
 */
public class EventWorker implements Runnable {
	
	private EventDispatcher dispatcher;

	/**
	 * Creates a new event worker.
	 * 
	 * @param dispatcher The {@link EventDispatcher} this worker should use to poll new work.
	 */
	protected EventWorker(EventDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	@Override
	public void run() {
		while (dispatcher.isRunning()) {
			try {
				Event next = dispatcher.getNext();
				if (next == null) continue;
				next.dispatchImmediately(dispatcher);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
