package riskyken.earlyToBed;

import riskyken.earlyToBed.common.config.ConfigHandler;
import riskyken.earlyToBed.common.handler.PlayerSleepInBedHandler;
import riskyken.earlyToBed.common.lib.LibModInfo;
import riskyken.earlyToBed.utils.ModLogger;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = LibModInfo.ID, name = LibModInfo.NAME, version = LibModInfo.VERSION)
public class EarlyToBed {

    @Mod.Instance(LibModInfo.ID)
    public static EarlyToBed instance;
    
    @Mod.EventHandler
    public void perInit(FMLPreInitializationEvent event) {
        ModLogger.info("Loading " + LibModInfo.NAME + " " + LibModInfo.VERSION);
        ConfigHandler.init(event.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void load(FMLInitializationEvent event) {
        new PlayerSleepInBedHandler();
    }
    
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }
}
