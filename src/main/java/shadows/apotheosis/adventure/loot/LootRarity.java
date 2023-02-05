package shadows.apotheosis.adventure.loot;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.adventure.AdventureModule;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootRarity.LootRule;
import shadows.placebo.codec.EnumCodec;
import shadows.placebo.color.GradientColor;
import shadows.placebo.json.WeightedJsonReloadListener.ILuckyWeighted;

public record LootRarity(int defaultWeight, String id, TextColor color, List<LootRule> rules, int ordinal) implements ILuckyWeighted, Comparable<LootRarity> {

	public static final List<LootRarity> LIST;
	public static final Map<String, LootRarity> BY_ID;
	public static final Map<LootRarity, float[]> WEIGHTS = new HashMap<>();
	//Formatter::off
	public static final Codec<LootRarity> DIRECT_CODEC = RecordCodecBuilder.create(inst -> 
		inst.group(
			Codec.INT.fieldOf("default_weight").forGetter(LootRarity::defaultWeight),
			Codec.STRING.fieldOf("id").forGetter(LootRarity::id),
			TextColor.CODEC.fieldOf("color").forGetter(LootRarity::color),
			new ListCodec<>(LootRule.CODEC).fieldOf("rules").forGetter(LootRarity::rules),
			Codec.INT.fieldOf("ordinal").forGetter(LootRarity::ordinal))
			.apply(inst, LootRarity::new)
		);
	//Formatter::on

	public static final Codec<LootRarity> DISPATCH_CODEC = ExtraCodecs.stringResolverCodec(LootRarity::id, LootRarity::byId);

