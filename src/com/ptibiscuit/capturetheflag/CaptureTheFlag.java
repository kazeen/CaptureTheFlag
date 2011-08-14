package com.ptibiscuit.capturetheflag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class CaptureTheFlag extends JavaPlugin {

	private PluginManager pm;
	private ArrayList<Team> teams = new ArrayList<Team>();
	private FlagManager FlagM = new FlagManager();
	private RespawnManager rm = new RespawnManager();
	private DeathManager dm = new DeathManager();
	private Server sv;
	
	private World here;
	
	@Override
	public void onDisable() {
		
		
	}

	public static void main(String[] args)
	{
		SAXBuilder sb = new SAXBuilder();
		org.jdom.Document document = null;
		try {
			document = sb.build(new File("CaptureTheFlag/config.xml"));
			
		} catch (JDOMException e) {
				e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onEnable() {
		// Petite interlude pour trouver le bon world, et activer le serveur
		World actual = this.getServer().getWorlds().get(0);
		sv = this.getServer();
		
		pm = getServer().getPluginManager();
		System.out.println("Capture The Flag by Ptibiscuit");
		// On attribue les registres
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, FlagM, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_RESPAWN, rm, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, FlagM, Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_DEATH, dm, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, rm, Priority.Normal, this);
		// On charge les coordonées & informations dans le xml.
		SAXBuilder sb = new SAXBuilder();
		org.jdom.Document document = null;
		try {
			 try {
				document = sb.build(new File("CaptureTheFlag/config.xml"));
			} catch (JDOMException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Element root = document.getRootElement();
		Iterator i = root.getChildren("team").iterator();
		while (i.hasNext())
		{
			Element now = (Element) i.next();
			System.out.println("Création d'un équipe : " + now.getAttribute("name").toString());
			Team EnCours = new Team(now.getAttribute("name").toString(), now.getAttribute("tag").toString());
			
			// Initialisation de la position du drapeau
			Element flag = now.getChild("flag");
			EnCours.setFlagLoc(new Location(actual,
											Integer.parseInt(flag.getAttribute("x").toString()),
											Integer.parseInt(flag.getAttribute("y").toString()),
											Integer.parseInt(flag.getAttribute("z").toString())));
			
			// Initialisation du spawn des joueurs
			Element spawn = now.getChild("spawn");
			EnCours.setSpawnLoc(new Location(actual,
					Integer.parseInt(spawn.getAttribute("x").toString()),
					Integer.parseInt(spawn.getAttribute("y").toString()),
					Integer.parseInt(spawn.getAttribute("z").toString())));
		}
		// On initialise le jeu
		reinitialiser();
	}
	
	public void removeAnyFlagFromAllPlayers()
	{
		for (Team t2 : teams)
		{
			for (Player p2 : t2.getMembres())
			{
				boolean yes = false;
				ItemStack[] is = p2.getInventory().getContents();
				for (ItemStack now : is)
				{
					if (now == t2.getFlagItem())
					{
						yes = true;
					}
				}
				if (yes)
				{
					p2.getInventory().remove(t2.getFlagItem());
				}
			}
		}
	}
	
	public void removeFlagsFromPlayer(Player p)
	{
		for (Team t2 : teams)
		{
			boolean yes = false;
			ItemStack[] is = p.getInventory().getContents();
			for (ItemStack now : is)
			{
				if (now == t2.getFlagItem())
				{
					yes = true;
				}
			}
			if (yes)
			{
				p.getInventory().remove(t2.getFlagItem());
			}
		}
	}
	
	public void reinitialiser() 
	{
		// On enlève tous les drapeaux de toutes les équipes.
		removeAnyFlagFromAllPlayers();
		for (Team t : teams)
		{
			// On replace les drapeaux dans chaque base !
			here.getBlockAt(t.getFlagLoc()).setType(t.getFlagType()); // On le met en tant que drapeau
			t.setFlag(here.getBlockAt(t.getFlagLoc())); // On enregistre ce block. =)
			
			// On fait spawn les joueurs dans chaque base !
			for (Player p : t.getMembres())
			{
				p.teleport(t.getSpawnLoc());
			}
		}
	}
	
	public Team getTeamByPlayer(Player p)
	{
		for (Team t : teams)
		{
			if (t.getMembres().contains(p))
				return t;
		}
		return null;
	}
	
	
	class FlagManager extends BlockListener
	{
		public void onBlockPlace(BlockPlaceEvent e)
		{
			if (getTeamByItemFlag(e.getItemInHand()) != null)
				e.setCancelled(false);
		}
		
		public void onBlockBreak(BlockBreakEvent e)
		{
			if (getTeamByFlag(e.getBlock()) != null)
				e.setCancelled(true);
		}
		
		public void onBlockDamage(BlockDamageEvent e)
		{
			// Si le bloc damagé est null, je le fais pour la securité, in the case of. =)
			if (e.getBlock() == null)
				return;
			// Si le joueur appartient bien au jeu. =)
			Team PlayerBelong;
			if ((PlayerBelong = getTeamByPlayer(e.getPlayer())) != null)
			{
				Team BlockBelong;
				// Si le bloc est un drapeau quelconque
				if ((BlockBelong = getTeamByFlag(e.getBlock())) != null)
				{
					// Si l'équipe du joueur et du drapeau sont les mêmes. C'est que c'est pour ramener le drapeau !
					if (BlockBelong == PlayerBelong)
					{
						ItemStack drapeauPerdant;
						if ((drapeauPerdant = getFlagItemByPlayer(e.getPlayer())) != null)
						{
							// On lui enlève le drapeau, il en aura plus besoin ... =P
							removeFlagsFromPlayer(e.getPlayer());
							sv.broadcastMessage("[CTF] " + PlayerBelong.getName() + " a rapporté un drapeau !");
							
							// On remets pour l'équipe perdante des valeurs par défauts et on lui refait un drapeau. =)
							Team perdant = getTeamByItemFlag(drapeauPerdant);
							perdant.setFlagItem(null);
							here.getBlockAt(perdant.getFlagLoc()).setType(perdant.getFlagType());
							perdant.setFlag(here.getBlockAt(perdant.getFlagLoc()));
						}
					}
					// Si ils sont différents, c'est que c'est pour voler le drapeau !
					if (BlockBelong != PlayerBelong)
					{
						// Tout d'abord on supprime le drapeau sur le sol
						here.getBlockAt(BlockBelong.getFlagLoc()).setType(Material.AIR);
						BlockBelong.setFlag(null);
						
						// Ensuite, on donne l'item drapeau au joueur qui a pris le drapeau. =)
						e.getPlayer().getInventory().addItem(new ItemStack(PlayerBelong.getFlagType()));
						sv.broadcastMessage("[CTF] " + PlayerBelong.getName() + " a capturé le drapeau de " + BlockBelong.getName());
					}
				}
			}
		}
	}
	
	class CommandManager extends PlayerListener
	{
		public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e)
		{
			String commandBrut = e.getMessage().substring(1);
			String[] commandHache = commandBrut.split(" ");
			if (commandHache[0].equals("ctf"))
			{
				if (commandHache[1].equals("join"))
				{
					Team ask;
					if ((ask = getTeamByTag(commandHache[2])) != null)
					{
						ask.addMembre(e.getPlayer());
						e.getPlayer().teleport(ask.getSpawnLoc());
						sv.broadcastMessage("[CTF] " + e.getPlayer().getDisplayName() + " a rejoint  " + ask.getName() + " !");
						
					}
				}
				if (commandHache[1].equals("leave"))
				{
					Team ask;
					if ((ask = getTeamByPlayer(e.getPlayer())) != null)
					{
						ask.removeMembre(e.getPlayer());
					}
				}
			}
		}
	}
	
	public Team getTeamByTag(String tag)
	{
		for (Team t : teams)
		{
			if (t.getTag().equals(tag))
				return t;
		}
		return null;
	}
		
	public ItemStack getFlagItemByPlayer(Player p)
	{
		for (Team t : teams)
		{
			if (p.getInventory().contains(t.getFlagItem()))
				return t.getFlagItem();
		}
		return null;
	}
	
	public Team getTeamByFlag(Block test)
	{
		for (Team t : teams)
		{
			if (t.getFlag() == test)
			{
				return t;
			}
		}
		return null;
	}
	
	public Team getTeamByItemFlag(ItemStack test)
	{
		for (Team t : teams)
		{
			if (t.getFlagItem() == test)
			{
				return t;
			}
		}
		return null;
	}
	
	class DeathManager extends EntityListener
	{
		public void onEntityDeath(EntityDeathEvent e)
		{
			
			if (e.getEntity() instanceof Player)
			{
				Player p = (Player) e.getEntity();
				if (getFlagItemByPlayer(p) != null)
				{
					// On supprime l'objet drapeau. :)
					p.getInventory().remove(getFlagItemByPlayer(p));
					
				}
			}
		}
	}
	
	class RespawnManager extends PlayerListener
	{
		public void onPlayerRespawn(PlayerRespawnEvent e)
		{
			Team tampon;
			if ((tampon = getTeamByPlayer(e.getPlayer())) != null)
				e.setRespawnLocation(tampon.getSpawnLoc());
		}
	}
}
