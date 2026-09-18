package store.menkiestes.menkiafk;

import org.junit.jupiter.api.Test;
import store.menkiestes.menkiafk.placeholder.MenkiAfkExpansion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PlaceholderApiContractTest {

    @Test
    void expansionDeclaresMenkiAfkOwnershipAndPersistence() throws NoSuchMethodException {
        assertEquals(
                MenkiAfkExpansion.class,
                MenkiAfkExpansion.class.getMethod("getRequiredPlugin").getDeclaringClass(),
                "MENKIAFK must explicitly own its internal PlaceholderAPI expansion"
        );
        assertEquals(
                MenkiAfkExpansion.class,
                MenkiAfkExpansion.class.getMethod("persist").getDeclaringClass(),
                "MENKIAFK expansion must explicitly persist across /papi reload"
        );
    }

    @Test
    void hotPathPlaceholdersDoNotEagerlySnapshotStatistics() throws IOException {
        String expansion = source("src/main/java/store/menkiestes/menkiafk/placeholder/MenkiAfkExpansion.java");

        assertFalse(
                expansion.contains("StatsManager.Snapshot stats = statsManager.snapshot(player.getUniqueId());"),
                "Status/reason/time/type placeholders must not eagerly snapshot synchronized statistics"
        );
        assertTrue(expansion.contains("case \"status\" -> manager.placeholderStatus(id);"));
        assertTrue(expansion.contains("case \"last_afk\" -> lastAfk(statsManager.snapshot(id));"));
    }

    @Test
    void pluginHandlesPlaceholderApiDisableAndReenable() throws IOException {
        String plugin = source("src/main/java/store/menkiestes/menkiafk/MenkiAfkPlugin.java");

        assertTrue(plugin.contains("implements Listener"));
        assertTrue(plugin.contains("PluginEnableEvent"));
        assertTrue(plugin.contains("PluginDisableEvent"));
        assertTrue(plugin.contains("unhookPlaceholderApi();"));
        assertTrue(plugin.contains("placeholderExpansion = null;"));
    }

    @Test
    void placeholderApiRemainsOptionalAtRuntime() throws IOException {
        String pluginYml = source("src/main/resources/plugin.yml");
        String pom = source("pom.xml");

        assertTrue(pluginYml.contains("softdepend: [PlaceholderAPI, Essentials]"));
        assertTrue(pom.contains("<artifactId>placeholderapi</artifactId>"));

        int dependency = pom.indexOf("<artifactId>placeholderapi</artifactId>");
        int dependencyEnd = pom.indexOf("</dependency>", dependency);
        assertTrue(dependency >= 0 && dependencyEnd > dependency);
        String papiDependency = pom.substring(dependency, dependencyEnd);
        assertTrue(papiDependency.contains("<scope>provided</scope>"));
    }

    private static String source(String relativePath) throws IOException {
        return Files.readString(Path.of(relativePath));
    }
}
