package com.dre.magicspellsextended;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class TimeJump extends InstantSpell implements Listener{
	
	private int confIntervall = 500; //500 milliseconds / 0.5 seconds
	private int confJumpbacks = 20;
	private int confMaxJumpbacks = 200;
	
	private long lastSave;
	private Map<Player, ArrayList<Location>> players= new HashMap<Player, ArrayList<Location>>();
	
	public TimeJump(MagicConfig config, String spellName) {
		super(config, spellName);
		
		this.confIntervall = getConfigInt("intervall", 500);
		this.confJumpbacks = getConfigInt("jumpbacks", 20);
		this.confMaxJumpbacks = getConfigInt("max-jumpbacks", 200);
		
		MagicSpells.registerEvents(this);
		
		setupScheduler();
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState castState, float power, String[] args) {
		ArrayList<Location> locations = players.get(player);
		
		if(locations != null){
			if(this.confJumpbacks <= locations.size()){
				player.teleport(locations.get(locations.size() - this.confJumpbacks));
				locations.subList(locations.size() - this.confJumpbacks, locations.size()).clear();
				player.sendMessage(ChatColor.GREEN + "~~SWUSH~~");
				return PostCastAction.HANDLE_NORMALLY;
			} else {
				player.sendMessage(ChatColor.RED + "Zeitreise nicht m�glich!");
				return PostCastAction.NO_REAGENTS;
			}
		} 
		
		return PostCastAction.NO_REAGENTS;
	}
	
	private void setupScheduler(){
		Bukkit.getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, new Runnable(){
			@Override
			public void run() {
				if(lastSave + confIntervall < System.currentTimeMillis()){
					for(Player player : Bukkit.getOnlinePlayers()){
						if(players.get(player) == null){
							ArrayList<Location> locations = new ArrayList<Location>();
							
							locations.add(player.getLocation());
							
							players.put(player, locations);
						} else {
							ArrayList<Location> locations = players.get(player);
							
							locations.add(player.getLocation());
							
							if(locations.size() > confMaxJumpbacks){
								locations.remove(0);
							}
						}
					}
				}
			}
		}, 0, 5);
	}
	
	@EventHandler()
	private void onPlayerDeath(PlayerDeathEvent event){
		Player player = event.getEntity();
		if(players.get(player) != null){
			ArrayList<Location> locations = players.get(player);
			locations.clear();
		}
	}
}
