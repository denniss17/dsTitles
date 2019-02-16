package denniss17.dsTitle;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@SuppressWarnings("deprecation")
public class PlayerListener implements Listener {

	private DSTitle plugin;
	
	private boolean asyncChatListener;
	
	public static String prefixTag;
	public static String suffixTag;
	public static String playerTag;
	
	public PlayerListener(DSTitle plugin, boolean asyncChatListener){
		this.plugin = plugin;
		this.asyncChatListener = asyncChatListener;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerSyncChat(PlayerChatEvent event){
		if(asyncChatListener) return;
		
		event.setFormat(parseChatFormat(event.getFormat(), event.getPlayer()));
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerAsyncChat(AsyncPlayerChatEvent event) {
		if(!asyncChatListener) return;
		
		event.setFormat(parseChatFormat(event.getFormat(), event.getPlayer()));
	}

	private String parseChatFormat(String chatFormat, Player player){		
		//Check to make sure the user has a chatTag before speaking. If not, assign them the default one.
		if(plugin.getTitleManager().getPlayerPrefix((OfflinePlayer) player) == null){
			if(plugin.getTitleManager().getDefaultPrefix()!=null)
			plugin.getTitleManager().setPlayerPrefix(plugin.getTitleManager().getDefaultPrefix(), player);
        }
		if(plugin.getTitleManager().getPlayerSuffix((OfflinePlayer) player) == null){
			if(plugin.getTitleManager().getDefaultPrefix()!=null)
			plugin.getTitleManager().setPlayerSuffix(plugin.getTitleManager().getDefaultSuffix(), player);
        }
		if(plugin.getConfig().getBoolean("general.overwrite_format")){
			chatFormat = plugin.getConfig().getString("general.chat_format");	
		}
		if(plugin.getConfig().getBoolean("general.use_chattag")){
			if(!plugin.getConfig().getBoolean("general.overwrite_format")){
				if(!chatFormat.contains(prefixTag)) chatFormat = chatFormat.replace(playerTag, prefixTag + playerTag);
				if(!chatFormat.contains(suffixTag)) chatFormat = chatFormat.replace(playerTag, playerTag + suffixTag);
			}
			Title prefix = plugin.getTitleManager().getPlayerPrefix(player);
			Title suffix = plugin.getTitleManager().getPlayerSuffix(player);
			if(prefix!=null && prefix.chatTag!=null){
				if(!chatFormat.contains(prefixTag)){
					chatFormat = chatFormat.replace(playerTag, prefix.chatTag + playerTag + "&r");
				}else{
					chatFormat = chatFormat.replace(prefixTag, prefix.chatTag + "&r");
				}
			}else{
				chatFormat = chatFormat.replace(prefixTag, "");
			}
			if(suffix!=null && suffix.chatTag!=null){
				if(!chatFormat.contains(suffixTag)){
					chatFormat = chatFormat.replace(playerTag, playerTag + suffix.chatTag + "&r");
				}else{
					chatFormat = chatFormat.replace(suffixTag, suffix.chatTag + "&r");
				}
			}else{
				chatFormat = chatFormat.replace(suffixTag, "");
			}
		}
		
		return ChatStyler.setTotalStyle(chatFormat);
	}
    
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event){

        // When a Player Joins, grab the Title
        plugin.getStorage().loadTitlesPlayer(event.getPlayer());
        //If the player just joined for the first time, assign them the default Prefix and Default Suffix
        if(plugin.getTitleManager().getPlayerPrefix((OfflinePlayer)event.getPlayer()) == null){
        	if(plugin.getTitleManager().getDefaultPrefix()!=null)
        	plugin.getTitleManager().setPlayerPrefix(plugin.getTitleManager().getDefaultPrefix(), event.getPlayer());
        }
        if(plugin.getTitleManager().getPlayerSuffix((OfflinePlayer) event.getPlayer()) == null){
        	if(plugin.getTitleManager().getDefaultPrefix()!=null)
        	plugin.getTitleManager().setPlayerSuffix(plugin.getTitleManager().getDefaultSuffix(), event.getPlayer());
        }
		
		// Check for update and send message
		if(plugin.getPermissionManager().hasPermission(event.getPlayer(), "ds_title.admin")){
			// If there is a new version
			if(DSTitle.versionChecker!=null && DSTitle.versionChecker.getLatestVersionName() != null){
				if(!DSTitle.versionChecker.getLatestVersionName().equals(plugin.getDescription().getVersion())){
				// Send message to player with admin permissions
				plugin.sendMessage(event.getPlayer(), plugin.getConfig().getString("messages.update_notification")
						.replace("{version}", DSTitle.versionChecker.getLatestVersionName())
						.replace("{current}", plugin.getDescription().getVersion())
						.replace("{website}", plugin.getDescription().getWebsite()));
			}
			}
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		if(plugin.getConfig().getBoolean("general.use_nametag")){
			plugin.getTeamManager().removePlayerFromTeam(event.getPlayer());
		}
	}
}
