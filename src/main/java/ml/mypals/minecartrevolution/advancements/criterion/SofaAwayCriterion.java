package ml.mypals.minecartrevolution.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ml.mypals.minecartrevolution.registeries.MRModCriteria;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class SofaAwayCriterion extends SimpleCriterionTrigger<SofaAwayCriterion.TriggerInstance> {

    @Override
    public @NonNull Codec<SofaAwayCriterion.TriggerInstance> codec() {
        return SofaAwayCriterion.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, _ -> true);
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<SofaAwayCriterion.TriggerInstance> CODEC = RecordCodecBuilder.create(
                i -> i.group(
                                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(SofaAwayCriterion.TriggerInstance::player)
                        )
                        .apply(i, SofaAwayCriterion.TriggerInstance::new)
        );

        @Override
        public @NonNull Optional<ContextAwarePredicate> player() {
            return player;
        }

        public static Criterion<TriggerInstance> sofaAway() {
            return MRModCriteria.SOFA_AWAY.get().createCriterion(new SofaAwayCriterion.TriggerInstance(Optional.empty()));
        }
    }
}