package net.winrob.commons.saon;

public class EventWorker implements Runnable {
	
	private EventDispatcher dispatcher;

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
