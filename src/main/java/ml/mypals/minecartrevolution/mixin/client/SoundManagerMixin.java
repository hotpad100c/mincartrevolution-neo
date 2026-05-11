package ml.mypals.minecartrevolution.mixin.client;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundManager.class)
public class SoundManagerMixin {
    @Inject(method = "getSoundEvent", at = @At("HEAD"), cancellable = true)
    private void onGetSoundEvent(Identifier location, CallbackInfoReturnable<WeighedSoundEvents> cir) {
        if (location.getNamespace().equals("minecart_music_mp3")) {
            WeighedSoundEvents overrideEvent = new WeighedSoundEvents(location, null) {
                @Override
                public @NonNull Sound getSound(@NonNull RandomSource random) {
                    return new Sound(
                            location,
                            ConstantFloat.of(1.0F),
                            ConstantFloat.of(1.0F),
                            1,
                            Sound.Type.FILE,
                            true,
                            false,
                            16
                    );
                }

                @Override
                public int getWeight() {
                    return 1;
                }
            };
            cir.setReturnValue(overrideEvent);
        }
    }
}
