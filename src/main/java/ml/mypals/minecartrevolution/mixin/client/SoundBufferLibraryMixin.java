package ml.mypals.minecartrevolution.mixin.client;

import ml.mypals.minecartrevolution.client.sound.DirectBufferAudioStream;
import ml.mypals.minecartrevolution.util.MusicUtils;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(SoundBufferLibrary.class)
public class SoundBufferLibraryMixin {
    @Inject(method = "getStream", at = @At(value = "HEAD"), cancellable = true)
    private void getStream(Identifier location, boolean looping, CallbackInfoReturnable<CompletableFuture<AudioStream>> cir) {
        if (location.getNamespace().equals("minecart_music_mp3")) {
            String audioKey = location.getPath();
            if (audioKey.startsWith("sounds/")) {
                audioKey = audioKey.substring("sounds/".length());
            }
            if (audioKey.endsWith(".ogg")) {
                audioKey = audioKey.substring(0, audioKey.length() - ".ogg".length());
            }
            MusicUtils.RawAudioData raw = MusicUtils.getRawData(audioKey);
            if (raw != null) {
                cir.setReturnValue(CompletableFuture.completedFuture(
                        new DirectBufferAudioStream(raw.data(), raw.sampleRate(), raw.channels())
                ));
            }
        }
    }
}
