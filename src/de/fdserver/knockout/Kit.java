package de.fdserver.knockout;

import de.myfdweb.minecraft.itemsapi.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public enum Kit {

    ENTERHAKEN(Material.FISHING_ROD, true), JETPACK(Material.GLASS_BOTTLE, false), FALLSCHIRM(Material.FEATHER, false),  ENDERPERLE(Material.ENDER_PEARL, false), GRANATE(Material.EGG, false);

    public static final ItemStack KNOCKBACK_STICK = new ItemBuilder(Material.STICK).setDisplayName("§6Knockback-Stick").addEnchantment(Enchantment.KNOCKBACK, 2).build();
    private final Material material;
    private final boolean infinite;

    Kit(Material material, boolean infinite) {
        this.material = material;
        this.infinite = infinite;
    }

    public String getName() {
        return name().charAt(0) + name().toLowerCase().substring(1);
    }

    public String getDisplayName() {
        return "§a" + getName();
    }

    public Material getMaterial() {
        return material;
    }

    public boolean isInfinite() {
        return infinite;
    }

    public void apply(Player p) {
        p.getInventory().clear();
        p.getInventory().addItem(KNOCKBACK_STICK);
        addKitItem(p);
        p.getInventory().setItem(8, new ItemBuilder(Material.PAPER).setDisplayName("§aMap-/Kitvote").build());
    }

    public void addKitItem(Player p) {
        ItemBuilder builder = new ItemBuilder(material).setDisplayName(getDisplayName()).setUnbreakable(true);
        if(material.equals(Material.BOW))
            builder.addEnchantment(Enchantment.ARROW_INFINITE, 5);
        p.getInventory().addItem(builder.build());
    }

    @Override
    public String toString() {
        return getName();
    }
}
