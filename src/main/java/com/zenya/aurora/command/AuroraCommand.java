package com.zenya.aurora.command;

import com.github.ipecter.rtu.biomelib.RTUBiomeLib;
import com.zenya.aurora.Aurora;
import com.zenya.aurora.event.ParticleUpdateEvent;
import com.zenya.aurora.util.ext.LightAPI;
import com.zenya.aurora.file.ParticleFile;
import com.zenya.aurora.storage.ParticleFileCache;
import com.zenya.aurora.storage.ParticleFileManager;
import com.zenya.aurora.storage.ToggleManager;
import com.zenya.aurora.storage.StorageFileManager;
import com.zenya.aurora.util.LocationTools;
import com.zenya.aurora.util.ChatBuilder;
import com.zenya.aurora.util.ChunkContainer;
import com.zenya.aurora.worldguard.AmbientParticlesFlag;
import com.zenya.aurora.worldguard.WGManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AuroraCommand implements CommandExecutor {

    private void sendUsage(CommandSender sender) {
        ChatBuilder chat = (new ChatBuilder()).withSender(sender);
        chat.sendMessages("command.help");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        ChatBuilder chat = (new ChatBuilder()).withSender(sender);

        //No command arguments
        if (args.length < 1) {
            sendUsage(sender);
            return true;
        }

        //No permission
        if (!sender.hasPermission("aurora.command." + args[0])) {
            chat.sendMessages("no-permission");
            return true;
        }

        //help, toggle, reload, status
        if (args.length == 1) {
            if (args[0].toLowerCase().equals("help")) {
                sendUsage(sender);
                return true;
            }

            if (args[0].toLowerCase().equals("toggle")) {
                if (!(sender instanceof Player)) {
                    chat.sendMessages("player-required");
                    return true;
                }
                Player player = (Player) sender;
                chat.withPlayer(player);
                if (ToggleManager.INSTANCE.isToggled(player.getName())) {
                    ToggleManager.INSTANCE.registerToggle(player.getName(), false);
                    chat.sendMessages("command.toggle.disable");
                } else {
                    ToggleManager.INSTANCE.registerToggle(player.getName(), true);
                    chat.sendMessages("command.toggle.enable");
                }
                Bukkit.getPluginManager().callEvent(new ParticleUpdateEvent(player));
                return true;
            }

            if (args[0].toLowerCase().equals("reload")) {
                StorageFileManager.reloadFiles();
                if (!StorageFileManager.getConfig().getBool("enable-lighting")) {
                    try {
                        LightAPI.disable();
                    } catch (NoClassDefFoundError exc) {
                        // Already disabled, do nothing
                    }
                }
                LightAPI api = LightAPI.INSTANCE;
                ParticleFileCache.reload();
                chat.withArgs(ParticleFileManager.INSTANCE.getParticles().size()).sendMessages("command.reload");

                for (Player p : Bukkit.getOnlinePlayers()) {
                    Bukkit.getPluginManager().callEvent(new ParticleUpdateEvent(p));
                }
                return true;
            }

            if (args[0].toLowerCase().equals("status")) {
                String globalFiles = "";
                if (ParticleFileManager.INSTANCE.getParticles() == null || ParticleFileManager.INSTANCE.getParticles().size() == 0) {
                    //No particle files
                    globalFiles += "None";
                } else {
                    for (ParticleFile particleFile : ParticleFileManager.INSTANCE.getParticles()) {
                        //Check if enabled or disabled
                        String particleName = particleFile.isEnabled() ? ChatBuilder.translateColor("&a") : ChatBuilder.translateColor("&c");
                        //If enabled, check if active in region/biome
                        if (particleFile.isEnabled() && sender instanceof Player) {
                            Player player = (Player) sender;
                            String biomeName = RTUBiomeLib.getInterface().getBiomeName(player.getLocation()).toUpperCase();

                            //WG support
                            if (WGManager.getWorldGuard() != null) {
                                if (AmbientParticlesFlag.INSTANCE.getParticles(player).contains(particleFile)) {
                                    //Set if particle is in region
                                    particleName = ChatBuilder.translateColor("&b");
                                } else if (AmbientParticlesFlag.INSTANCE.getParticles(player).isEmpty()
                                        && ParticleFileCache.INSTANCE.getClass(biomeName).contains(particleFile)) {
                                    //If region has no particles, fallback to biome
                                    particleName = ChatBuilder.translateColor("&b");
                                }
                                //No WG
                            } else if (ParticleFileCache.INSTANCE.getClass(biomeName).contains(particleFile)) {
                                //Only set if particle is in biome
                                particleName = ChatBuilder.translateColor("&b");
                            }

                            //Check if disabled in world
                            if (StorageFileManager.getConfig().listContains("disabled-worlds", player.getWorld().getName())) {
                                particleName = ChatBuilder.translateColor("&c");
                            }
                        }
                        particleName += (particleFile.getName() + ChatBuilder.translateColor("&f, "));
                        globalFiles += particleName;
                    }
                }
                chat.withArgs(ParticleFileManager.INSTANCE.getParticles().size(), globalFiles).sendMessages("command.status");
                return true;
            }

            //Wrong arg1
            sendUsage(sender);
            return true;
        }

        //toggle, fixlighting
        if (args.length == 2) {
            if (args[0].toLowerCase().equals("toggle")) {
                if (!(sender instanceof Player)) {
                    chat.sendMessages("player-required");
                    return true;
                }
                Player player = (Player) sender;

                switch (args[1].toLowerCase()) {
                    case "on" -> {
                        ToggleManager.INSTANCE.registerToggle(player.getName(), true);
                        chat.sendMessages("command.toggle.enable");
                    }
                    case "off" -> {
                        ToggleManager.INSTANCE.registerToggle(player.getName(), false);
                        chat.sendMessages("command.toggle.disable");
                    }
                    default -> {
                        //Wrong arg2 for toggle
                        sendUsage(sender);
                        return true;
                    }
                }
                Bukkit.getPluginManager().callEvent(new ParticleUpdateEvent(player));
                return true;
            }

            if (args[0].toLowerCase().equals("fixlighting")) {
                if (!(sender instanceof Player)) {
                    chat.sendMessages("player-required");
                    return true;
                }
                Player player = (Player) sender;

                try {
                    int chunks = (Integer.parseInt(args[1]) > 0 && Integer.parseInt(args[1]) < 10) ? Integer.parseInt(args[1]) : 10;
                    chat.sendMessages("command.fixlighting.start");
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (ChunkContainer container : LocationTools.getSurroundingChunks(player.getLocation().getChunk(), chunks)) {
                                container.getWorld().refreshChunk(container.getX(), container.getZ());
                            }
                            chat.withArgs(chunks, chunks).sendMessages("command.fixlighting.done");
                        }
                    }.runTask(Aurora.getInstance());
                } catch (NumberFormatException exc) {
                    //Wrong arg2
                    sendUsage(sender);
                }
                return true;
            }

            //Wrong arg1
            sendUsage(sender);
            return true;
        }
        //Incorrect number of args
        sendUsage(sender);
        return true;
    }
}
