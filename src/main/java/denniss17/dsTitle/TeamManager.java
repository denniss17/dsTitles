package denniss17.dsTitle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import denniss17.dsTitle.Title;

@SuppressWarnings("deprecation")
public class TeamManager {	
	private DSTitle plugin;
	
	private Map<String, Team> teams;
	
	public TeamManager(DSTitle plugin) {
		this.plugin = plugin;
		this.teams = new HashMap<String, Team>();
	}

	/**
	 * Returns a Team with the right prefix and suffix
	 * @param prefix The title to get prefix and suffix from
     * @param suffix The title to get prefix and suffix from
	 * @return the Team
	 * @ensure result.getPrefix().equals(title.headprefix) && result.getSuffix().equals(title.headsuffix)
	 */
	public Team getTeam(Title prefix, Title suffix, Player player){
		Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
		
		String prefixName = prefix==null ? "" : prefix.name;
		String suffixName = suffix==null ? "" : suffix.name;
		Team team = teams.get(prefixName + "-" + suffixName + "-" + player.getUniqueId().toString());
		
		if(team==null){
			int i = 0;
			while(scoreboard.getTeam("dt_" + i)!=null){
				i++;
			}
			team = scoreboard.registerNewTeam("dt_" + i);
			if(prefix!=null && prefix.headTag != null){
				String p = ChatStyler.setTotalStyle(prefix.headTag);
				if(p.length()>16) p = p.substring(0, 16);
				team.setPrefix(p);
			}
			if(suffix!=null && suffix.headTag != null){
				String p = ChatStyler.setTotalStyle(suffix.headTag);
				if(p.length()>16) p = p.substring(0, 16);
				team.setSuffix(p);
			}
			// Set options to same as if not in team
			team.setAllowFriendlyFire(true);
			team.setCanSeeFriendlyInvisibles(false);
			for(Player p : plugin.getServer().getOnlinePlayers()){
				if(!p.canSee(player)){
					team.setNameTagVisibility(NameTagVisibility.NEVER);
				}
			}
			//Deprecated, will likely replace when future versions of Spigot are sure to continue
			
			teams.put(prefixName + "-" + suffixName + "-" + player.getUniqueId().toString(), team);
		}
		
		return team;
	}

	public void removePlayerFromTeam(Player player) {
		removePlayerFromTeam(player, plugin.getTitleManager().getPlayerPrefix(player), plugin.getTitleManager().getPlayerSuffix(player));
	}

	/**
	 * Remove the player from the team corresponding to the given title
	 * @param player
     * @param prefix The title to get prefix and suffix from
     * @param suffix The title to get prefix and suffix from
	 * @require title!=null
	 */
	public void removePlayerFromTeam(Player player, Title prefix, Title suffix) {
		if(prefix==null && suffix==null) return;
		String prefixName = prefix==null ? "" : prefix.name;
		String suffixName = suffix==null ? "" : suffix.name;
		Team team = teams.get(prefixName + "-" + suffixName + "-" + player.getUniqueId().toString());
		if(team!=null){
			team.removePlayer(player);
			// Cleanup
			if(team.getSize()==0){
				removeTeam(team);
			}
		}
	}

	private void removeTeam(Team team) {
		String key = null;
		Iterator<Entry<String, Team>> it = teams.entrySet().iterator();
		Entry<String, Team> entry;
		while(it.hasNext() && key==null){
			entry = it.next();
			if(entry.getValue().equals(team)){
				key=entry.getKey();
			}
		}
		
		if(key!=null) teams.remove(key);
		team.unregister();
	}

	/**
	 * Reload the prefixes and suffixes of the teams
	 */
	public void reloadTags() {		
		// Remove all teams
		cleanUpTeams(true);
		
		// Recreate all teams
		if(plugin.getConfig().getBoolean("general.use_nametag")){
			for(Player player : plugin.getServer().getOnlinePlayers()){
				Title prefix = plugin.getTitleManager().getPlayerPrefix(player);
				Title suffix = plugin.getTitleManager().getPlayerSuffix(player);
				if(prefix!=null || suffix!=null){
					plugin.getTeamManager().getTeam(prefix, suffix, player).addPlayer(player);
				}
			}
		}
	}
	
	/**
	 * Clean up unused teams
	 * @param force Remove all teams, even if there are still players in it.
	 */
	public void cleanUpTeams(boolean force){
		Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
		
		for(Team team : scoreboard.getTeams()){
			if(team.getName().startsWith("dt_")){
				if(team.getSize()==0 || force){
					// Nobody in team, or everything should be removed
					removeTeam(team);
				}else{
					// There is somebody in the team, but maybe it is not a valid team,
					// because the title could be renamed or removed
					
					// Find corresponding title
					boolean found = false;
					Iterator<Title> prefixIterator = plugin.getTitleManager().getPrefixes().iterator();
					Title prefix;
					while(prefixIterator.hasNext() && !found){
						prefix = prefixIterator.next();
						if(prefix.headTag!=null && team.getPrefix().equals(ChatStyler.setTotalStyle(prefix.headTag))){
							Iterator<Title> suffixIterator = plugin.getTitleManager().getSuffixes().iterator();
							Title suffix;
							while(suffixIterator.hasNext() && !found){
								suffix = suffixIterator.next();
								if(suffix.headTag!=null && team.getSuffix().equals(ChatStyler.setTotalStyle(suffix.headTag))){
									found = true;
								}
							}
						}
					}
					
					if(!found){
						// Title is removed, but team still exists
						removeTeam(team);
					}
				}
			}
		}		
	}
}
