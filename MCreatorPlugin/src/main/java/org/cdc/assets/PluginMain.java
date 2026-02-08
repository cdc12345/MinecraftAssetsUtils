package org.cdc.assets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import net.mcreator.generator.GeneratorWrapper;
import net.mcreator.io.UserFolderManager;
import net.mcreator.minecraft.DataListEntry;
import net.mcreator.minecraft.DataListLoader;
import net.mcreator.plugin.JavaPlugin;
import net.mcreator.plugin.Plugin;
import net.mcreator.plugin.PluginLoader;
import net.mcreator.plugin.events.PreGeneratorsLoadingEvent;
import net.mcreator.plugin.events.WorkspaceSelectorLoadedEvent;
import net.mcreator.plugin.events.workspace.MCreatorLoadedEvent;
import net.mcreator.preferences.PreferencesManager;
import net.mcreator.preferences.entries.BooleanEntry;
import net.mcreator.ui.MCreator;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdc.data.VersionManifest;
import org.cdc.utils.L10N;
import org.cdc.utils.StringUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.cdc.api.TranslatorAPI.translate;

public class PluginMain extends JavaPlugin {
	private final Logger LOG = LogManager.getLogger(PluginMain.class);

	private VersionManifest versionManifest = null;

	private static final BooleanEntry singleLanguageMode = new BooleanEntry("singleLanguageMode", false);

	private static boolean translated = false;

	public PluginMain(Plugin plugin) {
		super(plugin);

		this.addListener(PreGeneratorsLoadingEvent.class, a -> {
			PreferencesManager.PREFERENCES.ui.addPluginEntry("readable", singleLanguageMode);
			PreferencesManager.initNonCore();
		});

		this.addListener(WorkspaceSelectorLoadedEvent.class, a -> {
			try {
				this.versionManifest = new VersionManifest();
			} catch (URISyntaxException | IOException ignored) {
			}
		});
		this.addListener(MCreatorLoadedEvent.class, a -> {
			if (translated) {
				return;
			}
			SwingUtilities.invokeLater(() -> {
				try {
					var languageFileSource = downloadAssets(
							a.getMCreator().getGenerator().getGeneratorMinecraftVersion());
					if (languageFileSource == null) {
						LOG.info("Lang file failed");
						return;
					}
					L10N languageFile = new L10N();
					languageFile.addSource(languageFileSource::containsKey,
							key -> languageFileSource.containsKey(key) ? languageFileSource.get(key).getAsString() : null);
					languageFile.setFallBack(key ->{
						var value = net.mcreator.ui.init.L10N.t(key);
						LOG.info("{} -> {}", key, value);
						return value;
					});
					translate(a.getMCreator(), languageFile);

					//					mappings = "villagerprofessions";
					//					var villagerProfessions = DataListLoader.loadDataList(mappings);
					//					for (DataListEntry entry : villagerProfessions) {
					//						register = entry.getName().toLowerCase();
					//						var key = "entity.minecraft.villager." + register;
					//						if (languageFile.has(key)) {
					//							entry.setReadableName(StringUtils.getReadableName(languageFile.get(key),
					//									entry.getReadableName()));
					//						} else {
					//							LOG.info("Missing {} key: {}", mappings, key);
					//						}
					//					}

					//					mappings = "tabs";
					//					var tabsItemGroups = DataListLoader.loadDataList(mappings);
					//					for (DataListEntry entry : tabsItemGroups) {
					//						register = Arrays.stream(entry.getReadableName().split(" ")).map(net.mcreator.util.StringUtils::uppercaseFirstLetter).collect(
					//								Collectors.joining());
					//						var key = "itemGroup." + net.mcreator.util.StringUtils.lowercaseFirstLetter(register);
					//						if (languageFile.has(key)) {
					//							entry.setReadableName(StringUtils.getReadableName(languageFile.get(key).getAsString(),
					//									entry.getReadableName()));
					//						} else {
					//							LOG.info("Missing {} key: {}", mappings, key);
					//						}
					//					}

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				translated = true;
			});
		});
	}

	private Map<String, JsonElement> downloadAssets(String version) throws IOException {
		var locale = PreferencesManager.PREFERENCES.ui.language.get();
		if (locale.getLanguage().equals("en")) {
			var possibleLocale = Locale.of(System.getProperty("user.language", "en"),
					System.getProperty("user.region", ""));
			if (possibleLocale.getLanguage().equals("en")) {
				LOG.info("ignored language");
				return null;
			} else {
				locale = possibleLocale;
			}
		}
		String name = locale.getLanguage() + "_" + locale.getCountry().toLowerCase();
		LOG.info(name);

		var cacheParent = UserFolderManager.getFileFromUserFolder("cache");
		String suffix = version + "-" + name + ".json";
		var cache = new File(cacheParent, suffix);
		if (versionManifest != null) {
			URL url;
			cache.getParentFile().mkdirs();
			try {
				// 确保语言文件存在
				if (!cache.exists()) {
					url = new URI(versionManifest.getSpecificVersion(version).getClient()
							.getAssetDownloadURL("minecraft/lang/" + name + ".json")).toURL();
					FileUtils.copyURLToFile(url, cache, 10_000, 5_000);
				}
			} catch (IOException e) {
				LOG.error(e);
				var input = PluginLoader.INSTANCE.getResources(Pattern.compile(cache.getName()));
				input.forEach(a -> {
					try {
						FileUtils.copyURLToFile(Objects.requireNonNull(PluginLoader.INSTANCE.getResource(a)), cache);
					} catch (IOException ignored) {
					}
				});
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}

		if (cache.exists()) {
			var gson = new Gson();
			var map = new HashMap<>(gson.fromJson(new FileReader(cache), JsonObject.class).asMap());
			for (File file : Objects.requireNonNull(cacheParent.listFiles())) {
				if (!file.equals(cache) && file.isFile() && file.getName().endsWith(suffix)){
					map.putAll(gson.fromJson(new FileReader(file),JsonObject.class).asMap());
				}
			}
			return map;
		} else {
			LOG.info(cache);
			return null;
		}
	}

	public static boolean isSingleLanguageMode() {
		return singleLanguageMode.get();
	}
}
