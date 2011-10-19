package com.guntherdw.bukkit.SnowBalls;

import com.guntherdw.bukkit.tweakcraft.TweakcraftUtils;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author GuntherDW
 */
public class SnowBalls extends JavaPlugin {

    private final SnowBallsBlock blockListener = new SnowBallsBlock(this);
    private final SnowBallsPlayer playerListener = new SnowBallsPlayer(this);
    // private final SnowBallsInventory inventoryListener = new SnowBallsInventory(this);
    private final JsonHelper jsonHelper = new JsonHelper(this);
    private Map<Player, Boolean> cuiPlayers;
    // private Map<Player, Boolean> newCUI;
    protected String CUIPattern = "§7§3§3§7";
    protected static final Logger log = Logger.getLogger("Minecraft");
    protected static List<ShapelessRecipe> shapelessRecipes;
    protected static List<ShapedRecipe> shapedRecipes;
    protected static Map<Integer, Integer> extraids;
    protected TweakcraftUtils tcutils = null;
    protected WorldGuardPlugin wg = null;
    protected WorldEditPlugin we = null;
    private PluginDescriptionFile pdffile;
    protected boolean enableInfiniteLava = false;
    protected boolean enableMaxStack = false;
    protected boolean leavesLoot = false;
    protected boolean bookshelvesdrop = false;
    protected boolean icedrop = false;
    private boolean inited = false;
    protected Map<Integer, Integer> origstacksizes = new HashMap<Integer, Integer>();
/*
block:
   cacaobeanbonus: true
   dropbookshelves: true
   dropice: true
 */

    @SuppressWarnings("unchecked")
    private void parseConfig() {

        /* Dirty hack for /snowballs reload */

        if(inited) { this.shapelessRecipes.clear(); this.shapedRecipes.clear(); this.getConfiguration().load(); }
        else       inited = true;

        this.enableInfiniteLava = this.getConfiguration().getBoolean("hacks.inflava", false);
        if(this.enableInfiniteLava) log.info("[SnowBalls] Infinite lava hack enabled!");
        this.enableMaxStack = this.getConfiguration().getBoolean("hacks.maxstack", false);
        if(this.enableMaxStack) log.info("[SnowBalls] Custom max stack size enabled!");

        this.leavesLoot = this.getConfiguration().getBoolean("block.cacaobeanbonus", false);
        if(this.leavesLoot) log.info("[SnowBalls] Extra birch leaves loot bonus enabled!");
        this.bookshelvesdrop = this.getConfiguration().getBoolean("block.dropbookshelves", false);
        if(this.bookshelvesdrop) log.info("[SnowBalls] Book shelves dropping enabled!");
        this.icedrop = this.getConfiguration().getBoolean("block.dropice", false);
        if(this.icedrop) log.info("[SnowBalls] Ice dropping enabled!");

        List<String> shapelesslines = this.getConfiguration().getKeys("recipes.shapeless");
        for(String s1 : shapelesslines) {
            try {
                List<Integer> result = this.getConfiguration().getIntList("recipes.shapeless."+s1+".result", null);
                ShapelessRecipe sl = new ShapelessRecipe(new ItemStack(result.get(0), result.get(2), result.get(1).shortValue()));
                List<Object> ingredients = this.getConfiguration().getList("recipes.shapeless."+s1+".ingredients");
                if(ingredients!=null) {
                    for(Object iline : ingredients) {
                        if(iline instanceof List) {
                            List<Integer> l = (List<Integer>) iline;
                            sl.addIngredient(Material.getMaterial(l.get(0)), l.get(1));
                        }
                    }
                }
                // this.getServer().addRecipe(sl);
                log.info("[SnowBalls] Added shapeless recipe with name '"+s1+"'!");
                shapelessRecipes.add(sl);

            } catch(NullPointerException ex) {
                System.out.println("Error with recipe '"+s1+"'!");
            }
        }

        List<String> shapedrecipelines = this.getConfiguration().getKeys("recipes.shaped");
        for(String s1 : shapedrecipelines) {
            try{
                List<Integer> result = this.getConfiguration().getIntList("recipes.shaped."+s1+".result", null);
                ShapedRecipe sr = new ShapedRecipe(new ItemStack(result.get(0), result.get(2), result.get(1).shortValue()));
                // sr.
                String[] shape = this.getConfiguration().getStringList("recipes.shaped."+s1+".shape", null).toArray(new String[0]);
                sr.shape(shape);
                for(String charac : this.getConfiguration().getKeys("recipes.shaped."+s1+".ingredients")) {
                    List<Integer> l = this.getConfiguration().getIntList("recipes.shaped."+s1+".ingredients."+charac, null);
                    Material mat = Material.getMaterial(l.get(0));
                    sr.setIngredient(charac.toCharArray()[0], mat, l.get(1));
                }
                log.info("[SnowBalls] Added shaped recipe with name '"+s1+"'!");
                shapedRecipes.add(sr);
            } catch(NullPointerException ex) {
                System.out.println("Error with recipe '"+s1+"'!");
            }
        }

        // if(enableInfiniteLava)
        if(enableMaxStack) this.setMaxStack();
    }

