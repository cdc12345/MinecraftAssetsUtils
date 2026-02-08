package org.cdc.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;

public class L10N {

	private static final Logger LOG = LogManager.getLogger("IST L10N");

	private List<Map.Entry<Function<String, Boolean>, Function<String, String>>> sources;
	private Function<String, String> fallback;

	public L10N() {
		sources = new ArrayList<>();
	}

	public void addSource(Function<String, Boolean> check, Function<String, String> getter) {
		sources.add(new AbstractMap.SimpleEntry<>(check, getter));
	}

	public void setFallBack(Function<String, String> fallBack) {
		this.fallback = fallBack;
	}

	public boolean has(String key) {
		return has(key, false);
	}

	public boolean has(String key, boolean log) {
		boolean exist = sources.stream().anyMatch(functionFunctionEntry -> functionFunctionEntry.getKey().apply(key));
		if (log && !exist) {
			LOG.error("{} do not exist", key);
		}
		return exist;
	}

	public String get(String key) throws NoSuchElementException {
		String result = null;
		for (Map.Entry<Function<String, Boolean>, Function<String, String>> source : sources) {
			result = source.getValue().apply(key);
			if (result != null) {
				break;
			}
		}
		return result == null ? fallback.apply(key) : result;
	}
}