	//Formatter::off
	public static final LootRarity COMMON = new LootRarity(400, "common", 0x808080, ImmutableList.of(
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 0.25F)
	));

	public static final LootRarity UNCOMMON = new LootRarity(320, "uncommon", 0x33FF33, ImmutableList.of(
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 0.45F, new LootRule(AffixType.ABILITY, 0.25F)),
			new LootRule(AffixType.SOCKET, 0.45F)
	));

	public static final LootRarity RARE = new LootRarity(150, "rare", 0x5555FF, ImmutableList.of(
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.ABILITY, 0.25F)),
			new LootRule(AffixType.ABILITY, 1),
			new LootRule(AffixType.ABILITY, 0.33F),
			new LootRule(AffixType.SOCKET, 0.65F),
			new LootRule(AffixType.SOCKET, 0.45F),
			new LootRule(AffixType.DURABILITY, 0.1F)
	));

	public static final LootRarity EPIC = new LootRarity(90, "epic", 0xBB00BB, ImmutableList.of(
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.ABILITY, 0.45F)),
			new LootRule(AffixType.STAT, 0.5F, new LootRule(AffixType.ABILITY, 0.33F)),
			new LootRule(AffixType.ABILITY, 1),
			new LootRule(AffixType.ABILITY, 0.65F),
			new LootRule(AffixType.SOCKET, 0.85F),
			new LootRule(AffixType.SOCKET, 0.65F),
			new LootRule(AffixType.SOCKET, 0.45F),
			new LootRule(AffixType.DURABILITY, 0.3F)
	));

	public static final LootRarity MYTHIC = new LootRarity(40, "mythic", 0xED7014, ImmutableList.of(
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1),
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.ABILITY, 0.5F)),
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.ABILITY, 0.4F)),
			new LootRule(AffixType.ABILITY, 1),
			new LootRule(AffixType.ABILITY, 1),
			new LootRule(AffixType.ABILITY, 0.3F),
			new LootRule(AffixType.SOCKET, 1F),
			new LootRule(AffixType.SOCKET, 0.85F),
			new LootRule(AffixType.SOCKET, 0.65F),
			new LootRule(AffixType.DURABILITY, 0.5F)
	));

	public static final LootRarity ANCIENT = new LootRarity(0, "ancient", GradientColor.RAINBOW, ImmutableList.of(
			new LootRule(AffixType.ANCIENT, 1), 
			new LootRule(AffixType.STAT, 1), 
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.ABILITY, 0.7F)), 
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.ABILITY, 0.6F)), 
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.ABILITY, 0.5F)), 
			new LootRule(AffixType.STAT, 1, new LootRule(AffixType.ABILITY, 0.4F)), 
			new LootRule(AffixType.ABILITY, 1), new LootRule(AffixType.ABILITY, 1), 
			new LootRule(AffixType.ABILITY, 0.75F), 
			new LootRule(AffixType.ABILITY, 0.45F), 
			new LootRule(AffixType.SOCKET, 1F), 
			new LootRule(AffixType.SOCKET, 0.85F), 
			new LootRule(AffixType.SOCKET, 0.65F), 
			new LootRule(AffixType.SOCKET, 0.45F), 
			new LootRule(AffixType.SOCKET, 0.25F), 
			new LootRule(AffixType.DURABILITY, 0.75F)
	));

	//Formatter::on

	static {
		LIST = ImmutableList.of(COMMON, UNCOMMON, RARE, EPIC, MYTHIC, ANCIENT);
		BY_ID = ImmutableMap.copyOf(LIST.stream().collect(Collectors.toMap(LootRarity::id, Function.identity())));
	}

	private static int num = 0;

	private LootRarity(int defaultWeight, String id, TextColor color, List<LootRule> rules) {
		this(defaultWeight, id, color, rules, num++);
	}

	private LootRarity(int defaultWeight, String id, int color, List<LootRule> rules) {
		this(defaultWeight, id, TextColor.fromRgb(color), rules);
	}

	@Override
	public float getQuality() {
		return WEIGHTS.get(this)[1];
	}

	@Override
	public int getWeight() {
		return (int) WEIGHTS.get(this)[0];
	}

	/**
	 * Checks if this rarity is the same or worse than the passed rarity.
	 */
	public boolean isAtMost(LootRarity other) {
		return this.ordinal() <= other.ordinal();
	}

	/**
	 * Checks if this rarity is the same or better than the passed rarity.
	 */
	public boolean isAtLeast(LootRarity other) {
		return this.ordinal() >= other.ordinal();
	}

	/**
	 * Returns the minimum (worst) rarity between this and other.
	 */
	public LootRarity min(@Nullable LootRarity other) {
		if (other == null) return this;
		return this.ordinal <= other.ordinal ? this : other;
	}

	/**
	 * Returns the maximum (best) rarity between this and other.
	 */
	public LootRarity max(@Nullable LootRarity other) {
		if (other == null) return this;
		return this.ordinal >= other.ordinal ? this : other;
	}

	/**
	 * Clamps a loot rarity to within a min/max bound.
	 * @param lowerBound The minimum valid rarity
	 * @param upperBound The maximum valid rarity
	 * @return This, if this is within the bounds, or the min or max if it exceeded that bound.
	 */
	public LootRarity clamp(@Nullable LootRarity lowerBound, @Nullable LootRarity upperBound) {
		return this.min(upperBound).max(lowerBound);
	}

	public Component toComponent() {
		return Component.translatable("rarity.apoth." + this.id).withStyle(Style.EMPTY.withColor(this.color));
	}

	@Override
	public String toString() {
		return this.id;
	}

	public static LootRarity byId(String id) {
		return BY_ID.get(id.toLowerCase(Locale.ROOT));
	}

	public static Set<String> ids() {
		return BY_ID.keySet();
	}

	public static List<LootRarity> values() {
		return LIST;
	}

	public static LootRarity random(RandomSource rand, float luck) {
		return random(rand, luck, null, null);
	}

	public static LootRarity random(RandomSource rand, float luck, @Nullable Clamped item) {
		if (item == null) return random(rand, luck);
		return random(rand, luck, item.getMinRarity(), item.getMaxRarity());
	}

	public static LootRarity random(RandomSource rand, float luck, @Nullable LootRarity min, @Nullable LootRarity max) {
		List<Wrapper<LootRarity>> list = LIST.stream().filter(r -> r.clamp(min, max) == r).map(r -> r.<LootRarity>wrap(luck)).toList();
		return WeightedRandom.getRandomItem(rand, list).map(Wrapper::getData).get();
	}

	public static record LootRule(AffixType type, float chance, @Nullable LootRule backup) {

		//Formatter::off
		public static final Codec<LootRule> CODEC = RecordCodecBuilder.create(inst -> 
			inst.group(
				new EnumCodec<>(AffixType.class).fieldOf("type").forGetter(LootRule::type),
				Codec.FLOAT.fieldOf("chance").forGetter(LootRule::chance),
				ExtraCodecs.lazyInitializedCodec(() -> LootRule.CODEC).optionalFieldOf("backup", null).forGetter(LootRule::backup))
				.apply(inst, LootRule::new)
			);
		//Formatter::on

		private static Random jRand = new Random();

		public LootRule(AffixType type, float chance) {
			this(type, chance, null);
		}

		public void execute(ItemStack stack, LootRarity rarity, Set<Affix> currentAffixes, MutableInt sockets, RandomSource rand) {
			if (this.type == AffixType.DURABILITY) return;
			if (rand.nextFloat() <= this.chance) {
				if (this.type == AffixType.SOCKET) {
					sockets.add(1);
					return;
				}
				List<Affix> available = AffixHelper.byType(this.type).stream().filter(a -> a.canApplyTo(stack, rarity) && !currentAffixes.contains(a)).collect(Collectors.toList());
				if (available.size() == 0) {
					if (backup != null) backup.execute(stack, rarity, currentAffixes, sockets, rand);
					else AdventureModule.LOGGER.error("Failed to execute LootRule {}/{}/{}/{}!", ForgeRegistries.ITEMS.getKey(stack.getItem()), rarity.id(), this.type, this.chance);
					return;
				}
				jRand.setSeed(rand.nextLong());
				Collections.shuffle(available, jRand);
				currentAffixes.add(available.get(0));
			}
		}
	}

	public static class Serializer implements JsonSerializer<LootRarity>, JsonDeserializer<LootRarity> {

		@Override
		public LootRarity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return LootRarity.byId(json.getAsString());
		}

		@Override
		public JsonElement serialize(LootRarity src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.id);
		}

	}

	public static interface Clamped {

		public LootRarity getMinRarity();

		public LootRarity getMaxRarity();

		default LootRarity clamp(LootRarity rarity) {
			return rarity.clamp(getMinRarity(), getMaxRarity());
		}

	}

	@Override
	public int compareTo(LootRarity o) {
		return Integer.compare(this.ordinal, o.ordinal);
	}
}