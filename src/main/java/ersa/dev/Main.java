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

    @Override
    public void onEnable() {
        if (ErsaAC.isInitialized()) hookAPI();
        else {
            Bukkit.getPluginManager().registerEvents(this,this);
        }
    }
    @EventHandler
    public void onAPILoaded(ErsaLoadedEvent event) {
        hookAPI();
    }

    private void hookAPI() {
        try {
            ersaAPI = ErsaAC.get();
        } catch (ErsaNotInitializedException e) {
            throw new RuntimeException(e);
        }
        ersaAPI.eventManager().registerEvent(new ErsaCommandListener(this));
        getLogger().info("Hooked Using ErsaAPI");
    }

}
