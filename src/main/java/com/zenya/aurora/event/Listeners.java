package com.zenya.aurora.event;

import com.github.ipecter.rtu.biomelib.RTUBiomeLib;
import com.zenya.aurora.Aurora;
import com.zenya.aurora.file.ParticleFile;
import com.zenya.aurora.storage.ParticleFileCache;
import com.zenya.aurora.storage.ParticleManager;
import com.zenya.aurora.storage.ToggleManager;
import com.zenya.aurora.storage.StorageFileManager;
import com.zenya.aurora.particle.*;
import com.zenya.aurora.util.LocationTools;
import com.zenya.aurora.util.TimeCheck;
import com.zenya.aurora.worldguard.AmbientParticlesFlag;
import com.zenya.aurora.worldguard.WGManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Listeners implements Listener {

    private void spawnParticles(ParticleUpdateEvent e, List<ParticleFile> particleFiles) {
        Player player = e.getPlayer();

        for (ParticleFile particleFile : particleFiles) {
            if (!particleFile.isEnabled()) {
                continue;
            }

            LocationTools.getParticleLocations(
                    e.getNearbyChunks(StorageFileManager.getConfig().getInt("particle-spawn-radius")),
                    particleFile.getSpawning().isRelativePlayerPosition() ? particleFile.getSpawning().getMinY() + player.getLocation().getY() : particleFile.getSpawning().getMinY(),
                    particleFile.getSpawning().isRelativePlayerPosition() ? particleFile.getSpawning().getMaxY() + player.getLocation().getY() : particleFile.getSpawning().getMaxY(),
                    particleFile.getSpawning().getSpawnDistance(),
                    particleFile.getSpawning().getRandMultiplier(),
                    particleFile.getSpawning().isShuffleLocations()).thenAcceptAsync(locs -> {

                switch (particleFile.getParticle().getParticleType().toUpperCase()) {
                    case "LINE" ->
                        ParticleManager.INSTANCE.registerTask(player, new LineParticle(player, locs, particleFile));
                    case "CUBE" ->
                        ParticleManager.INSTANCE.registerTask(player, new CubeParticle(player, locs, particleFile));
                    case "RING" ->
                        ParticleManager.INSTANCE.registerTask(player, new RingParticle(player, locs, particleFile));
                    case "CIRCLE" ->
                        ParticleManager.INSTANCE.registerTask(player, new CircleParticle(player, locs, particleFile));
                    case "SPHERE" ->
                        ParticleManager.INSTANCE.registerTask(player, new SphereParticle(player, locs, particleFile));
                    case "WAVE" ->
                        ParticleManager.INSTANCE.registerTask(player, new WaveParticle(player, locs, particleFile));
                    default -> //Default to point particle
                        ParticleManager.INSTANCE.registerTask(player, new PointParticle(player, locs, particleFile));
                }
            });
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                boolean status = StorageFileManager.getDatabase().getToggleStatus(player.getName());
                ToggleManager.INSTANCE.cacheToggle(player.getName(), status);
                Bukkit.getPluginManager().callEvent(new ParticleUpdateEvent(player));
            }
        }.runTask(Aurora.getInstance());
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        ToggleManager.INSTANCE.uncacheToggle(player.getName());
        Bukkit.getPluginManager().callEvent(new ParticleUpdateEvent(player));
    }

    @EventHandler
    public void onParticleUpdateEvent(ParticleUpdateEvent e) {
        //Init variables
        Player player = e.getPlayer();
        String biomeName = RTUBiomeLib.getInterface().getBiomeName(player.getLocation()).toLowerCase();

        //Remove old tasks
        ParticleManager.INSTANCE.unregisterTasks(player, false);

        //Ignore if spawn conditions are not met
        if (StorageFileManager.getConfig().listContains("disabled-worlds", player.getWorld().getName())) {
            return;
        }
        if (!TimeCheck.isDuring(player.getPlayerTime())) {
            return;
        }

        //Ignore for disabled players
        if (!player.hasPermission("aurora.view")) {
            return;
        }
        if (!ToggleManager.INSTANCE.isToggled(player.getName())) {
            return;
        }

        //Register new tasks
        List<ParticleFile> biomeParticles = ParticleFileCache.INSTANCE.getClass(biomeName);
        if (WGManager.INSTANCE.getWorldGuard() != null) {
            List<ParticleFile> regionParticles = AmbientParticlesFlag.INSTANCE.getParticles(player);
            //WorldGuard support
            if (!regionParticles.isEmpty()) {
                spawnParticles(e, regionParticles);
            } else {
                if (biomeParticles != null && !biomeParticles.isEmpty()) {
                    spawnParticles(e, biomeParticles);
                }
            }
        } else {
            //No WorldGuard support
            if (biomeParticles != null && !biomeParticles.isEmpty()) {
                spawnParticles(e, biomeParticles);
            }
        }
    }
}
