package com.tierzero.stacksonstacks.api;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import com.google.common.collect.Lists;
import com.tierzero.stacksonstacks.SoS;
import com.tierzero.stacksonstacks.compat.RotaryCompat;
import com.tierzero.stacksonstacks.util.ClientUtils;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class IngotFinder {
	private static String[] invalidNames = new String[] { "ingotDouble", "ingotTriple", "ingotQuad", "ingotQuin" };

	public static void registerIngots() {		
		FMLControlledNamespacedRegistry<Item> itemRegistry = GameData.getItemRegistry();
		Set<String> registeredItemNames = itemRegistry.getKeys();

		List<String> validRegisteredNames = getValidNames(Lists.newArrayList(registeredItemNames));
		List<String> validOredictNames = getValidNames(Lists.newArrayList(OreDictionary.getOreNames()));
			
		for(String validName : validOredictNames) {
			for(ItemStack stack : OreDictionary.getOres(validName)) {
				IngotRegistry.registerIngot(stack, validName);
			}
		}
		
		for(String validName : validRegisteredNames) {
			ItemStack stack = new ItemStack(itemRegistry.getObject(validName));
			IngotRegistry.registerIngot(stack, validName);
		}
	}
	
	private static List<String> getValidNames(List<String> names) {
		List<String> validNames = new ArrayList<String>();
		
		for (String name : names) {
			if (!name.isEmpty() && name.contains("ingot")) {

				boolean invalid = false;
				for (String invalidName : invalidNames) {
					if (name.startsWith(invalidName)) {
						invalid = true;
					}
				}

				if(!invalid) {
					validNames.add(name);
				}
				
			}
		}
		
		return validNames;
	}

	@SideOnly(Side.CLIENT)
	public static void registerIngotColors() {
		for (Ingot ingot : IngotRegistry.getRegisteredIngots()) {
			ingot.setColor(getColor(ingot.getIngotStack()));
		}
		if (SoS.config.goldAltTexture)
			IngotRegistry.getIngot(Items.gold_ingot, 0).setIcon(new ItemStack(Blocks.gold_block).getIconIndex());
	}

	@SideOnly(Side.CLIENT)
	public static int getStackColour(ItemStack stack, int pass) {
		return stack.getItem().getColorFromItemStack(stack, pass);
	}

	@SideOnly(Side.CLIENT)
	public static Color getColor(ItemStack stack) {
		List<Color> colors = new ArrayList<Color>();
		try {
			BufferedImage texture = ImageIO.read(ClientUtils.getIconResource(stack).getInputStream());
			Color textureColour = getAverageColor(texture);
			colors.add(textureColour);

			for (int pass = 0; pass < stack.getItem().getRenderPasses(stack.getItemDamage()); pass++) {

				int stackColor = getStackColour(stack, pass);

				if (stackColor != 16777215) {
					colors.add(new Color(stackColor));
					colors.remove(textureColour);
				}
			}
		} catch (Exception e) {
		}

		
		if (Loader.isModLoaded("gregapi")) {
			try {
				Class<?> clazz = Class.forName("gregapi.item.prefixitem.PrefixItem");
				Class<?> itemClazz = stack.getItem().getClass();
				if (clazz.isAssignableFrom(itemClazz)) {
					
					for (int pass = 0; pass < stack.getItem().getRenderPasses(stack.getItemDamage()); pass++) {

						int stackColor = getStackColour(stack, pass);

						if (stackColor != 16777215) {
							colors.add(new Color(stackColor));
						}
					}

				}
			} catch (ClassNotFoundException e) {
			}
		}
		if (Loader.isModLoaded("gregtech")) {
			try {
				Class<?> cls = Class.forName("gregtech.api.items.GT_MetaGenerated_Item");
				Class<?> itemCls = stack.getItem().getClass();
				if (cls.isAssignableFrom(itemCls)) {
					Method m = itemCls.getMethod("getRGBa", ItemStack.class);
					short[] rgba = (short[]) m.invoke(stack.getItem(), stack);
					Color c = new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
					colors.clear();
					colors.add(c);

				}
			} catch (Exception e) {
			}
		}
		if (RotaryCompat.INSTANCE.isEnabled()) {
			try {
				Class<?> clazz = Class.forName("Reika.DragonAPI.ModRegistry.ModOreList");
				if (clazz != null) {

					Method m = clazz.getMethod("getModOreFromOre", ItemStack.class);

				}
			} catch (Exception e) {

			}
		}
		
		
		
		float red = 0;
		float green = 0;
		float blue = 0;
		for (Color c : colors) {
			red += c.getRed();
			green += c.getGreen();
			blue += c.getBlue();
		}
		float count = colors.size();
		
		Color ingotColor = new Color((int) (red / count), (int) (green / count), (int) (blue / count));

		
		if(ingotColor.getRed() == 0 && ingotColor.getBlue() == 0 && ingotColor.getRed() == 0) {
			//Change it to look like iron
			ingotColor = new Color(156, 156, 156);
		}

		return ingotColor;
	}

	@SideOnly(Side.CLIENT)
	private static Color getAverageColor(BufferedImage image) {
		int red = 0;
		int green = 0;
		int blue = 0;
		int count = 0;
		int offset = 4;
		for (int i = offset; i < image.getWidth() - offset; i++) {
			for (int j = offset; j < image.getHeight() - offset; j++) {
				Color imageColor = new Color(image.getRGB(i, j));

				if (imageColor.getAlpha() != 255
						|| imageColor.getRed() <= 10 && imageColor.getBlue() <= 10 && imageColor.getGreen() <= 10) {
					continue;
				}

				red += imageColor.getRed();
				green += imageColor.getGreen();
				blue += imageColor.getBlue();
				count++;
			}
		}

		return new Color((int) (red / count), (int) (green / count), (int) (blue / count));
	}

}