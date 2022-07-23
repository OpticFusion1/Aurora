package com.zenya.aurora.util.ext;

import org.bukkit.Location;
import ru.beykerykt.minecraft.lightapi.common.api.engine.EditPolicy;
import ru.beykerykt.minecraft.lightapi.common.api.engine.SendPolicy;

/**
 * LightAPI https://github.com/BeYkeRYkt/LightAPI
 *
 * @version 5.0.0-preview
 */
public class LightAPI {

    public static LightAPI INSTANCE = new LightAPI();
    private static ru.beykerykt.minecraft.lightapi.common.LightAPI mAPI;
    private static boolean disabled;

    public LightAPI() {
        mAPI = ru.beykerykt.minecraft.lightapi.common.LightAPI.get();
        disabled = false;
    }

    public static void disable() {
        disabled = true;
    }

    /**
     * A method to remove lighting and instantly update it client-side
     *
     * @param location Location to remove lighting from
     * @param lightFlags Type of lighting to remove
     * @param async Whether light updating should be asynchronous
     * @see #setLight(Location, int, int, boolean, boolean)
     */
    public static void clearLight(Location location, int lightFlags, boolean async, boolean force) {
        setLight(location, lightFlags, 0, async, force);
    }

    /**
     * A method to set lighting and instantly update it client-side
     *
     * @param location Location to create lighting
     * @param lightLevel Light level to set
     * @param lightFlags Type of lighting to create
     * @param async Whether light updating should be asynchronous
     */
    public static void setLight(Location location, int lightFlags, int lightLevel, boolean async, boolean force) {
        if (disabled) {
            return;
        }
        EditPolicy editPolicy = async ? EditPolicy.DEFERRED : EditPolicy.IMMEDIATE;
        SendPolicy sendPolicy = async ? SendPolicy.DEFERRED : SendPolicy.IMMEDIATE;
        if (force) {
            editPolicy = EditPolicy.FORCE_IMMEDIATE;
            sendPolicy = SendPolicy.IMMEDIATE;
        }
        mAPI.setLightLevel(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                lightLevel, lightFlags, editPolicy, sendPolicy, null);
    }
}
