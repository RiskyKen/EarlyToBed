package riskyken.earlyToBed.common.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public final class ConfigHandler {
    
    public static final String CATEGORY_GENERAL = "general";
    
    private static Configuration config;
    
    public static int earliestSleepingTime;
    public static boolean checkForHostiles;
    
    public static void init(File file) {
        if (config == null) {
            config = new Configuration(file);
            loadConfigFile();
        }
    }
    
    public static void loadConfigFile() {
        earliestSleepingTime =
                config.getInt("Earliest Sleeping Time", CATEGORY_GENERAL, 6000, 0, 28000,
                        "The earliest time that players are allowed to sleep at.\n" +
                        "\n" +
                        "0 = dawn\n" + 
                        "6000 = noon\n" + 
                        "12000 = sunset (Minecraft default)\n" + 
                        "18000 = midnight\n");
        
        checkForHostiles = 
                config.getBoolean("Check for Hostiles", CATEGORY_GENERAL, true,
                        "If set to false players will be allows to sleep even if hostiles mobs are nearby.");
        
        if (config.hasChanged()) {
            config.save();
        }
    }
}
