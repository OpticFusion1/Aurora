package com.zenya.aurora.file;

import com.zenya.aurora.storage.StorageFileManager;
import com.zenya.aurora.util.RandomNumber;
import java.util.ArrayList;
import java.util.List;

public class ParticleFile {

    private String name;
    private boolean enabled;
    private Spawning spawning;
    private Particle particle;
    private Properties properties;

    public static class Spawning {

        private String[] biomes;
        private double spawnDistance;
        private double randMultiplier;
        private boolean relativePlayerPosition;
        private double minY;
        private double maxY;
        private boolean shuffleLocations;

        public String[] getBiomes() {
            YAMLFile presentsFile = StorageFileManager.getBiomes();
            List<String> finalBiomes = new ArrayList<>();

            if (biomes != null && biomes.length != 0) {
                for (String biome : biomes) {
                    if (biome.toUpperCase().startsWith("PRESENT=")) {
                        String present = biome.split("=")[1];
                        if (presentsFile.getList(present) != null && !presentsFile.getList(present).isEmpty()) {
                            for (String presentBiome : presentsFile.getList(present)) {
                                finalBiomes.add(namespaced(presentBiome));
                            }
                        }
                    } else {
                        finalBiomes.add(namespaced(biome));
                    }
                }
            }
            return finalBiomes.toArray(new String[finalBiomes.size()]);
        }
        private String namespaced(String name) {
            return name.contains(":") ? name.toLowerCase() : "minecraft:" + name.toLowerCase();
        }
        public double getSpawnDistance() {
            return spawnDistance;
        }

        public double getRandMultiplier() {
            return randMultiplier;
        }

        public boolean isRelativePlayerPosition() {
            return relativePlayerPosition;
        }

        public double getMinY() {
            return minY;
        }

        public double getMaxY() {
            return maxY;
        }

        public boolean isShuffleLocations() {
            return shuffleLocations;
        }

    }

    public static class Particle {

        private String particleName;
        private String particleType;
        private int maxCount;
        private boolean enableLighting;

        public String getParticleName() {
            return particleName;
        }

        public String getParticleType() {
            return particleType;
        }

        public int getMaxCount() {
            return maxCount;
        }

        public boolean isEnableLighting() {
            return enableLighting;
        }

    }

    public static class Properties {

        private RandomNumber<Double> rate;
        private RandomNumber<Integer> update;
        private RandomNumber<Long> duration;
        private RandomNumber<Double> length;
        private RandomNumber<Double> radius;
        private RandomNumber<Double> waveCycles;
        private RandomNumber<Double> waveAmplitude;
        private RandomNumber<Double> rotationAngle;
        private char rotationAxis;

        public RandomNumber<Double> getRate() {
            return rate;
        }

        public RandomNumber<Integer> getUpdate() {
            return update;
        }

        public RandomNumber<Long> getDuration() {
            return duration;
        }

        public RandomNumber<Double> getLength() {
            return length;
        }

        public RandomNumber<Double> getRadius() {
            return radius;
        }

        public RandomNumber<Double> getWaveCycles() {
            return waveCycles;
        }

        public RandomNumber<Double> getWaveAmplitude() {
            return waveAmplitude;
        }

        public RandomNumber<Double> getRotationAngle() {
            return rotationAngle;
        }

        public char getRotationAxis() {
            return rotationAxis;
        }

    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Spawning getSpawning() {
        return spawning;
    }

    public Particle getParticle() {
        return particle;
    }

    public Properties getProperties() {
        return properties;
    }

}
