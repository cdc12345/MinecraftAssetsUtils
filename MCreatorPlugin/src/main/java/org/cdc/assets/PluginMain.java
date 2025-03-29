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
import net.mcreator.plugin.events.workspace.MCreatorLoadedEvent;
import net.mcreator.preferences.PreferencesManager;
import net.mcreator.ui.init.L10N;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdc.data.VersionManifest;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Pattern;

public class PluginMain extends JavaPlugin {
    private final Logger LOG = LogManager.getLogger(PluginMain.class);

    private final VersionManifest versionManifest;

    public PluginMain(Plugin plugin) {
        super(plugin);

        try {
            this.versionManifest = new VersionManifest();
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

        this.addListener(MCreatorLoadedEvent.class, a -> {
            String register = "null";
            try {
                JsonObject jsonObject = downloadAssets(a.getMCreator().getGenerator().getGeneratorMinecraftVersion());
                if (jsonObject == null) {
                    LOG.info("Lang file failed");
                    return;
                }
                var ca = DataListLoader.loadDataList("blocksitems");
                var cam = new GeneratorWrapper(a.getMCreator().getGenerator());
                for (DataListEntry entry : ca) {
                    register = cam.map(entry.getName(), "blocksitems", 1);
                    if (jsonObject.has("item.minecraft." + register))
                        entry.setReadableName(jsonObject.get("item.minecraft." + register).getAsString());
                    else if (jsonObject.has("block.minecraft." + register))
                        entry.setReadableName(jsonObject.get("block.minecraft." + register).getAsString());
                }
            } catch (Exception e) {
                LOG.error(register);
                throw new RuntimeException(e);
            }
        });
    }

    private JsonObject downloadAssets(String version) throws IOException {
        var locale = PreferencesManager.PREFERENCES.ui.language.get();
        if (locale == L10N.DEFAULT_LOCALE) {
            LOG.info("ignored language");
            return null;
        }
        String name = locale.getLanguage() + "_" + locale.getCountry().toLowerCase();

        var url = new URL(versionManifest.getSpecificVersion(version).getClient().getAssetDownloadURL("minecraft/lang/" + name + ".json"));
        var cache = new File(UserFolderManager.getFileFromUserFolder("cache"), "cache" + name + ".json");
        cache.getParentFile().mkdirs();
        try {
            long ms = System.currentTimeMillis() - cache.lastModified();
            LOG.info("{} : {}", cache.getName(), ms);
            if (ms > 360000) {
                if (cache.exists()) {
                    cache.delete();
                }
                FileUtils.copyURLToFile(url, cache, 10_000, 5_000);
            }
        } catch (IOException e) {
            LOG.error(e);
            var input = PluginLoader.INSTANCE.getResources(Pattern.compile(cache.getName()));
            input.forEach(a -> {
                try {
                    FileUtils.copyURLToFile(Objects.requireNonNull(PluginLoader.INSTANCE.getResource(a)), cache);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }


        if (cache.exists()) {
            return new Gson().fromJson(new FileReader(cache), JsonObject.class);
        } else {
            return null;
        }
    }
}
