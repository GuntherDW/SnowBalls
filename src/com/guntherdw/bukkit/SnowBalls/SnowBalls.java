package com.guntherdw.bukkit.SnowBalls;

import com.sk89q.worldedit.blocks.ItemType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author GuntherDW
 */
public class SnowBalls extends JavaPlugin {

    private final SnowBallsBlock blockListener = new SnowBallsBlock(this);
    private final SnowBallsPlayer playerListener = new SnowBallsPlayer(this);
    // private final SnowBallsInventory inventoryListener = new SnowBallsInventory(this);
    private final SnowBallsPluginMessageListener pluginMessageListener = new SnowBallsPluginMessageListener(this);
    private final JsonHelper jsonHelper = new JsonHelper(this);
    // private Map<Player, Boolean> cuiPlayers;
    // private Map<Player, Boolean> newCUI;
    // protected String CUIPattern = "§7§3§3§7";
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
    private boolean inited = false;
    protected Map<Integer, Integer> origstacksizes = new HashMap<Integer, Integer>();

    @SuppressWarnings("unchecked")
    private void parseConfig() {

        /* Dirty hack for /snowballs reload */

        if (inited) {
            this.shapelessRecipes.clear();
            this.shapedRecipes.clear();
            this.getConfiguration().load();
        } else inited = true;

        this.enableInfiniteLava = this.getConfiguration().getBoolean("hacks.inflava", false);
        if (this.enableInfiniteLava) log.info("[SnowBalls] Infinite lava hack enabled!");
        this.enableMaxStack = this.getConfiguration().getBoolean("hacks.maxstack", false);
        if (this.enableMaxStack) log.info("[SnowBalls] Custom max stack size enabled!");

        this.leavesLoot = this.getConfiguration().getBoolean("block.cacaobeanbonus", false);
        if (this.leavesLoot) log.info("[SnowBalls] Extra birch leaves loot bonus enabled!");
        this.bookshelvesdrop = this.getConfiguration().getBoolean("block.dropbookshelves", false);
        if (this.bookshelvesdrop) log.info("[SnowBalls] Book shelves dropping enabled!");
        this.icedrop = this.getConfiguration().getBoolean("block.dropice", false);
        if (this.icedrop) log.info("[SnowBalls] Ice dropping enabled!");

        List<String> shapelesslines = this.getConfiguration().getKeys("recipes.shapeless");
        for (String s1 : shapelesslines) {
            try {
                List<Integer> result = this.getConfiguration().getIntList("recipes.shapeless." + s1 + ".result", null);
                ShapelessRecipe sl = new ShapelessRecipe(new ItemStack(result.get(0), result.get(2), result.get(1).shortValue()));
                List<Object> ingredients = this.getConfiguration().getList("recipes.shapeless." + s1 + ".ingredients");
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
                shapelessRecipes.add(sl);

            } catch (NullPointerException ex) {
                System.out.println("Error with recipe '" + s1 + "'!");
            }
        }

        List<String> shapedrecipelines = this.getConfiguration().getKeys("recipes.shaped");
        for (String s1 : shapedrecipelines) {
            try {
                List<Integer> result = this.getConfiguration().getIntList("recipes.shaped." + s1 + ".result", null);
                ShapedRecipe sr = new ShapedRecipe(new ItemStack(result.get(0), result.get(2), result.get(1).shortValue()));
                // sr.
                String[] shape = this.getConfiguration().getStringList("recipes.shaped." + s1 + ".shape", null).toArray(new String[0]);
                sr.shape(shape);
                for (String charac : this.getConfiguration().getKeys("recipes.shaped." + s1 + ".ingredients")) {
                    List<Integer> l = this.getConfiguration().getIntList("recipes.shaped." + s1 + ".ingredients." + charac, null);
                    Material mat = Material.getMaterial(l.get(0));
                    sr.setIngredient(charac.toCharArray()[0], mat, l.get(1));
                }
                log.info("[SnowBalls] Added shaped recipe with name '" + s1 + "'!");
                shapedRecipes.add(sr);
            } catch (NullPointerException ex) {
                System.out.println("Error with recipe '" + s1 + "'!");
            }
        }

        // if(enableInfiniteLava)
        if (enableMaxStack) this.setMaxStack();
    }

    public void onDisable() {
        log.info("[" + pdffile.getName() + "] " + pdffile.getName() + " version " + pdffile.getVersion() + " shutting down!");
    }

