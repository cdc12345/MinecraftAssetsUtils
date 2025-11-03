package org.cdc.assets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdc.data.VersionManifest;
import org.cdc.utils.StringUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

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
				String mappings = "null";
				String register = "null";
				try {
					JsonObject languageFile = downloadAssets(
							a.getMCreator().getGenerator().getGeneratorMinecraftVersion());
					if (languageFile == null) {
						LOG.info("Lang file failed");
						return;
					}
					mappings = "blocksitems";
					var ca = DataListLoader.loadDataList("blocksitems");
					var generatorWrapper = new GeneratorWrapper(a.getMCreator().getGenerator());
					for (DataListEntry entry : ca) {
						register = generatorWrapper.map(entry.getName(), "blocksitems", 1);
						if (languageFile.has("item.minecraft." + register))
							entry.setReadableName(StringUtils.getReadableName(
									languageFile.get("item.minecraft." + register).getAsString(),
									entry.getReadableName(), true));
						else if (languageFile.has("block.minecraft." + register))
							entry.setReadableName(StringUtils.getReadableName(
									languageFile.get("block.minecraft." + register).getAsString(),
									entry.getReadableName(), true));
					}

					mappings = "entities";
					var entites = DataListLoader.loadDataList("entities");
					for (DataListEntry entry : entites) {
						register = generatorWrapper.map(entry.getName(), "entities", 2);
						if (register.equals("zombie") && !entry.getName().equals("EntityZombie")) {
							continue;
						}
						var key = "entity.minecraft." + register;
						if (languageFile.has(key)) {
							entry.setReadableName(StringUtils.getReadableName(languageFile.get(key).getAsString(),
									entry.getReadableName()));
						} else {
							LOG.info("Missing {} key: {}", mappings, key);
						}
					}

					mappings = "biomes";
					var biomes = DataListLoader.loadDataList("biomes");
					for (DataListEntry entry : biomes) {
						register = generatorWrapper.map(entry.getName(), "biomes");
						if (register.equals("plains") && !entry.getName().equals("plains")) {
							continue;
						}
						var key = "biome.minecraft." + register;
						if (languageFile.has(key)) {
							entry.setReadableName(StringUtils.getReadableName(languageFile.get(key).getAsString(),
									entry.getReadableName()));
						} else {
							LOG.info("Missing {} key: {}", mappings, key);
						}
					}

					mappings = "enchantments";
					var enchantments = DataListLoader.loadDataList(mappings);
					for (DataListEntry entry : enchantments) {
						register = generatorWrapper.map(entry.getName(), mappings, 1).toLowerCase();
						if (register.equals("efficiency") && !entry.getName().equals("EFFICIENCY")) {
							continue;
						}
						var key = "enchantment.minecraft." + register;
						if (languageFile.has(key)) {
							entry.setReadableName(StringUtils.getReadableName(languageFile.get(key).getAsString(),
									entry.getReadableName()));
						} else {
							LOG.info("Missing {} key: {}", mappings, key);
						}
					}

					mappings = "achievements";
					var achievements = DataListLoader.loadDataList(mappings);
					for (DataListEntry entry : achievements) {
						var achievementEntry = StringUtils.getAchievementEntry(entry.getName());
						register = achievementEntry.getValue();
						if (register.replaceAll("\\s", "").length() != register.length()) {
							continue;
						}
						var key = "advancements." + achievementEntry.getKey() + "." + register + ".title";
						if (languageFile.has(key)) {
							entry.setReadableName(StringUtils.getReadableName(languageFile.get(key).getAsString(),
									entry.getReadableName()));
						} else {
							LOG.info("Missing {} key: {}", mappings, key);
						}
					}

					mappings = "attributes";
					var attributes = DataListLoader.loadDataList(mappings);
					for (DataListEntry entry : attributes) {
						register = StringUtils.getCodeField(generatorWrapper.map(entry.getName(), mappings))
								.toLowerCase();
						var key = "attribute.name." + register;
						if (languageFile.has(key)) {
							entry.setReadableName(StringUtils.getReadableName(languageFile.get(key).getAsString(),
									entry.getReadableName()));
							continue;
						}
						key = "attribute.name.generic." + register;
						if (languageFile.has(key)) {
							entry.setReadableName(StringUtils.getReadableName(languageFile.get(key).getAsString(),
									entry.getReadableName()));
							continue;
						}
						key = "attribute.name.player." + register;
						if (languageFile.has(key)) {
							entry.setReadableName(StringUtils.getReadableName(languageFile.get(key).getAsString(),
									entry.getReadableName()));
						}
					}

					mappings = "gamemodes";
					var gamemodes = DataListLoader.loadDataList(mappings);
					for (DataListEntry entry : gamemodes) {
						register = entry.getName().toLowerCase();
						var key = "gameMode." + register;
						if (languageFile.has(key)) {
							entry.setReadableName(StringUtils.getReadableName(languageFile.get(key).getAsString(),
									entry.getReadableName()));
						} else {
							LOG.info("Missing {} key: {}", mappings, key);
						}
					}

					mappings = "gamerules";
					var gamerules = DataListLoader.loadDataList(mappings);
					for (DataListEntry entry : gamerules) {
						register = net.mcreator.util.StringUtils.snakeToCamel(entry.getName().toLowerCase());
						var key = "gamerule." + net.mcreator.util.StringUtils.lowercaseFirstLetter(register);
						if (languageFile.has(key)) {
							entry.setReadableName(StringUtils.getReadableName(languageFile.get(key).getAsString(),
									entry.getReadableName()));
						} else {
							LOG.info("Missing {} key: {}", mappings, key);
						}
					}

					mappings = "potions";
					var potions = DataListLoader.loadDataList(mappings);
					for (DataListEntry entry : potions) {
						var prefix = "";
						register = entry.getName().toLowerCase();
						if (register.replaceAll("\\s", "").length() != register.length()) {
							continue;
						}
						if (register.startsWith("long") || register.startsWith("strong")) {
							register = register.replaceFirst("long_|strong_", "");
							prefix = entry.getName().toLowerCase().replace(register, "");
						}
						var key = "item.minecraft.potion.effect." + register;
						if (languageFile.has(key)) {
							entry.setReadableName(
									StringUtils.getReadableName(prefix + languageFile.get(key).getAsString(),
											entry.getReadableName()));
						} else {
							LOG.info("Missing {} key: {}", mappings, key);
						}
					}

					//					mappings = "villagerprofessions";
					//					var villagerProfessions = DataListLoader.loadDataList(mappings);
					//					for (DataListEntry entry : villagerProfessions) {
					//						register = entry.getName().toLowerCase();
					//						var key = "entity.minecraft.villager." + register;
					//						if (languageFile.has(key)) {
					//							entry.setReadableName(StringUtils.getReadableName(languageFile.get(key).getAsString(),
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
					LOG.error("{}->{}", mappings, register);
					throw new RuntimeException(e);
				}
				translated = true;
			});
		});
	}

	private JsonObject downloadAssets(String version) throws IOException {
		var locale = PreferencesManager.PREFERENCES.ui.language.get();
		if (locale.getLanguage().equals("en")) {
			var possibleLocale = Locale.of(System.getProperty("user.language","en"),System.getProperty("user.region",""));
			if (possibleLocale.getLanguage().equals("en")) {
				LOG.info("ignored language");
				return null;
			} else {
				locale = possibleLocale;
			}
		}
		String name = locale.getLanguage() + "_" + locale.getCountry().toLowerCase();
		LOG.info(name);

		var cache = new File(UserFolderManager.getFileFromUserFolder("cache"), version + "-" + name + ".json");
		if (versionManifest != null) {
			URL url;
			cache.getParentFile().mkdirs();
			try {
				long ms = System.currentTimeMillis() - cache.lastModified();
				LOG.info("{} : {}", cache.getName(), ms);
				if (ms > 36000000L) {
					if (cache.exists()) {
						cache.delete();
					}
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
			return new Gson().fromJson(new FileReader(cache), JsonObject.class);
		} else {
			LOG.info(cache);
			return null;
		}
	}

	public static boolean isSingleLanguageMode() {
		return singleLanguageMode.get();
	}
}