    public void onDisable() {
        //To change body of implemented methods use File | Settings | File Templates.
        log.info("["+pdffile.getName()+"] "+pdffile.getName()+" version "+pdffile.getVersion()+" shutting down!");
    }

    /* public void genRecipes() {
        this.reloadConfig();
    } */

    public void addRecipes() {
        for(ShapelessRecipe recipe : shapelessRecipes) {
            this.getServer().addRecipe(recipe);
        }
        for(ShapedRecipe recipe : shapedRecipes) {
            this.getServer().addRecipe(recipe);
        }
    }

    public void onEnable() {
        // cuiPlayers = new ArrayList<Player>();
        // newCUI =
        cuiPlayers = new HashMap<Player, Boolean>();
        pdffile = this.getDescription();

        this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Monitor, this);
        this.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Monitor, this);
        this.getServer().getPluginManager().registerEvent(Event.Type.LEAVES_DECAY, blockListener, Event.Priority.Monitor, this);
        this.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_FROMTO, blockListener, Event.Priority.Lowest, this);
        /* this.getServer().getPluginManager().registerEvent(Event.Type.FURNACE_BURN, inventoryListener, Event.Priority.Monitor, this);
        this.getServer().getPluginManager().registerEvent(Event.Type.FURNACE_SMELT, inventoryListener, Event.Priority.Monitor, this); */

        this.setupTcUtils();
        // this.setupWE();
        this.setupWG();

        shapelessRecipes = new ArrayList<ShapelessRecipe>();
        shapedRecipes = new ArrayList<ShapedRecipe>();
        this.parseConfig();
        this.addRecipes();

        log.info("["+pdffile.getName()+"] "+pdffile.getName()+" version "+pdffile.getVersion()+" loaded!");
    }

    public void setupTcUtils() {
        Plugin plug = this.getServer().getPluginManager().getPlugin("TweakcraftUtils");
        if(tcutils==null) {
            if(plug!=null)
                tcutils = (TweakcraftUtils) plug;
        }
    }

    public void setupWG() {
        Plugin plug = this.getServer().getPluginManager().getPlugin("WorldGuard");
        if(wg==null) {
            if(plug!=null)
                wg = (WorldGuardPlugin) plug;
        }
    }

    public void setupWE() {
        Plugin plug = this.getServer().getPluginManager().getPlugin("WorldEdit");
        if(we==null) {
            if(plug!=null)
                we = (WorldEditPlugin) plug;
        }
    }


    public void setMaxStack() {
        // log.info("[SnowBalls] Custom max stack sizes enabled!");
        extraids = new TreeMap<Integer, Integer>();

        /* for(Integer i : origstacksizes.keySet()) {

        } */

        for(Material mat : Material.values()) {
            int matid = mat.getId();

            if(mat.getMaxStackSize() != 64
                    && !ItemType.shouldNotStack(matid))
            {
                if(!origstacksizes.containsKey(matid)) { // Only store it if we're going to change it!
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



    public List<String> getRecipes(boolean SUIv2) {
        List<String> recipes = new ArrayList<String>();
        String s = "";
        ItemStack isr;
        List<MaterialData> ist;
        if(shapelessRecipes!=null && shapelessRecipes.size()>0) {
            for(ShapelessRecipe sr : shapelessRecipes) {
                isr = sr.getResult();
                ist = sr.getIngredientList();
                s = CUIPattern+"0:"+isr.getTypeId()+";"+isr.getDurability()+";"+isr.getAmount();
                for(MaterialData md : ist) {
                    if(!SUIv2)
                        s+="|"+md.getItemTypeId()+";"+md.getData()+";"+1;
                    else
                        s+="|"+md.getItemTypeId()+(md.getData()!=0?";"+md.getData():"");

                }
                recipes.add(s);
            }
        }

        for(ShapedRecipe shr : shapedRecipes) {
            isr = shr.getResult();
            String[] shape = shr.getShape();
            s = CUIPattern+"1:"+isr.getTypeId()+";"+isr.getDurability()+";"+isr.getAmount();
            /* while(shape.size()<3) {
                shape.add("");
            } */
            for(String sha : shape) {
                while(sha.length()<3) {
                    sha+=" ";
                }
                // System.out.println("shape : "+sha.replace(' ', '.'));
                for(char a:sha.toCharArray()) {
                    if(a==' ') {
                        s+="|";
                    } else {
                        MaterialData md = shr.getIngredientMap().get(a);
                        if(!SUIv2) s+="|"+md.getItemTypeId()+";"+md.getData()+";"+1;
                        else s+="|"+md.getItemTypeId()+(md.getData()!=0?";"+md.getData():"");
                    }
                }
            }
            recipes.add(s);
        }
        int x = 0;


        s=CUIPattern+"i";
        for(Integer i : extraids.keySet()) {
            if(x>0 && x%5==0) {
                // s=s.substring(0, s.length()-1);
                recipes.add(s);
                s=CUIPattern+"i";
            }

            s+="|"+i+";"+extraids.get(i);
            x++;
        }
        if(x%5!=0) {
            /* if(s.charAt(s.length()-1)== '|')
                s=s.substring(0, s.length()-1); */
            recipes.add(s); // leftovers?
        }

        return recipes;
    }

    public JsonHelper getJsonHelper() {
        return jsonHelper;
    }

    public TweakcraftUtils getTcutils() {
        return tcutils;
    }

    public void sendRecipes(Player p) {
        if(cuiPlayers.containsKey(p)) {
            boolean SUIv2 = cuiPlayers.get(p);
            for(String r : getRecipes(SUIv2)) {
                // boolean go = true;
                if(r.length()>60 && !SUIv2) {
                    p.sendMessage(ChatColor.AQUA+"This server uses recipes with a newer format");
                    p.sendMessage(ChatColor.AQUA+"You're running a legacy version of the SnowBalls-clientmod,");
                    p.sendMessage(ChatColor.AQUA+"Please upgrade your clientmod to fully utilise all the recipes");
                } else {
                    p.sendRawMessage(r);
                }
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("snowballs")) {
            if(args.length>0) {
                if(args[0].equalsIgnoreCase("client") && sender instanceof Player) {
                    Player player = (Player) sender;
                    boolean SUIv2 = args.length>1 && args[1].equalsIgnoreCase("SUIv2");
                    if(!SUIv2)
                        log.info("["+pdffile.getName()+"] WARNING: "+player.getName()+" is using a legacy version of SnowBalls-clientmod!");

                    boolean onList = cuiPlayers.containsKey(player);


                    if(!onList) {
                        log.info("["+pdffile.getName()+"] Adding "+player.getName()+" to the SUI list...");
                        cuiPlayers.put((Player)sender, SUIv2);
                    } else {
                        boolean legacy = !cuiPlayers.get(player);
                        if(legacy && SUIv2) {
                            log.info("["+pdffile.getName()+"] Updating "+player.getName()+"'s SUI status to SUIv2");
                            cuiPlayers.put((Player)sender, SUIv2);
                        } else {
                            log.info("["+pdffile.getName()+"] "+player.getName()+" already is assigned to the SUI list...");
                        }
                    }
                    
                    this.sendRecipes((Player) sender);
                } else if(args[0].equalsIgnoreCase("sendrecipes")) {
                    this.sendRecipes((Player) sender);
                } else if(args[0].equalsIgnoreCase("reload")) {
                    if(sender.isOp() || sender.hasPermission("snowballs.reloadconfig")) {
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