package com.dre.magicspellsextended;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class TimeJump extends InstantSpell{
	
	private int confIntervall = 500; //500 milliseconds / 0.5 seconds
	private int confJumpbacks = 20;
	
	private long lastSave;
	private Map<Player, ArrayList<Location>> players= new HashMap<Player, ArrayList<Location>>();
	
	public TimeJump(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.confIntervall = getConfigInt("intervall", 500);
		this.confJumpbacks = getConfigInt("jumpbacks", 20);
		
		setupScheduler();
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState castState, float power, String[] args) {
		ArrayList<Location> locations = players.get(player);
		
		if(locations != null){
			player.teleport(locations.get(0));
		}
		
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private void setupScheduler(){
		Bukkit.getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, new Runnable(){
			@Override
			public void run() {
				if(lastSave + confIntervall < System.currentTimeMillis()){
					for(Player player : Bukkit.getOnlinePlayers()){
						if(players.get(player) != null){
							ArrayList<Location> locations = new ArrayList<Location>();
							
							locations.add(player.getLocation());
							
							players.put(player, locations);
						} else {
							ArrayList<Location> locations = players.get(player);
							
							locations.add(player.getLocation());
							
							if(locations.size() > confJumpbacks){
								locations.remove(0);
							}
						}
					}
				}
			}
		}, 0, 5);
	}
}
