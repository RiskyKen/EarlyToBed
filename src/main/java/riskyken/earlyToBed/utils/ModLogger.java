package riskyken.earlyToBed.utils;

import org.apache.logging.log4j.Level;

import riskyken.earlyToBed.common.lib.LibModInfo;
import cpw.mods.fml.common.FMLLog;

public class ModLogger {
    
    public static void info(Object object) {
        FMLLog.log(LibModInfo.NAME, Level.INFO, String.valueOf(object));
    }

    public static void log(Level logLevel, Object object) {
        FMLLog.log(LibModInfo.NAME, logLevel, String.valueOf(object));
    }
}
