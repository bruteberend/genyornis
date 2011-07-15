package nl.alleveenstra.genyornis.sessions;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

enum AntStatus {
  INITIALIZING,
  COMPILING,
  COPYING,
  JARRING,
  ZIPPING,
  DONE,
  ERROR
}
/**
 * @author alle.veenstra@gmail.com
 */
public class Session {
    private static final Logger log = LoggerFactory.getLogger(Session.class);

	EnumMap<TYPES, Object> values = new EnumMap<TYPES, Object>(TYPES.class);
	public Session() {
		values.put(TYPES.REQUEST_COUNTER, null);
	}

    public static enum TYPES {
        REQUEST_COUNTER
    }

    public <T> T getValue(TYPES type) {
        if (values.containsKey(type))
           return (T) values.get(type);
        return null;
    }

    public void setValues(TYPES type, Object value) {
        this.values.put(type, value);
    }
}
