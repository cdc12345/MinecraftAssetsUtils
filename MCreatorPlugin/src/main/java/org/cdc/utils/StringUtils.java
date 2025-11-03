package org.cdc.utils;

import org.cdc.assets.PluginMain;

import java.util.AbstractMap;
import java.util.Map;

public class StringUtils {
	public static Map.Entry<String,String> getAchievementEntry(String fullPath){
		var path = fullPath.toLowerCase().split("/");
		return new AbstractMap.SimpleEntry<>(path[0], path[path.length - 1]);
	}

	public static String getCodeField(String code){
		var path = code.split("\\.");
		return path[path.length-1];
	}

	public static String getReadableName(String translated,String original){
		return getReadableName(translated,original,false);
	}

	public static String getReadableName(String translated,String original, boolean mustSingle){
		if (isSingleMode() || mustSingle){
			return translated;
		}
		return translated + " - " + original;
	}

	private static boolean isSingleMode(){
		return PluginMain.isSingleLanguageMode();
	}
}