    public void addRecipes() {
        for (ShapelessRecipe recipe : shapelessRecipes) {
            this.getServer().addRecipe(recipe);
        }
        for (ShapedRecipe recipe : shapedRecipes) {
            this.getServer().addRecipe(recipe);
        }
    }

    public void onEnable() {
        // cuiPlayers = new HashMap<Player, Boolean>();
        pdffile = this.getDescription();

        // this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Monitor, this);
        this.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Monitor, this);
        this.getServer().getPluginManager().registerEvent(Event.Type.LEAVES_DECAY, blockListener, Event.Priority.Monitor, this);
        this.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_FROMTO, blockListener, Event.Priority.Lowest, this);

        this.getServer().getMessenger().registerIncomingPluginChannel(this, pluginMessageChannel, pluginMessageListener);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, pluginMessageChannel);


        shapelessRecipes = new HashSet<ShapelessRecipe>();
        shapedRecipes = new HashSet<ShapedRecipe>();
        this.parseConfig();
        this.addRecipes();

        log.info("[" + pdffile.getName() + "] " + pdffile.getName() + " version " + pdffile.getVersion() + " loaded!");
    }

    public void setMaxStack() {
        extraids = new TreeMap<Integer, Integer>();

        for (Material mat : Material.values()) {
            int matid = mat.getId();

            if (mat.getMaxStackSize() != 64
                && !ItemType.shouldNotStack(matid)) {
                if (!origstacksizes.containsKey(matid)) { // Only store it if we're going to change it!
                    origstacksizes.put(matid, mat.getMaxStackSize());
                }
                /* ItemStack stack = new ItemStack(matid);
                stack */
                CraftItemStack cis = new CraftItemStack(matid);
                cis.setMaxStackSize(64);

                extraids.put(matid, 64);
                // stack.setMaxStackSize(64);
            }
        }
    }


    public List<String> getRecipes() {
        List<String> recipes = new ArrayList<String>();
        String s = "";
        ItemStack isr;
        List<MaterialData> ist;
        if (shapelessRecipes != null && shapelessRecipes.size() > 0) {
            for (ShapelessRecipe sr : shapelessRecipes) {
                isr = sr.getResult();
                ist = sr.getIngredientList();
                s = /* CUIPattern + */ "0:" + isr.getTypeId() + ";" + isr.getDurability() + ";" + isr.getAmount();
                Map<MaterialData, Integer> matList = new HashMap<MaterialData, Integer>();
                for (MaterialData md : ist) {
                    if (!matList.containsKey(md))
                        matList.put(md, 1);
                    else
                        matList.put(md, matList.get(md) + 1);
                }

                for (MaterialData md : matList.keySet()) {
                    int amount = matList.get(md);
                    // if (!SUIv2)
                    s += "|" + md.getItemTypeId() + ";" + md.getData() + ";" + amount;
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
                        MaterialData md = shr.getIngredientMap().get(a);
                        s += "|" + md.getItemTypeId() + ";" + md.getData() + ";" + 1;
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

    public void sendRecipes(Player p) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        List<String> recipes = getRecipes();

        int i = 0;
        for (String recipeLine : recipes) {
            try {
                bos.write(recipeLine.getBytes("UTF-8"));
                bos.write((byte) 0);
                i++;
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
                /* if (args[0].equalsIgnoreCase("client") && sender instanceof Player) {
                    Player player = (Player) sender;
                    boolean SUIv25 = args.length > 1 && args[1].equalsIgnoreCase("SUIv25");

                    if (!onList) {
                        log.info("[" + pdffile.getName() + "] Adding " + player.getName() + " to the SUI list...");
                        cuiPlayers.put((Player) sender, SUIv25);
                    } else {
                        boolean legacy = !cuiPlayers.get(player);
                        if (legacy && SUIv25) {
                            log.info("[" + pdffile.getName() + "] Updating " + player.getName() + "'s SUI status to SUIv2");
                            cuiPlayers.put((Player) sender, SUIv25);
                        } else {
                            log.info("[" + pdffile.getName() + "] " + player.getName() + " already is assigned to the SUI list...");
                        }
                    }

                    this.sendRecipes((Player) sender);
                } else */
                if (args[0].equalsIgnoreCase("sendrecipes")) {
                    this.sendRecipes((Player) sender);
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (sender.isOp() || sender.hasPermission("snowballs.reloadconfig")) {
                        sender.sendMessage(ChatColor.YELLOW + "Reloading config...");
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