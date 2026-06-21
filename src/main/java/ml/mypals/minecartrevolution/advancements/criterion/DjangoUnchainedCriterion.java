package ml.mypals.minecartrevolution.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ml.mypals.minecartrevolution.registeries.MRModCriteria;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class DjangoUnchainedCriterion extends SimpleCriterionTrigger<DjangoUnchainedCriterion.TriggerInstance> {
    @Override
    public @NotNull Codec<DjangoUnchainedCriterion.TriggerInstance> codec() {
        return DjangoUnchainedCriterion.TriggerInstance.CODEC;
    }
    public void trigger(ServerPlayer player) {
        this.trigger(player, _ -> true);
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<DjangoUnchainedCriterion.TriggerInstance> CODEC = RecordCodecBuilder.create(
                i -> i.group(
                                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(DjangoUnchainedCriterion.TriggerInstance::player)
                        )
                        .apply(i, DjangoUnchainedCriterion.TriggerInstance::new)
        );

        @Override
        public @NonNull Optional<ContextAwarePredicate> player() {
            return player;
        }

        public static Criterion<DjangoUnchainedCriterion.TriggerInstance> trigger() {
            return MRModCriteria.DJANGO_UNCHAINED.get().createCriterion(new DjangoUnchainedCriterion.TriggerInstance(Optional.empty()));
        }
    }
}