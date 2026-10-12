package ersa.dev;

import dev.ersa.api.ErsaAC;
import dev.ersa.api.ErsaAPI;
import dev.ersa.api.events.impl.ErsaLoadedEvent;
import dev.ersa.api.exception.ErsaNotInitializedException;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {
    private ErsaAPI ersaAPI;
    private ErsaCommandListener listener;

    @Override
    public void onEnable() {
        if (ErsaAC.isInitialized()) hookAPI();
        else {
            Bukkit.getPluginManager().registerEvents(this, this);
        }
    }

    @EventHandler
    public void onAPILoaded(ErsaLoadedEvent event) {
        hookAPI();
    }

    @Override
    public void onDisable() {
        if (ersaAPI != null) {
            try {
                ersaAPI.commandManager().unregisterCommand("discord");
            } catch (Exception ignored) {
            }
        }
        ErsaCommandListener.shutdown();
        try {
            ersaAPI.eventManager().unregisterEvent(listener);
        } catch (NoSuchMethodError ignored) {
        }
        // silently ignore, since this method was introduced in version 1.0.7
        // do not break backward compatibility
    }

    private void hookAPI() {
        try {
            ersaAPI = ErsaAC.get();
        } catch (ErsaNotInitializedException e) {
            throw new RuntimeException(e);
        }
        listener = new ErsaCommandListener(this);
        ersaAPI.eventManager().registerEvent(listener);
        ersaAPI.commandManager().registerCommand(new DiscordCommand(listener));
        getLogger().info("Hooked Using ErsaAPI");
    }

}
