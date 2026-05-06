package ml.mypals.minecartrevolution.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import java.util.Optional;

import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class MovingOnJukeboxCartCriterion extends SimpleCriterionTrigger<MovingOnJukeboxCartCriterion.@org.jetbrains.annotations.NotNull Conditions> {


    @Override
    public @NotNull Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public record Conditions(Optional<ContextAwarePredicate> playerPredicate, String entityTranslateId)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static Codec<Conditions> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                        Codec.STRING.fieldOf("entity").forGetter(Conditions::entity)
                ).apply(instance, Conditions::new));

        @Override
        public @NotNull Optional<ContextAwarePredicate> player() {
            return playerPredicate;
        }

        public String entity() {
            return entityTranslateId;
        }

        public boolean requirementsMet(Entity entity) {
            return Objects.equals(entity.getType().getDescriptionId(), entityTranslateId);
        }
    }

    public void trigger(ServerPlayer player, Entity entity) {
        trigger(player, conditions -> conditions.requirementsMet(entity));
    }
}
