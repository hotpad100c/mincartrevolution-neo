package ml.mypals.minecartrevolution.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.DynamicOps;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ml.mypals.minecartrevolution.MinecartRevolution;
import ml.mypals.minecartrevolution.inventory.LinkedContainer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.TagValueInput;

public class LinkedContainerManager {
  public static final LinkedContainerManager INSTANCE = new LinkedContainerManager();
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Type DATA_TYPE = new TypeToken<Map<String, String>>() {}.getType();
  private static final String FILE_NAME = "linked_chests.json";

  private final Map<String, LinkedContainer> containers = new HashMap<>();

  public void load(MinecraftServer server) {
    if (server == null) return;
    File file =
        server
            .getWorldPath(LevelResource.ROOT)
            .resolve(MinecartRevolution.MODID)
            .resolve(FILE_NAME)
            .toFile();
    if (!file.exists()) {
      return;
    }

    try (FileReader reader = new FileReader(file)) {
      Map<String, String> data = GSON.fromJson(reader, DATA_TYPE);
      if (data == null) return;

      HolderLookup.Provider provider = server.registryAccess();
      this.containers.clear();

      data.forEach(
          (key, snbt) -> {
            LinkedContainer container = get(key);
            try {
              CompoundTag nbt = TagParser.parseCompoundFully(snbt);
              var input = TagValueInput.create(ProblemReporter.DISCARDING, provider, nbt);
              input
                  .read("Items", ItemContainerContents.CODEC)
                  .ifPresent(
                      contents -> {
                        container.clearContent();
                        contents.copyInto(container.getItems());
                      });

              containers.put(key, container);
            } catch (Exception _) {
            }
          });
    } catch (Exception _) {
    }
  }

  public void save(MinecraftServer server) {
    if (server == null) return;

    HolderLookup.Provider provider = server.registryAccess();
    Map<String, String> data = new HashMap<>();

    containers.forEach(
        (key, container) -> {
          try {
            CompoundTag nbt = new CompoundTag();
            DynamicOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, provider);
            List<ItemStack> list = new ArrayList<>();
            for (int i = 0; i < container.getContainerSize(); i++) {
              list.add(container.getItem(i));
            }
            ItemContainerContents contents = ItemContainerContents.fromItems(list);
            ItemContainerContents.CODEC
                .encodeStart(ops, contents)
                .resultOrPartial(_ -> {})
                .ifPresent(tag -> nbt.put("Items", tag));

            data.put(key, nbt.toString());
          } catch (Exception _) {
          }
        });

    File file = server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME).toFile();
    try (FileWriter writer = new FileWriter(file)) {
      GSON.toJson(data, writer);
    } catch (Exception _) {
    }
  }

  public static LinkedContainer get(String key) {
    return INSTANCE.containers.computeIfAbsent(key, LinkedContainer::new);
  }
}
