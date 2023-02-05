package shadows.apotheosis.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyedBase;
import shadows.placebo.json.WeightedJsonReloadListener.ILuckyWeighted;

/**
 * Util class to contain the full equipment for an entity.
 * @author Shadows
 *
 */
public class GearSet extends TypeKeyedBase<GearSet> implements ILuckyWeighted {

	protected final int weight;
	protected final float quality;
	protected final List<WeightedItemStack> mainhands;
	protected final List<WeightedItemStack> offhands;
	protected final List<WeightedItemStack> boots;
	protected final List<WeightedItemStack> leggings;
	protected final List<WeightedItemStack> chestplates;
	protected final List<WeightedItemStack> helmets;
	protected final List<String> tags = new ArrayList<>();

	public GearSet(int weight, float quality, List<WeightedItemStack> mainhands, List<WeightedItemStack> offhands, List<WeightedItemStack> boots, List<WeightedItemStack> leggings, List<WeightedItemStack> chestplates, List<WeightedItemStack> helmets) {
		this.weight = weight;
		this.quality = quality;
		this.mainhands = mainhands;
		this.offhands = offhands;
		this.boots = boots;
		this.leggings = leggings;
		this.chestplates = chestplates;
		this.helmets = helmets;
	}

	@Override
	public int getWeight() {
		return this.weight;
	}

	@Override
	public float getQuality() {
		return this.quality;
	}

	/**
	 * Makes the entity wear this armor set.  Returns the entity for convenience.
	 */
	public LivingEntity apply(LivingEntity entity) {
		entity.setItemSlot(EquipmentSlot.MAINHAND, getRandomStack(this.mainhands, entity.random));
		entity.setItemSlot(EquipmentSlot.OFFHAND, getRandomStack(this.offhands, entity.random));
		entity.setItemSlot(EquipmentSlot.FEET, getRandomStack(this.boots, entity.random));
		entity.setItemSlot(EquipmentSlot.LEGS, getRandomStack(this.leggings, entity.random));
		entity.setItemSlot(EquipmentSlot.CHEST, getRandomStack(this.chestplates, entity.random));
		entity.setItemSlot(EquipmentSlot.HEAD, getRandomStack(this.helmets, entity.random));
		return entity;
	}

	/**
	 * Returns a copy of a random itemstack in this list of stacks.
	 */
	public static ItemStack getRandomStack(List<WeightedItemStack> stacks, RandomSource random) {
		if (stacks.isEmpty()) return ItemStack.EMPTY;
		return WeightedRandom.getRandomItem(random, stacks).get().getStack().copy();
	}

	public static class WeightedItemStack extends Weighted {

		final ItemStack stack;

		public WeightedItemStack(ItemStack stack, int weight) {
			super(weight);
			this.stack = stack;
		}

		public ItemStack getStack() {
			return this.stack;
		}

		@Override
		public String toString() {
			return "Stack: " + this.stack.toString() + " @ Weight: " + this.weight;
		}
	}

	public static class SetPredicate implements Predicate<GearSet> {

		protected final String key;
		protected final Predicate<GearSet> internal;

		public SetPredicate(String key) {
			this.key = key;
			if (key.startsWith("#")) {
				String tag = key.substring(1);
				this.internal = t -> t.tags.contains(tag);
			} else {
				ResourceLocation id = new ResourceLocation(key);
				this.internal = t -> t.id.equals(id);
			}
		}

		@Override
		public boolean test(GearSet t) {
			return this.internal.test(t);
		}

	}

	public static class SetPredicateAdapter implements JsonDeserializer<SetPredicate>, JsonSerializer<SetPredicate> {

		@Override
		public JsonElement serialize(SetPredicate src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.key);
		}

		@Override
		public SetPredicate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return new SetPredicate(json.getAsString());
		}

	}
}
