package ml.mypals.minecartrevolution.factory;

import ml.mypals.minecartrevolution.entity.minecarts.AdvancedMinecartEntityTypes;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

public class MinecartRegistration {
    public static List<MinecartRegistration> registrationsSaved;
    private final Block block;
    private final EntityType<? extends AbstractMinecart> entityType;
    private final Item item;
    private final AdvancedMinecartEntityTypes.Type type;
    private final BiFunction<Level, Vec3, AbstractMinecart> factory;
    @Nullable
    private final EntityRenderer<?extends AbstractMinecart,  ? extends MinecartRenderState> rendererFactory;

    public MinecartRegistration(Block block, EntityType<? extends AbstractMinecart> entityType,
                                Item item, AdvancedMinecartEntityTypes.Type type,
                                BiFunction<Level, Vec3, AbstractMinecart> factory,
                                @Nullable EntityRenderer<? extends AbstractMinecart, ? extends MinecartRenderState> rendererFactory) {
        this.block = block;
        this.entityType = entityType;
        this.item = item;
        this.type = type;
        this.factory = factory;
        this.rendererFactory = rendererFactory;
    }
/*
    public static void registerAll(List<MinecartRegistration> registrations, boolean isClient) {
        registrationsSaved = registrations;
        ItemGroupEvents.modifyEntriesEvent(MINECART_REVOLUTION_ITEM_GROUP_KEY).register(entries -> {
            for (MinecartRegistration reg : registrations) {
                entries.accept(reg.item);
            }
        });
        for (MinecartRegistration reg : registrations) {



            //Registry.register(Registries.ENTITY_TYPE, EntityType.getId(reg.entityType), reg.entityType);

            Registry.register(BuiltInRegistries.ITEM, ResourceLocation.parse(reg.item.toString()), reg.item);

            factoryMap.put(reg.block, reg.factory);

            if(isClient) {
                EntityRendererRegistry.register((EntityType) reg.entityType,
                        (context) -> reg.rendererFactory == null?
                                new MinecartRenderer(context, ModelLayers.MINECART)
                                :reg.rendererFactory
                );
            }
        }
    }*/

}