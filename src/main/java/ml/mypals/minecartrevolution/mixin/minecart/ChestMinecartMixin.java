package ml.mypals.minecartrevolution.mixin.minecart;

import ml.mypals.minecartrevolution.interfaces.IMinecartChestExtension;
import ml.mypals.minecartrevolution.interfaces.IMinecartContainer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartChest.class)
public abstract class ChestMinecartMixin extends AbstractMinecart implements IMinecartContainer, IMinecartChestExtension {
    @Unique
    private final ChestLidController minecartrevolution$chestLidController = new ChestLidController();
    @Unique
    private float minecartrevolution$viewers = 0;

    protected ChestMinecartMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(method = "interact", at = @At(target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;angerNearbyPiglins(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/player/Player;Z)V",value = "INVOKE"))
    public void interact(Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {
        minecartrevolution$viewers++;
    }
    @Override
    public void minecartrevolution$OnContainerClosed(Level level, Player player) {
        this.level().broadcastEntityEvent(this, (byte) 11);
        minecartrevolution$viewers--;
        if (this.minecartrevolution$viewers <= 0) {
            minecartrevolution$viewers = 0;
            this.gameEvent(GameEvent.CONTAINER_CLOSE, player);
            this.level().playSound(this, this.blockPosition(), SoundEvents.SHULKER_CLOSE, SoundSource.BLOCKS);
        }
    }




    @Override
    public ChestLidController minecartrevolution$getChestLidController() {
        return minecartrevolution$chestLidController;
    }
    @Override
    public void tick(){
        super.tick();
        this.minecartrevolution$chestLidController.shouldBeOpen(this.minecartrevolution$viewers > 0);
        this.minecartrevolution$chestLidController.tickLid();
    }
}
