package name.modid;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;  
import net.minecraft.util.crash.CrashException;  
import net.minecraft.util.crash.CrashReport; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerAutorestart implements ModInitializer {
	public static final String MOD_ID = "serverautorestart";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final long INACTIVITY_THRESHOLD = 1000 * 60 * 30; // 30 minutes
	private static final int CHECK_INTERVAL_TICKS = 20 * 60 * 10; // 10 minute
	private long lastActivityTime;
	private int tickCounter;

	@Override
	public void onInitialize() {
		lastActivityTime = System.currentTimeMillis();
		this.tickCounter = 0;
		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
	}

	private void onServerTick(MinecraftServer server) {  
        tickCounter++;  
        if (tickCounter >= CHECK_INTERVAL_TICKS) {
			LOGGER.info("Checking server activity...");
            tickCounter = 0; // Reset the counter  
            checkInactivity(server);  
        }  
    }

	private void checkInactivity(MinecraftServer server) {
		try {  
            if (server.getPlayerManager().getPlayerList().isEmpty()) {  
                long currentTime = System.currentTimeMillis();  
                if (currentTime - lastActivityTime > INACTIVITY_THRESHOLD) {  
					LOGGER.info(String.format("Server has been inactive for %d minutes. Restarting...", INACTIVITY_THRESHOLD / 1000 / 60));
                    server.stop(true);
                }  
            } else {
                lastActivityTime = System.currentTimeMillis();  
            }  
        } catch (CrashException e) {  
            CrashReport crashReport = e.getReport();  
            server.setCrashReport(crashReport);
        }
	}

}