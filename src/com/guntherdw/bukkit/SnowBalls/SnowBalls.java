/*
 * Copyright (c) 2012 GuntherDW
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.guntherdw.bukkit.SnowBalls;

import com.sk89q.worldedit.blocks.ItemType;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author GuntherDW
 */
public class SnowBalls extends JavaPlugin {

    private final SnowBallsBlock blockListener = new SnowBallsBlock(this);

    private final SnowBallsPluginMessageListener pluginMessageListener = new SnowBallsPluginMessageListener(this);
    private final ZonesHelper zonesHelper = new ZonesHelper(this);
    private final JsonHelper jsonHelper = new JsonHelper(this);

    protected static final Logger log = Logger.getLogger("Minecraft");
    protected String pluginMessageChannel = "SnowBalls";
    protected static Set<ShapelessRecipe> shapelessRecipes;
    protected static Set<ShapedRecipe> shapedRecipes;
    protected static Map<Integer, Integer> extraids;
    private PluginDescriptionFile pdffile;
    protected boolean enableInfiniteLava = false;
    protected boolean enableMaxStack = false;
    protected boolean leavesLoot = false;
    protected boolean bookshelvesdrop = false;
    protected boolean icedrop = false;
    protected int defaultSearchRange = 20;
    protected int maximumSearchRange = 100;
    private boolean inited = false;

    protected boolean useZones = false;

    protected Map<Integer, Integer> origstacksizes = new HashMap<Integer, Integer>();

