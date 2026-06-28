package ersa.dev;

import dev.ersa.api.ErsaAPI;
import dev.ersa.api.ErsaAnticheat;
import dev.ersa.api.exception.ErsaNotInitializedException;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private ErsaAPI ersaAPI;

    @Override
    public void onEnable() {
        try {
            ersaAPI = ErsaAnticheat.get();
        } catch (ErsaNotInitializedException e) {
            throw new RuntimeException(e);
        }
        ersaAPI.eventManager().register(new ErsaCommandListener(this));
    }

}
