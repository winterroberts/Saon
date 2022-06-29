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
				dispatcher.propagate(next);
				if (!next.isCanceled()) next.run();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
