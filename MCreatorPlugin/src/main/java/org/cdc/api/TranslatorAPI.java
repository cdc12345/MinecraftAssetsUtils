package org.cdc.api;

import net.mcreator.generator.GeneratorWrapper;
import net.mcreator.minecraft.DataListEntry;
import net.mcreator.minecraft.DataListLoader;
import net.mcreator.ui.MCreator;
import org.cdc.utils.L10N;
import org.cdc.utils.StringUtils;

public class TranslatorAPI {
	public static void translate(MCreator a, L10N languageFile) {
		String mappings;
		String register;
		var blocksitems = DataListLoader.loadDataList("blocksitems");
		var generatorWrapper = new GeneratorWrapper(a.getGenerator());
		for (DataListEntry entry : blocksitems) {
			register = generatorWrapper.map(entry.getName(), "blocksitems", 1);
			var entry1 = StringUtils.getNamespaceAndPath(register);
			if (languageFile.has(getItemKey(entry1.getKey(), entry1.getValue())))
				entry.setReadableName(
						StringUtils.getReadableName(languageFile.get(getItemKey(entry1.getKey(), entry1.getValue())),
								entry.getReadableName(), true));
			else if (languageFile.has(getBlockKey(entry1.getKey(), entry1.getValue()), true))
				entry.setReadableName(
						StringUtils.getReadableName(languageFile.get(getBlockKey(entry1.getKey(), entry1.getValue())),
								entry.getReadableName(), true));
			else if (register.charAt(0) == '#')
				entry.setReadableName(StringUtils.getReadableName(languageFile.get(getBlockKey(entry1.getKey(), entry1.getValue())),
						entry.getReadableName(), true));
		}

		var entites = DataListLoader.loadDataList("entities");
		for (DataListEntry entry : entites) {
			register = generatorWrapper.map(entry.getName(), "entities", 2);
			if (register.equals("zombie") && !entry.getName().equals("EntityZombie")) {
				continue;
			}
			var entry1 = StringUtils.getNamespaceAndPath(register);
			var key = getEntityKey(entry1.getKey(), entry1.getValue());
			if (languageFile.has(key)) {
				entry.setReadableName(StringUtils.getReadableName(languageFile.get(key), entry.getReadableName()));
			}
		}

		var biomes = DataListLoader.loadDataList("biomes");
		for (DataListEntry entry : biomes) {
			register = generatorWrapper.map(entry.getName(), "biomes");
			if (register.equals("plains") && !entry.getName().equals("plains")) {
				continue;
			}
			var entry1 = StringUtils.getNamespaceAndPath(register);
			var key = getBiomeKey(entry1.getKey(), entry1.getValue());
			if (languageFile.has(key)) {
				entry.setReadableName(StringUtils.getReadableName(languageFile.get(key), entry.getReadableName()));
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
				entry.setReadableName(StringUtils.getReadableName(languageFile.get(key), entry.getReadableName()));
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
				entry.setReadableName(StringUtils.getReadableName(languageFile.get(key), entry.getReadableName()));
			}
		}

		mappings = "attributes";
		var attributes = DataListLoader.loadDataList(mappings);
		for (DataListEntry entry : attributes) {
			register = StringUtils.getCodeField(generatorWrapper.map(entry.getName(), mappings)).toLowerCase();
			var key = "attribute.name." + register;
			if (languageFile.has(key)) {
				entry.setReadableName(StringUtils.getReadableName(languageFile.get(key), entry.getReadableName()));
				continue;
			}
			key = "attribute.name.generic." + register;
			if (languageFile.has(key)) {
				entry.setReadableName(StringUtils.getReadableName(languageFile.get(key), entry.getReadableName()));
				continue;
			}
			key = "attribute.name.player." + register;
			if (languageFile.has(key)) {
				entry.setReadableName(StringUtils.getReadableName(languageFile.get(key), entry.getReadableName()));
			}
		}

		mappings = "gamemodes";
		var gamemodes = DataListLoader.loadDataList(mappings);
		for (DataListEntry entry : gamemodes) {
			register = entry.getName().toLowerCase();
			var key = "gameMode." + register;
			if (languageFile.has(key)) {
				entry.setReadableName(StringUtils.getReadableName(languageFile.get(key), entry.getReadableName()));
			}
		}

		mappings = "gamerules";
		var gamerules = DataListLoader.loadDataList(mappings);
		for (DataListEntry entry : gamerules) {
			register = net.mcreator.util.StringUtils.snakeToCamel(entry.getName().toLowerCase());
			var key = "gamerule." + net.mcreator.util.StringUtils.lowercaseFirstLetter(register);
			if (languageFile.has(key)) {
				entry.setReadableName(StringUtils.getReadableName(languageFile.get(key), entry.getReadableName()));
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
			var entry1 = StringUtils.getNamespaceAndPath(register);
			var key = getPotionEffectKey(entry1.getKey(), entry1.getValue());
			if (languageFile.has(key)) {
				entry.setReadableName(
						StringUtils.getReadableName(prefix + languageFile.get(key), entry.getReadableName()));
			}
		}
	}

	public static String getItemKey(String namespace, String path) {
		path = path.replace('/','_');
		return "item.%s.%s".formatted(namespace, path);
	}

	public static String getBlockKey(String namespace, String path) {
		// / -> _
		path = path.replace('/','_');
		return "block.%s.%s".formatted(namespace, path);
	}

	public static String getEntityKey(String namespace, String path) {
		return "entity.%s.%s".formatted(namespace, path);
	}

	public static String getBiomeKey(String namespace, String path) {
		return "biome.%s.%s".formatted(namespace, path);
	}

	public static String getPotionEffectKey(String namespace, String path) {
		return "item.%s.potion.effect.%s".formatted(namespace, path);
	}
}
