package com.zenya.aurora.storage;

import com.github.ipecter.rtu.biomelib.RTUBiomeLib;
import com.zenya.aurora.file.ParticleFile;
import com.zenya.aurora.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ParticleFileCache {

    public static ParticleFileCache INSTANCE = new ParticleFileCache();
    private HashMap<String, List<ParticleFile>> particleCacheMap = new HashMap<>();

    public ParticleFileCache() {
        for (ParticleFile particleFile : ParticleFileManager.INSTANCE.getParticles()) {
            if (particleFile.getSpawning() == null || particleFile.getSpawning().getBiomes() == null || particleFile.getSpawning().getBiomes().length == 0) {
                continue;
            }
            String[] biomes = particleFile.getSpawning().getBiomes();
            if (biomes.length == 1 && biomes[0].equals("ALL")) {
                for (String biome : RTUBiomeLib.getInterface().getBiomesName()) {
                    registerBiome(biome, particleFile);
                }
                return;
            }
            for (String biome : particleFile.getSpawning().getBiomes()) {
                registerBiome(biome, particleFile);
            }
        }
    }

    private void registerBiome(String biomeName, ParticleFile particleFile) {
        try {
            registerClass(biomeName, particleFile);
        } catch (Exception exc) {
            Logger.logError("Error loading biome %s from particle %s", biomeName, particleFile.getName());
        }
    }

    public List<ParticleFile> getClass(String biome) {
        return particleCacheMap.getOrDefault(biome, new ArrayList<>());
    }

    public Set<String> getBiomes() {
        return particleCacheMap.keySet();
    }

    public void registerClass(String biome, ParticleFile particleFile) {
        String biomeName = namespaced(biome);
        particleCacheMap.computeIfAbsent(biomeName, k -> new ArrayList<>()).add(particleFile);
    }

    private String namespaced(String name) {
        return name.contains(":") ? name.toLowerCase() : "minecraft:" + name.toLowerCase();
    }

    public void unregisterFile(String name) {
        particleCacheMap.remove(name);
    }

    public static void reload() {
        ParticleFileManager.INSTANCE.reload();
        INSTANCE = new ParticleFileCache();
    }
}
