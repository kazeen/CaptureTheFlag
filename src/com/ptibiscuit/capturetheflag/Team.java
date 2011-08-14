package com.ptibiscuit.capturetheflag;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Team {
	private ArrayList<Player> membres = new ArrayList<Player>();
	private String name;
	private String tag;
	private Location spawnLoc;
	private Location flagLoc;
	private Material flagType;
	
	// Ce sera un ou l'autre :
	// • Soit le drapeau est un bloc et il est au sol.
	// • Soit il est dans l'inventaire de quelqu'un. =)
	private Block flag;
	public Block getFlag() {
		return flag;
	}

	public void setFlag(Block flag) {
		this.flag = flag;
	}

	public void removeMembre(Player p)
	{
		membres.remove(p);
	}
	
	public void addMembre(Player p)
	{
		membres.add(p);
	}
	
	public ItemStack getFlagItem() {
		return flagItem;
	}

	public void setFlagItem(ItemStack flagItem) {
		this.flagItem = flagItem;
	}

	private ItemStack flagItem;
	
	public Team(String toName, String toTag)
	{
		name = toName;
		tag = toTag;
	}
	
	public Location getSpawnLoc() {
		return spawnLoc;
	}

	public void setSpawnLoc(Location spawnLoc) {
		this.spawnLoc = spawnLoc;
	}

	public Location getFlagLoc() {
		return flagLoc;
	}

	public void setFlagLoc(Location flagLoc) {
		this.flagLoc = flagLoc;
	}

	public ArrayList<Player> getMembres() {
		return membres;
	}

	public Material getFlagType() {
		return flagType;
	}

	public void setFlagType(Material flagType) {
		this.flagType = flagType;
	}

	public String getName() {
		return name;
	}

	public String getTag() {
		return tag;
	}
}
