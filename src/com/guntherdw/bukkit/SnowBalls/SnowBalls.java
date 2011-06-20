package com.guntherdw.bukkit.SnowBalls;

import com.guntherdw.bukkit.tweakcraft.TweakcraftUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author GuntherDW
 */
public class SnowBalls extends JavaPlugin {

    private final SnowBallsBlock blockListener = new SnowBallsBlock(this);
    private final SnowBallsPlayer playerListener = new SnowBallsPlayer(this);
    private final JsonHelper jsonHelper = new JsonHelper(this);
    private List<Player> cuiPlayers;
    protected String CUIPattern = "§7§3§3§7";
    protected static final Logger log = Logger.getLogger("Minecraft");
    protected static List<ShapelessRecipe> shapelessRecipes;
    protected static List<ShapedRecipe> shapedRecipes;
    protected TweakcraftUtils tcutils = null;
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
        /* ShapedRecipe woodswordrecipe = new ShapedRecipe(new ItemStack(Material.WOOD_SWORD, 1, (byte) 0));
        woodswordrecipe.shape("A","A","A");
        woodswordrecipe.setIngredient('A', Material.STICK);
        shapedRecipes.add(woodswordrecipe); */
        /* woolRecipe = new ShapelessRecipe(new ItemStack(Material.WOOL, 1));
        woolRecipe.addIngredient(Material.WOOL);
        woolRecipe.addIngredient(Material.INK_SACK, (byte) 15);
        this.getServer().addRecipe(woolRecipe); */
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

        shapelessRecipes = new ArrayList<ShapelessRecipe>();
        shapedRecipes = new ArrayList<ShapedRecipe>();
        this.genRecipes();
        this.addRecipes();
        setupTcUtils();
        log.info("["+pdffile.getName()+"] "+pdffile.getName()+" version "+pdffile.getVersion()+" loaded!");
    }

    public void setupTcUtils() {
        Plugin plug = this.getServer().getPluginManager().getPlugin("TweakcraftUtils");
        if(tcutils==null) {
            if(plug!=null)
                tcutils = (TweakcraftUtils) plug;
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
                /* while(sha.length()<3) {
                    sha+=" ";
                } */
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