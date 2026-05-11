package ml.mypals.minecartrevolution.util;

import javazoom.jl.decoder.*;
import net.minecraft.client.Minecraft;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class MusicUtils {
    private static final Map<String, RawAudioData> RAW_CACHE = new ConcurrentHashMap<>();

    private static final Path CACHE_DIR = Minecraft.getInstance().gameDirectory.toPath()
            .resolve("config").resolve("minecart_revolution").resolve("music_cache");

    public record RawAudioData(ByteBuffer data, int sampleRate, int channels) {}

    public static void prepareMusic(String name, URL url) {
        if (RAW_CACHE.containsKey(name)) return;

        CompletableFuture.runAsync(() -> {
            try {
                if (!Files.exists(CACHE_DIR)) Files.createDirectories(CACHE_DIR);
                Path cacheFile = CACHE_DIR.resolve(name + ".mp3");
                byte[] mp3Data;
                if (Files.exists(cacheFile)) {
                    mp3Data = Files.readAllBytes(cacheFile);
                } else {
                    try (InputStream is = url.openStream()) {
                        mp3Data = is.readAllBytes();
                    }
                    Files.write(cacheFile, mp3Data);
                }
                RawAudioData decoded = decodeToRawPcm(new ByteArrayInputStream(mp3Data));
                RAW_CACHE.put(name, decoded);

            } catch (Exception _) {}
        });
    }

    private static RawAudioData decodeToRawPcm(InputStream stream) throws Exception {
        Decoder decoder = new Decoder();
        Bitstream bitstream = new Bitstream(stream);
        ByteArrayOutputStream pcmStream = new ByteArrayOutputStream();

        int sampleRate = 44100;
        boolean rateCaptured = false;
        int currentChannels = 0;

        try {
            Header header;
            while ((header = bitstream.readFrame()) != null) {
                if (!rateCaptured) {
                    sampleRate = header.frequency();
                    rateCaptured = true;
                }
                SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);
                short[] samples = output.getBuffer();
                int length = output.getBufferLength();
                currentChannels = (header.mode() == Header.SINGLE_CHANNEL) ? 1 : 2;
                if (currentChannels == 2) {
                    for (int i = 0; i < length; i += 2) {
                        int mono = (samples[i] + samples[i + 1]) / 2;
                        pcmStream.write(mono & 0xFF);
                        pcmStream.write((mono >> 8) & 0xFF);
                    }
                } else {
                    for (int i = 0; i < length; i++) {
                        pcmStream.write(samples[i] & 0xFF);
                        pcmStream.write((samples[i] >> 8) & 0xFF);
                    }
                }
                bitstream.closeFrame();
            }
        } finally {
            bitstream.close();
        }

        byte[] bytes = pcmStream.toByteArray();
        ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(bytes).flip();
        return new RawAudioData(buffer, sampleRate, 1);
    }

    public static RawAudioData getRawData(String name) {
        return RAW_CACHE.get(name);
    }
}