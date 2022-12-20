package land.face.arena.data;

import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.tier.Tier;
import org.bukkit.inventory.ItemStack;

public class LootReward {

  private RewardType type;
  private int amount;
  private double probability;
  private String dataString;
  private String dataStringTwo;

  public RewardType getType() {
    return type;
  }

  public void setType(RewardType type) {
    this.type = type;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public double getProbability() {
    return probability;
  }

  public void setProbability(double probability) {
    this.probability = probability;
  }

  public String getDataString() {
    return dataString;
  }

  public void setDataString(String dataString) {
    this.dataString = dataString;
  }

  public String getDataStringTwo() {
    return dataStringTwo;
  }

  public void setDataStringTwo(String dataStringTwo) {
    this.dataStringTwo = dataStringTwo;
  }

  public static ItemStack toItemStack(LootReward lootReward, double level) {
    switch (lootReward.type) {
      case TIER:
        ItemRarity rarity = LootPlugin.getInstance().getRarityManager()
            .getRarity(lootReward.dataStringTwo);
        Tier tier;
        if ("random".equals(lootReward.dataString)) {
          tier = LootPlugin.getInstance().getTierManager().getRandomTier();
        } else {
          tier = LootPlugin.getInstance().getTierManager().getTier(lootReward.dataString);
        }
        return LootPlugin.getInstance().getNewItemBuilder()
            .withTier(tier)
            .withRarity(rarity)
            .withLevel((int) level)
            .build().getStack();
      case GEM:
        if ("random".equals(lootReward.dataString)) {
          return LootPlugin.getInstance().getSocketGemManager()
              .getRandomSocketGemByLevel((int) level).toItemStack(1);
        }
        return LootPlugin.getInstance().getSocketGemManager()
            .getSocketGem(lootReward.getDataString()).toItemStack(lootReward.amount);
      case ENCHANT_TOME:
        if ("random".equals(lootReward.dataString)) {
          return LootPlugin.getInstance().getEnchantTomeManager().getRandomEnchantTome()
              .toItemStack(1);
        }
        return LootPlugin.getInstance().getEnchantTomeManager()
            .getEnchantTome(lootReward.getDataString()).toItemStack(lootReward.amount);
      case CUSTOM:
        return LootPlugin.getInstance().getCustomItemManager()
            .getCustomItem(lootReward.getDataString()).toItemStack(lootReward.amount);
      case SCROLL:
        if ("random".equals(lootReward.dataString)) {
          return LootPlugin.getInstance().getScrollManager()
              .buildItemStack(LootPlugin.getInstance().getScrollManager().getRandomScroll());
        }
        ItemStack stack2 = LootPlugin.getInstance().getScrollManager().buildItemStack(
            LootPlugin.getInstance().getScrollManager().getScroll(lootReward.dataString));
        stack2.setAmount(lootReward.amount);
        return stack2;
    }
    return null;
  }

  public enum RewardType {
    TIER,
    GEM,
    CUSTOM,
    ID_TOME,
    ENCHANT_TOME,
    SCROLL
  }
}
