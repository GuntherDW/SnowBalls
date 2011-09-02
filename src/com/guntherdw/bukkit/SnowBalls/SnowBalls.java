package com.guntherdw.bukkit.SnowBalls;

import com.guntherdw.bukkit.tweakcraft.TweakcraftUtils;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Item;
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
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * @author GuntherDW
 */
public class SnowBalls extends JavaPlugin {

    private final SnowBallsBlock blockListener = new SnowBallsBlock(this);
    private final SnowBallsPlayer playerListener = new SnowBallsPlayer(this);
    // private final SnowBallsInventory inventoryListener = new SnowBallsInventory(this);
    private final JsonHelper jsonHelper = new JsonHelper(this);
    private List<Player> cuiPlayers;
    protected String CUIPattern = "§7§3§3§7";
    protected static final Logger log = Logger.getLogger("Minecraft");
    protected static List<ShapelessRecipe> shapelessRecipes;
    protected static List<ShapedRecipe> shapedRecipes;
    protected static Map<Integer, Integer> extraids;
    protected TweakcraftUtils tcutils = null;
    protected WorldGuardPlugin wg = null;
    protected WorldEditPlugin we = null;
    private PluginDescriptionFile pdffile;



    public void onDisable() {
        //To change body of implemented methods use File | Settings | File Templates.
        log.info("["+pdffile.getName()+"] "+pdffile.getName()+" version "+pdffile.getVersion()+" shutting down!");
    }

    public void genRecipes() {
        ShapelessRecipe[] woolRecipe = new ShapelessRecipe[15];
        for(int i=1; i<16; i++)
        {
            woolRecipe[i-1] = new ShapelessRecipe(new ItemStack(Material.WOOL, 1, (byte)0));
            woolRecipe[i-1].addIngredient(Material.WOOL, i);
            woolRecipe[i-1].addIngredient(Material.INK_SACK, (byte) 15);
            shapelessRecipes.add(woolRecipe[i-1]);
        }
        ShapelessRecipe   sandstoneRecipe;
        sandstoneRecipe = new ShapelessRecipe(new ItemStack(Material.SAND, 4, (byte) 0));
        sandstoneRecipe.addIngredient(Material.SANDSTONE);
        shapelessRecipes.add(sandstoneRecipe);
        ShapedRecipe reversegoldrecipe = new ShapedRecipe(new ItemStack(Material.GOLD_BLOCK, 2, (byte) 0));
        reversegoldrecipe.shape("AA","AA");
        reversegoldrecipe.setIngredient('A', Material.SPONGE);
        shapedRecipes.add(reversegoldrecipe);
        ShapedRecipe webrecipe;
        webrecipe = new ShapedRecipe(new ItemStack(Material.WEB, 1, (byte) 0));
        webrecipe.shape("A A", " A ", "A A");
        webrecipe.setIngredient('A', Material.STRING);
        this.getServer().addRecipe(webrecipe);
        shapedRecipes.add(webrecipe);
        ShapedRecipe spongerecipe = new ShapedRecipe(new ItemStack(Material.SPONGE, 8, (byte) 0));
        spongerecipe.shape("AA","AA");
        spongerecipe.setIngredient('A', Material.GOLD_BLOCK);
        shapedRecipes.add(spongerecipe);

        /* ItemStack itemstack = new ItemStack(Material.SIGN,  1);
        itemstack.set */
    }

    public void addRecipes() {
        for(ShapelessRecipe recipe : shapelessRecipes) {
            this.getServer().addRecipe(recipe);
        }
        for(ShapedRecipe recipe : shapedRecipes) {
            this.getServer().addRecipe(recipe);
        }
    }

    public void onEnable() {
        cuiPlayers = new ArrayList<Player>();
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
        this.genRecipes();
        this.addRecipes();
        this.setMaxStack();

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
        extraids = new TreeMap<Integer, Integer>();
        for(Material mat : Material.values()) {
            int matid = mat.getId();

            if(mat.getMaxStackSize() != 64
                    && !ItemType.shouldNotStack(matid))
            {
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
        if(shapelessRecipes!=null && shapelessRecipes.size()>0) {
            for(ShapelessRecipe sr : shapelessRecipes) {
                isr = sr.getResult();
                ist = sr.getIngredientList();
                s = CUIPattern+"0:"+isr.getTypeId()+";"+isr.getDurability()+";"+isr.getAmount();
                for(MaterialData md : ist) {
                    s+="|"+md.getItemTypeId()+";"+md.getData()+";"+1;
                }
                recipes.add(s);
            }
        }

        for(ShapedRecipe shr : shapedRecipes) {
            // List<String> shape = new ArrayList<String>();


            isr = shr.getResult();
            String[] shape = shr.getShape();
            /* for(String ingr : shr.getShape()) {
                shape.add(ingr);
            } */
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
                        s+="|"+md.getItemTypeId()+";"+md.getData()+";"+1;
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
        if(cuiPlayers.contains(p)) {
            for(String r : getRecipes()) {
                p.sendRawMessage(r);
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("snowballs")) {
            if(sender instanceof Player) {
                if(args.length>0) {
                    if(args[0].equalsIgnoreCase("client")) {
                        if(!cuiPlayers.contains((Player)sender)) {
                            log.info("["+pdffile.getName()+"] Adding "+((Player) sender).getName()+" to the SUI list...");
                            cuiPlayers.add((Player) sender);
                        } else {
                            log.info("["+pdffile.getName()+"] "+((Player) sender).getName()+" already is assigned to the SUI list...");
                        }
                        this.sendRecipes((Player) sender);
                    } else if(args[0].equalsIgnoreCase("sendrecipes")) {
                        this.sendRecipes((Player) sender);
                    }
                } else {
                    sender.sendMessage(command.getUsage());
                }
            } else {
                sender.sendMessage("What are you doing here?");
            }
        }
        return true;
    }

}