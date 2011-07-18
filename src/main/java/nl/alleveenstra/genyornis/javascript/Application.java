package nl.alleveenstra.genyornis.javascript;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.LinkedList;
import java.util.Queue;

import nl.alleveenstra.genyornis.Genyornis;

import org.mozilla.javascript.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a JavaScript application.
 *
 * @author alle.veenstra@gmail.com
 */
public class Application extends Thread {

	private static final Logger log = LoggerFactory.getLogger(Application.class);
	private static final int EVALUATE_TIMEOUT = 100;
	private Context cx;
	MyFactory contextFactory = new MyFactory();
	private Scriptable scope;
	private File javascript;
	private Queue<String> messages = new LinkedList<String>();
	private long cpuPerSecond = 0;
	private long lastUptime = 0;
	private long lastThreadCpuTime = 0;
	private boolean running = true;

	public Application(File javascript) {
		this.javascript = javascript;
	}

	/**
	 * Run the JavaScript file.
	 */
	@Override
	public void run() {
		cx = contextFactory.enterContext();
		cx.setOptimizationLevel(-1);
		cx.setMaximumInterpreterStackDepth(24);
		scope = cx.initStandardObjects();

		// make the communication channel available in the scope
		java.lang.Object wrappedPipe = Context.javaToJS(Genyornis.channelManager(), scope);
		ScriptableObject.putProperty(scope, "pipe", wrappedPipe);

		// make this application available in this scope
		java.lang.Object wrappedApplication = Context.javaToJS(this, scope);
		ScriptableObject.putProperty(scope, "application", wrappedApplication);

		try {
			cx.evaluateReader(scope, new FileReader(javascript), javascript.getName(), 0, null);
			while (running) {
				try {
					String message = getMessage();
					cx.evaluateString(scope, message, "<cmd>", 1, null);
					sleep(EVALUATE_TIMEOUT);
				} catch (Exception e) {
					log.error(e.getLocalizedMessage());
				}
			}
		} catch (FileNotFoundException e) {
			log.error(e.getLocalizedMessage());
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		} catch (EvaluatorException e) {
			// Lol exit!
		}
	}

	/**
	 * Read one message from the queue.
	 *
	 * @return a message
	 * @throws InterruptedException
	 */
	public synchronized String getMessage() throws InterruptedException {
		notify();
		while (messages.isEmpty()) {
			wait();
		}
		String message = (String) messages.poll();
		return message;
	}

	/**
	 * Deliver a message to the JavaScript application by calling a function with the message and sender as parameters.
	 *
	 * @param callback
	 * @param from
	 * @param message
	 */
	public synchronized void deliver(String callback, String from, String message) {
		String code = callback + "('" + from.replace("'", "\'") + "','" + message.replace("'", "\'") + "')";
		messages.add(code);
		notify();
	}

	public void updateMemoryUsage() {
		Object[] ids = scope.getIds();
		for (Object id : ids) {
			// scope.get(id)
		}
	}

	public void updateCpuUsage() {
		ThreadMXBean mxThread = ManagementFactory.getThreadMXBean();
		RuntimeMXBean mxRuntime = ManagementFactory.getRuntimeMXBean();
		long threadCpuTime = mxThread.getThreadCpuTime(getId());
		long uptime = mxRuntime.getUptime();
		cpuPerSecond = (threadCpuTime - lastThreadCpuTime) / (uptime - lastUptime);
		lastUptime = uptime;
		lastThreadCpuTime = threadCpuTime;
	}

	public long getCpuPerSecond() {
		return cpuPerSecond;
	}

	public void gracefullyQuit() {
		running = false;
		contextFactory.gracefullyQuit();
	}
}