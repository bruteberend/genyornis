package nl.alleveenstra.genyornis.channels;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.alleveenstra.genyornis.javascript.Application;

/**
 * This class is responsible for hooking applications to channels. Applications
 * are reached using a JavaScript function (callback).
 * 
 * @author alle.veenstra@gmail.com
 */
public class ApplicationHook extends ChannelHook {

	private static final Logger log = LoggerFactory.getLogger(ApplicationHook.class);
	private static Map<String, ApplicationHook> instances = new HashMap<String, ApplicationHook>();
	Application app;
	String callback;

	private ApplicationHook(Application app, String callback) {
		this.app = app;
		this.callback = callback;
	}

	/**
	 * Produce an application hook, binding an application to a channel.
	 *
	 * @param application
	 * @param callback
	 * @return an application hook
	 */
	public static ApplicationHook produce(Application application, String callback) {
		String key = application.getName().concat("::").concat(callback);
		if (!instances.containsKey(key)) {
			instances.put(key, new ApplicationHook(application, callback));
		}
		return instances.get(key);
	}

	@Override
	public void deliver(String from, String message) {
		app.deliver(this.callback, from, message);
	}
}
