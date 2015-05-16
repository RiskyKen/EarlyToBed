package riskyken.earlyToBed.common.handler;

import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.EnumStatus;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import riskyken.earlyToBed.common.config.ConfigHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;

public class PlayerSleepInBedHandler {
    
    public PlayerSleepInBedHandler() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }
    
    private boolean wasSleepingServer = false;
    private int sleepTimerServer = -1;
    
    @SubscribeEvent
    public void onPlayerTickEvent(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        
        if (event.phase == Phase.START & FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            wasSleepingServer = isPlayerSleeping(player);
            if (wasSleepingServer) {
                sleepTimerServer = getPlayerSleepTimer(player);
                sleepTimerServer++;
                if (sleepTimerServer > 100) {
                    sleepTimerServer = 100;
                }
                if (!isPlayerInBed(player)) {
                    player.wakeUpPlayer(true, true, false);
                    wasSleepingServer = false;
                }
                setPlayerSleeping(player, false);
                setPlayerSleepTimer(player, 0);
            }
        } 
        
        if (event.phase == Phase.END & FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            if (wasSleepingServer) {
                setPlayerSleeping(player, true);
                setPlayerSleepTimer(player, sleepTimerServer);
                wasSleepingServer = false;
                sleepTimerServer = -1;
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
        EntityPlayer.EnumStatus returnValue = EnumStatus.OK;
        EntityPlayer player = event.entityPlayer;
        
        int x = event.x;
        int y = event.y;
        int z = event.z;
        
        if (!player.worldObj.isRemote) {
            if (player.isPlayerSleeping() || !player.isEntityAlive()) {
                returnValue = EntityPlayer.EnumStatus.OTHER_PROBLEM;
                event.result = returnValue;
                return;
            }
            
            if (!player.worldObj.provider.isSurfaceWorld()) {
                returnValue = EntityPlayer.EnumStatus.NOT_POSSIBLE_HERE;
                event.result = returnValue;
                return;
            }
            
            if (getTimeOfDay(player.worldObj) < ConfigHandler.earliestSleepingTime) {
                returnValue = EntityPlayer.EnumStatus.NOT_POSSIBLE_NOW;
                event.result = returnValue;
                return;
            }
            
            if (Math.abs(player.posX - (double)x) > 3.0D || Math.abs(player.posY - (double)y) > 2.0D || Math.abs(player.posZ - (double)z) > 3.0D) {
                returnValue = EntityPlayer.EnumStatus.TOO_FAR_AWAY;
                event.result = returnValue;
                return;
            }
            
            if (ConfigHandler.checkForHostiles) {
                double d0 = 8.0D;
                double d1 = 5.0D;
                List list = player.worldObj.getEntitiesWithinAABB(EntityMob.class, AxisAlignedBB.getBoundingBox((double)x - d0, (double)y - d1, (double)z - d0, (double)x + d0, (double)y + d1, (double)z + d0));
                if (!list.isEmpty()) {
                    returnValue = EntityPlayer.EnumStatus.NOT_SAFE;
                    event.result = returnValue;
                    return;
                }
            }
        }
        
        if (player.isRiding()) {
            player.mountEntity((Entity)null);
        }
        
        
        try {
            Method m = ReflectionHelper.findMethod(Entity.class, player, new String[] {"func_70105_a", "setSize"}, float.class, float.class);
            m.invoke(player, 0.2F, 0.2F);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //player.setSize(0.2F, 0.2F); //func_70105_a
        
        player.yOffset = 0.2F;
    
        if (player.worldObj.blockExists(x, y, z))
        {
            int l = player.worldObj.getBlock(x, y, z).getBedDirection(player.worldObj, x, y, z);
            float f1 = 0.5F;
            float f = 0.5F;
    
            switch (l)
            {
                case 0:
                    f = 0.9F;
                    break;
                case 1:
                    f1 = 0.1F;
                    break;
                case 2:
                    f = 0.1F;
                    break;
                case 3:
                    f1 = 0.9F;
            }
            
            
            try {
                Method m = ReflectionHelper.findMethod(EntityPlayer.class, player, new String[] {"func_71013_b"}, int.class);
                m.invoke(player, l);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            
            //player.func_71013_b(l);
            
            player.setPosition((double)((float)x + f1), (double)((float)y + 0.9375F), (double)((float)z + f));
        } else {
            player.setPosition((double)((float)x + 0.5F), (double)((float)y + 0.9375F), (double)((float)z + 0.5F));
        }
        
        if (!(setPlayerSleeping(player, true) & setPlayerSleepTimer(player, 0))) {
            return;
        }
        
        //player.sleeping = true; //field_71083_bS
        //player.sleepTimer = 0;  //field_71076_b
        
        player.playerLocation = new ChunkCoordinates(x, y, z);
        player.motionX = player.motionZ = player.motionY = 0.0D;
        
        if (!player.worldObj.isRemote) {
            player.worldObj.updateAllPlayersSleepingFlag();
        }
        
        event.result = returnValue;
    }
    
    private boolean isPlayerSleeping(EntityPlayer player) {
        return player.isPlayerSleeping();
    }
    
    private int getPlayerSleepTimer(EntityPlayer player) {
        return player.getSleepTimer();
    }
    
    private boolean setPlayerSleeping(EntityPlayer player, boolean sleeping) {
        try {
            ReflectionHelper.setPrivateValue(EntityPlayer.class, player, sleeping, new String[] {"field_71083_bS" , "sleeping"});
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    private boolean setPlayerSleepTimer(EntityPlayer player, int sleepTimer) {
        try {
            ReflectionHelper.setPrivateValue(EntityPlayer.class, player, sleepTimer, new String[] {"field_71076_b" , "sleepTimer"});
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    private boolean isPlayerInBed(EntityPlayer player) {
        return player.worldObj.getBlock(player.playerLocation.posX, player.playerLocation.posY, player.playerLocation.posZ).isBed(player.worldObj, player.playerLocation.posX, player.playerLocation.posY, player.playerLocation.posZ, player);
    }
    
    private int getTimeOfDay(World world) {
        long totalTime = world.provider.getWorldTime();
        return (int) (totalTime % 24000);
    }
}