    @SuppressWarnings("unchecked")
    private void parseConfig() {

        /* Dirty hack for /snowballs reload */

        if (inited) {
            SnowBalls.shapelessRecipes.clear();
            SnowBalls.shapedRecipes.clear();
            this.getServer().resetRecipes();
            try {
                this.getConfig().load(new File(this.getDataFolder(), "config.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        } else inited = true;

        this.enableInfiniteLava = this.getConfig().getBoolean("hacks.inflava", false);
        if (this.enableInfiniteLava) log.info("[SnowBalls] Infinite lava hack enabled!");
        this.enableMaxStack = this.getConfig().getBoolean("hacks.maxstack", false);
        if (this.enableMaxStack) log.info("[SnowBalls] Custom max stack size enabled!");

        this.leavesLoot = this.getConfig().getBoolean("block.cacaobeanbonus", false);
        if (this.leavesLoot) log.info("[SnowBalls] Extra birch leaves loot bonus enabled!");
        this.bookshelvesdrop = this.getConfig().getBoolean("block.dropbookshelves", false);
        if (this.bookshelvesdrop) log.info("[SnowBalls] Book shelves dropping enabled!");
        this.icedrop = this.getConfig().getBoolean("block.dropice", false);
        if (this.icedrop) log.info("[SnowBalls] Ice dropping enabled!");
        this.defaultSearchRange = this.getConfig().getInt("search.default-range", 20);
        this.maximumSearchRange = this.getConfig().getInt("search.maximum-range", 100);
        log.info("[SnowBalls] Default search range : "+this.defaultSearchRange+"!");
        log.info("[SnowBalls] Maximum search range : "+this.maximumSearchRange+"!");

        this.useZones = zonesHelper.checkForZones();
        if(useZones) log.info("[SnowBalls] Using Zones to check for block permissions!");

        ConfigurationSection section = this.getConfig().getConfigurationSection("recipes.shapeless");
        if (section != null) {
            Set<String> shapelesslines = section.getKeys(false);

            for (String s1 : shapelesslines) {
                try {
                    List<Integer> result = this.getConfig().getIntegerList("recipes.shapeless." + s1 + ".result");
                    ShapelessRecipe sl = new ShapelessRecipe(new ItemStack(result.get(0), result.get(2), result.get(1).shortValue()));
                    List<?> ingredients = this.getConfig().getList("recipes.shapeless." + s1 + ".ingredients", null);
                    if (ingredients != null) {

                        for (Object iline : ingredients) {
                            if (iline instanceof List) {
                                List<Integer> l = (List<Integer>) iline;
                                if (l.size() > 2)
                                    sl.addIngredient(l.get(2), Material.getMaterial(l.get(0)), l.get(1));
                                else
                                    sl.addIngredient(Material.getMaterial(l.get(0)), l.get(1));
                            }
                        }
                    }
                    // this.getServer().addRecipe(sl);
                    log.info("[SnowBalls] Added shapeless recipe with name '" + s1 + "'!");
                    SnowBalls.shapelessRecipes.add(sl);

                } catch (NullPointerException ex) {
                    log.warning("Error with recipe '" + s1 + "'!");
                }
            }
        }

        section = this.getConfig().getConfigurationSection("recipes.shaped");

        if (section != null) {
            Set<String> shapedrecipelines = section.getKeys(false);

            for (String s1 : shapedrecipelines) {
                try {
                    List<Integer> result = this.getConfig().getIntegerList("recipes.shaped." + s1 + ".result");// , null);
                    ShapedRecipe sr = new ShapedRecipe(new ItemStack(result.get(0), result.get(2), result.get(1).shortValue()));
                    String[] shape = this.getConfig().getStringList("recipes.shaped." + s1 + ".shape"/* , null*/).toArray(new String[0]);
                    sr.shape(shape);


                    Set<String> shapedRecipeIngredients = this.getConfig().getConfigurationSection("recipes.shaped."+s1+".ingredients").getKeys(false);
                    for (String charac : shapedRecipeIngredients) {
                        List<Integer> l = this.getConfig().getIntegerList("recipes.shaped." + s1 + ".ingredients." + charac);//, null);
                        Material mat = Material.getMaterial(l.get(0));
                        sr.setIngredient(charac.toCharArray()[0], mat, l.get(1));
                    }
                    log.info("[SnowBalls] Added shaped recipe with name '" + s1 + "'!");
                    SnowBalls.shapedRecipes.add(sr);
                } catch (NullPointerException ex) {
                    log.warning("Error with recipe '" + s1 + "'!");
                }
            }
        }

        // if(enableInfiniteLava)

        if (enableMaxStack) {
            log.info("[SnowBalls] Setting max stack sizes!");
            this.setMaxStack();
        }
    }

    @Override
    public void onDisable() {
        log.info("[" + pdffile.getName() + "] " + pdffile.getName() + " version " + pdffile.getVersion() + " shutting down!");
        log.info("[" + pdffile.getName() + "] Resetting recipes to default");
        this.getServer().resetRecipes();
    }

    public void addRecipes() {
        for (ShapelessRecipe recipe : SnowBalls.shapelessRecipes) {
            this.getServer().addRecipe(recipe);
        }
        for (ShapedRecipe recipe : SnowBalls.shapedRecipes) {
            this.getServer().addRecipe(recipe);
        }
    }

    @Override
    public void onEnable() {
        // cuiPlayers = new HashMap<Player, Boolean>();
        pdffile = this.getDescription();
        this.getServer().getPluginManager().registerEvents(blockListener, this);

        this.getServer().getMessenger().registerIncomingPluginChannel(this, pluginMessageChannel, pluginMessageListener);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, pluginMessageChannel);

        shapelessRecipes = new HashSet<ShapelessRecipe>();
        shapedRecipes = new HashSet<ShapedRecipe>();
        this.parseConfig();
        this.addRecipes();

        this.getCommand("search").setExecutor(new SearchCommand(this));

        log.info("[" + pdffile.getName() + "] " + pdffile.getName() + " version " + pdffile.getVersion() + " loaded!");
    }

    public void setMaxStack() {
        extraids = new TreeMap<Integer, Integer>();

        // Walk the stack to find which version we're currently using.
        String packageName = getServer().getClass().getPackage().getName();
        String CBVersion = packageName.substring(packageName.lastIndexOf(".") + 1);
        getLogger().info("Found Craftbukkit version " + CBVersion);

        for (Material mat : Material.values()) {
            int matid = mat.getId();
            if(matid == 0 || matid > 4096) continue;

            if (mat.getMaxStackSize() != 64
                && !ItemType.shouldNotStack(matid)) {
                if (!origstacksizes.containsKey(matid)) { // Only store it if we're going to change it!
                    origstacksizes.put(matid, mat.getMaxStackSize());
                }
                //

                System.out.println("Setting maxStackSize of ItemID : "+matid);
                ItemStack stack = new ItemStack(matid);

                try {

                    Class cls = Class.forName("org.bukkit.craftbukkit."+CBVersion+".inventory.CraftItemStack");
                    Method m = cls.getDeclaredMethod("asCraftCopy", ItemStack.class);
                    Method m2 = cls.getDeclaredMethod("setMaxStackSize", int.class);
                    Object cis = m.invoke(null, stack);
                    m2.invoke(cis, 64);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                extraids.put(matid, 64);
            }
        }
    }

    public List<String> getRecipes() {
        List<String> recipes = new ArrayList<String>();
        String s = "";
        ItemStack isr;
        List<ItemStack> ist;
        if (shapelessRecipes != null && shapelessRecipes.size() > 0) {
            for (ShapelessRecipe sr : shapelessRecipes) {
                isr = sr.getResult();
                ist = sr.getIngredientList();
                s = /* CUIPattern + */ "0:" + isr.getTypeId() + ";" + isr.getDurability() + ";" + isr.getAmount();
                Map<ItemStack, Integer> matList = new HashMap<ItemStack, Integer>();
                for (ItemStack md : ist) {
                    if (!matList.containsKey(md))
                        matList.put(md, 1);
                    else
                        matList.put(md, matList.get(md) + 1);
                }

                for (Map.Entry<ItemStack, Integer> md : matList.entrySet()) {
                    // int amount = matList.get(md);
                    // if (!SUIv2)
                    s += "|" + md.getKey().getTypeId() + ";" + md.getKey().getData().getData() + ";" + md.getValue();
                    /* else {
                        boolean showAmount = (amount > 1);
                        boolean showData = md.getData() != 0 || showAmount;
                        s += "|" + md.getItemTypeId() + (showData ? ";" + md.getData() : "") + (showAmount ? ";" + amount : "");
                    } */
                }

                recipes.add(s);
            }
        }

        for (ShapedRecipe shr : shapedRecipes) {
            isr = shr.getResult();
            String[] shape = shr.getShape();
            s = /* CUIPattern + */ "1:" + isr.getTypeId() + ";" + isr.getDurability() + ";" + isr.getAmount();
            /* while(shape.size()<3) {
                shape.add("");
            } */
            for (String sha : shape) {
                while (sha.length() < 3) {
                    sha += " ";
                }
                // System.out.println("shape : "+sha.replace(' ', '.'));
                for (char a : sha.toCharArray()) {
                    if (a == ' ') {
                        s += "|";
                    } else {
                        ItemStack md = shr.getIngredientMap().get(a);
                        s += "|" + md.getTypeId() + ";" + md.getData().getData() + ";" + 1;
                    }
                }
            }
            recipes.add(s);
        }
        int x = 0;


        s =/* CUIPattern + */ "i";
        for (Integer i : extraids.keySet()) {
            s += "|" + i + ";" + extraids.get(i);
            x++;
        }
        if (x > 0)
            recipes.add(s);


        return recipes;
    }

    public JsonHelper getJsonHelper() {
        return jsonHelper;
    }

    public ZonesHelper getZonesHelper() {
        return zonesHelper;
    }

    public void sendRecipes(Player p) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        List<String> recipes = getRecipes();

        bos.write((byte) 10); // Inventories
        for (String recipeLine : recipes) {
            try {
                bos.write(recipeLine.getBytes("UTF-8"));
                bos.write((byte) 0);
            } catch (IOException ex) {
                ;
            }
        }
        byte[] result = bos.toByteArray();
        if(result.length>0)
            p.sendPluginMessage(this, pluginMessageChannel, result);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("snowballs")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("sendrecipes")) {
                    this.sendRecipes((Player) sender);
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (sender.hasPermission("snowballs.reloadconfig")) {
                        sender.sendMessage(ChatColor.YELLOW + "Reloading config...");
                        this.getServer().resetRecipes();
                        this.parseConfig();
                        this.addRecipes();
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "You don't have permission for this!");
                    }
                } else {
                    sender.sendMessage(command.getUsage());
                }
            }
        }
        return true;
    }
}