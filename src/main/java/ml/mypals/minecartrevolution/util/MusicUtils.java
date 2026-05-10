package ml.mypals.minecartrevolution.util;

import javazoom.jl.decoder.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MusicUtils {
    private static final Map<String, Integer> BUFFER_POOL = Collections.synchronizedMap(new HashMap<>());
    private static final Set<Integer> ACTIVE_SOURCES = Collections.synchronizedSet(new HashSet<>());

    private static final Path CACHE_DIR = Minecraft.getInstance().gameDirectory.toPath()
            .resolve("config").resolve("minecart_revolution").resolve("music_cache");

    private record DecodedResult(ByteBuffer buffer, int sampleRate) {}

    public static void downloadAndRegister(String name, URL url) {
        if (BUFFER_POOL.containsKey(name)) return;

        CompletableFuture.supplyAsync(() -> {
            try {
                if (!Files.exists(CACHE_DIR)) Files.createDirectories(CACHE_DIR);
                Path cacheFile = CACHE_DIR.resolve(name + ".mp3");

                if (Files.exists(cacheFile)) {
                    try (InputStream is = Files.newInputStream(cacheFile)) {
                        return decodeToPcm(is);
                    }
                } else {
                    byte[] data;
                    try (InputStream is = url.openStream()) {
                        data = is.readAllBytes();
                    }
                    CompletableFuture.runAsync(() -> {
                        try {
                            Files.write(cacheFile, data);
                        } catch (IOException _) {}
                    });
                    try (InputStream is = new ByteArrayInputStream(data)) {
                        return decodeToPcm(is);
                    }
                }
            } catch (Exception _) {}
            return null;
        }).thenAccept(result -> {
            if (result != null) {
                Minecraft.getInstance().execute(() -> registerBuffer(name, result.buffer, result.sampleRate));
            }
        });
    }

    private static DecodedResult decodeToPcm(InputStream mp3Stream) throws Exception {
        Decoder decoder = new Decoder();
        Bitstream bitstream = new Bitstream(mp3Stream);
        ByteArrayOutputStream pcmStream = new ByteArrayOutputStream();
        int sampleRate = -1;

        try {
            Header header;
            while ((header = bitstream.readFrame()) != null) {
                if (sampleRate == -1) sampleRate = header.frequency();

                SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);
                short[] samples = output.getBuffer();
                int length = output.getBufferLength();
                int channels = output.getChannelCount();

                for (int i = 0; i < length; i += channels) {
                    short val = (channels == 2) ? (short)((samples[i] + samples[i+1])/2) : samples[i];
                    pcmStream.write(val & 0xFF);
                    pcmStream.write((val >> 8) & 0xFF);
                }
                bitstream.closeFrame();
            }
        } finally {
            bitstream.close();
        }

        byte[] bytes = pcmStream.toByteArray();
        ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
        buffer.put(bytes).flip();
        return new DecodedResult(buffer, sampleRate);
    }

    private static void registerBuffer(String name, ByteBuffer pcmData, int sampleRate) {
        synchronized (BUFFER_POOL) {
            if (BUFFER_POOL.containsKey(name)) AL10.alDeleteBuffers(BUFFER_POOL.get(name));
            int bufferId = AL10.alGenBuffers();
            AL10.alBufferData(bufferId, AL10.AL_FORMAT_MONO16, pcmData, sampleRate);
            BUFFER_POOL.put(name, bufferId);
        }
    }

    public static int getBuffer(String name) {
        return BUFFER_POOL.getOrDefault(name, -1);
    }

    public static void addSource(int id) {
        ACTIVE_SOURCES.add(id);
    }

    public static void removeSource(int id) {
        ACTIVE_SOURCES.remove(id);
    }

    public static void syncActiveSources(float gain) {
        synchronized (ACTIVE_SOURCES) {
            Iterator<Integer> it = ACTIVE_SOURCES.iterator();
            while (it.hasNext()) {
                int id = it.next();
                if (!AL10.alIsSource(id)) {
                    it.remove();
                    continue;
                }
                AL10.alSourcef(id, AL10.AL_GAIN, gain);
            }
        }
    }

    public static void cleanupAll() {
        synchronized (ACTIVE_SOURCES) {
            for (int sourceId : ACTIVE_SOURCES) {
                if (AL10.alIsSource(sourceId)) {
                    AL10.alSourceStop(sourceId);
                    AL10.alDeleteSources(sourceId);
                }
            }
            ACTIVE_SOURCES.clear();
        }
        synchronized (BUFFER_POOL) {
            for (int bufferId : BUFFER_POOL.values()) {
                if (AL10.alIsBuffer(bufferId)) {
                    AL10.alDeleteBuffers(bufferId);
                }
            }
            BUFFER_POOL.clear();
        }
    }

    public static void pauseAll() {
        synchronized (ACTIVE_SOURCES) {
            for (int id : ACTIVE_SOURCES) {
                if (AL10.alIsSource(id) && AL10.alGetSourcei(id, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING) {
                    AL10.alSourcePause(id);
                }
            }
        }
    }
}