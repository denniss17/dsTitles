package denniss17.dsTitle;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import denniss17.dsTitle.Title.Type;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CommandExec implements CommandExecutor{

	private DSTitle plugin;
	
	public CommandExec(DSTitle plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args) {
		if(cmd.getName().equals("title")){
			return cmdTitle(sender, cmd, commandlabel, args);
		}
		return false;
	}
	
	/**
	 * Main method used when the /title command is executed
	 * @param sender The sender of the command
	 * @param cmd The command itself
	 * @param commandlabel The label of this command
	 * @param args The arguments passed to this command
	 * @return true if the command is executed correctly, false otherwise
	 */
	private boolean cmdTitle(CommandSender sender, Command cmd, String commandlabel, String[] args) {
		if(args.length==0){
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_header"));
			if(plugin.getPermissionManager().hasPermission(sender, "ds_title.prefix.list")) 	plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_prefix_list"));
			if(plugin.getPermissionManager().hasPermission(sender, "ds_title.prefix.self"))		plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_prefix_set"));
			if(plugin.getPermissionManager().hasPermission(sender, "ds_title.suffix.list"))		plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_suffix_list"));
			if(plugin.getPermissionManager().hasPermission(sender, "ds_title.suffix.self"))		plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_suffix_set"));
			if(plugin.getPermissionManager().hasPermission(sender, "ds_title.clear.self"))		plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_clear"));
			if(plugin.getPermissionManager().hasPermission(sender, "ds_title.add"))				plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_add"));
			if(plugin.getPermissionManager().hasPermission(sender, "ds_title.edit"))			plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_edit"));
			if(plugin.getPermissionManager().hasPermission(sender, "ds_title.grant"))			plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_grant"));
			if(plugin.getPermissionManager().hasPermission(sender, "ds_title.ungrant"))			plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_ungrant"));
			if(plugin.getPermissionManager().hasPermission(sender, "ds_title.reload"))			plugin.sendMessage(sender, plugin.getConfig().getString("messages.menu_reload"));
			return true;
		}else{
			if(args[0].equalsIgnoreCase("prefix")){
				if(args.length>1 && args[1].equals("set")){
					return cmdTitleSetPrefix(sender, cmd, commandlabel, args);
				}else{
					return cmdTitleListPrefix(sender, cmd, commandlabel, args);
				}
			}else if(args[0].equalsIgnoreCase("suffix")){
				if(args.length>1 && args[1].equals("set")){
					return cmdTitleSetSuffix(sender, cmd, commandlabel, args);
				}else{
					return cmdTitleListSuffix(sender, cmd, commandlabel, args);
				}
			}else if(args[0].equalsIgnoreCase("add")){
				return cmdTitleAdd(sender, cmd, commandlabel, args);
			}else if(args[0].equalsIgnoreCase("edit")){
				return cmdTitleEdit(sender, cmd, commandlabel, args);
			}else if(args[0].equalsIgnoreCase("clear")){
				return cmdTitleClear(sender, cmd, commandlabel, args);
			}else if(args[0].equalsIgnoreCase("reload")){
				return cmdTitleReload(sender, cmd, commandlabel, args);
			}else if(args[0].equalsIgnoreCase("grant")){
				return cmdTitleGrant(sender, cmd, commandlabel, args);
			}else if(args[0].equalsIgnoreCase("ungrant")){
				return cmdTitleUngrant(sender, cmd, commandlabel, args);
			}else{
				return false;
			}
		}
	}
	
	private boolean cmdTitleAdd(CommandSender sender, Command cmd, String commandlabel, String[] args) {
		if(args.length<3) return false;
		
		// Check permission
		if(!plugin.getPermissionManager().hasPermission(sender, "ds_title.add")){
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_permission"));
			return true;
		}
		
		String name = args[2];
		Type type;
		if(args[1].equals("prefix")){
			type = Type.PREFIX;
		}else if(args[1].equals("suffix")){
			type = Type.SUFFIX;
		}else{
			return false;
		}
		
		
		// Check existence
		if(type.equals(Type.PREFIX) && plugin.getTitleManager().prefixExists(name)){
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_prefix_exists"));
			return true;
		}
		if(type.equals(Type.SUFFIX) && plugin.getTitleManager().suffixExists(name)){
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_suffix_exists"));
			return true;
		}
		
		// Create new title			
		Title title = new Title(name, type, null, null, null, null);
		
		// Save new title
        plugin.getTitleManager().saveTitle(title);
		
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.title_added"));
		
		return true;
	}

	private boolean cmdTitleEdit(CommandSender sender, Command cmd,  String commandlabel, String[] args) {
		if(args.length<5) return false;
		
		// Check permission
		if(!plugin.getPermissionManager().hasPermission(sender, "ds_title.edit")){
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_permission"));
			return true;
		}
		
		// Fetch args
		String name = args[2];
		String field = args[3];
		String value = "";
		for(int i=4; i<args.length; i++){
			value+= args[i] + " ";
		}
		value = value.trim();
		Type type;
		if(args[1].equals("prefix")){
			type = Type.PREFIX;
		}else if(args[1].equals("suffix")){
			type = Type.SUFFIX;
		}else{
			return false;
		}
		
		// Get title
		Title title = null;
		if(type.equals(Type.PREFIX)){
			if (plugin.getTitleManager().prefixExists(name)){
				title=plugin.getTitleManager().getPrefix(name);
			}else{
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_prefix_not_found"));
				return true;
			}
		}
		if(type.equals(Type.SUFFIX)){
			if(plugin.getTitleManager().suffixExists(name)){
				title=plugin.getTitleManager().getSuffix(name);
			}else{
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_suffix_not_found"));
				return true;
			}
		}
		
		if(field.equals("chattag")){
			title.chatTag = value;
		}else if(field.equals("headtag")){
			if(value.length()>16){
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_headtag_too_long"));
				return true;
			}
			title.headTag = value;
		}else if(field.equals("permission")){
			title.permission = value;
		}else if(field.equals("description")){
			title.description = value;
		}else{
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_valid_field"));
			return false;
		}
		
		// Save title title
        plugin.getTitleManager().saveTitle(title);
		
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.title_edited"));
		
		return true;
	}

	private boolean cmdTitleListPrefix(CommandSender sender, Command cmd, String commandlabel, String[] args){
		if(!plugin.getPermissionManager().hasPermission(sender, "ds_title.prefix.list")){
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_permission"));
			return true;
		}
		
		SortedSet<Title> titles = plugin.getTitleManager().getPrefixes();
		sendTitleList(sender, titles, true);
		return true;
	}
	
	private boolean cmdTitleListSuffix(CommandSender sender, Command cmd, String commandlabel, String[] args){
		if(!plugin.getPermissionManager().hasPermission(sender, "ds_title.suffix.list")){
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_permission"));
			return true;
		}
		
		SortedSet<Title> titles = plugin.getTitleManager().getSuffixes();
		sendTitleList(sender, titles, false);
		return true;
	}
	
	private boolean cmdTitleSetPrefix(CommandSender sender, Command cmd, String commandlabel, String[] args){
		if(args.length<3) return false;
		
		// Check if player
		Player player;
		if(sender instanceof Player){
			player = (Player)sender;
		}else{
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_player"));
			return true;
		}
		
		// Get target
		@SuppressWarnings("deprecation")
		OfflinePlayer target = (args.length>3) ? plugin.getServer().getOfflinePlayer(args[3]) : player;
		
		// Check permission
		if(target.getName().equals(player.getName())){
			if(!plugin.getPermissionManager().hasPermission(player, "ds_title.prefix.self")){
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_permission"));
				return true;
			}
		}else{
			if(!plugin.getPermissionManager().hasPermission(player, "ds_title.prefix.other")){
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_permission"));
				return true;
			}
		}
		
		// Check prefix
		if(plugin.getTitleManager().prefixExists(args[2])){
			Title title = plugin.getTitleManager().getPrefix(args[2]);
			if(title.permission==null || plugin.getPermissionManager().hasPermission(player, title.permission)){
				// Clean up previous title
				if(plugin.getConfig().getBoolean("general.use_nametag")){
					plugin.getTeamManager().removePlayerFromTeam(player);
				}
				
				// Set new title
                plugin.getTitleManager().setPlayerPrefix(title, target);
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.prefix_set"));
			}else{
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_permission"));
			}
			
		}else{
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_prefix_not_found"));
		}
		
		return true;	
	}
	
	@SuppressWarnings("deprecation")
	private boolean cmdTitleSetSuffix(CommandSender sender, Command cmd, String commandlabel, String[] args){
		if(args.length<3) return false;
		
		// Check if player
		Player player;
		if(sender instanceof Player){
			player = (Player)sender;
		}else{
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_player"));
			return true;
		}
		
		// Get target
		OfflinePlayer target = (args.length>3) ? plugin.getServer().getOfflinePlayer(args[3]) : player;
		
		// Check permission
		if(target.getName().equals(player.getName())){
			if(!plugin.getPermissionManager().hasPermission(player, "ds_title.suffix.self")){
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_permission"));
				return true;
			}
		}else{
			if(!plugin.getPermissionManager().hasPermission(player, "ds_title.suffix.other")){
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_permission"));
				return true;
			}
		}
		
		// Check suffix
		if(plugin.getTitleManager().suffixExists(args[2])){
			Title title = plugin.getTitleManager().getSuffix(args[2]);
			if(title.permission==null || plugin.getPermissionManager().hasPermission(player, title.permission)){
				// Clean up previous title
				if(plugin.getConfig().getBoolean("general.use_nametag")){
					plugin.getTeamManager().removePlayerFromTeam(player);
				}
				
				// Set new title
                plugin.getTitleManager().setPlayerSuffix(title, target);
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.suffix_set"));
			}else{
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_permission"));
			}
			
		}else{
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_suffix_not_found"));
		}
		
		return true;
	}
	
	@SuppressWarnings("deprecation")
	private boolean cmdTitleClear(CommandSender sender, Command cmd, String commandlabel, String[] args){		
		// Check if player
		Player player;
		if(sender instanceof Player){
			player = (Player)sender;
		}else{
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_player"));
			return true;
		}
		
		// Get target
		OfflinePlayer target = (args.length>1) ? plugin.getServer().getOfflinePlayer(args[1]) : player;
		
		// Check permission
		if(target.getName().equals(player.getName())){
			if(!plugin.getPermissionManager().hasPermission(player, "ds_title.clear.self")){
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_permission"));
				return true;
			}
		}else{
			if(!plugin.getPermissionManager().hasPermission(player, "ds_title.clear.other")){
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_permission"));
				return true;
			}
		}
		
		// Clear title
        plugin.getTitleManager().clearPlayerTitle(target);
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.title_cleared"));
		
		return true;
	}
	
	private boolean cmdTitleReload(CommandSender sender, Command cmd, String commandlabel, String[] args){
		if(plugin.getPermissionManager().hasPermission(sender, "ds_title.reload")){
			// Reload config
			plugin.reloadConfiguration();
			// Reload team tags
			if(plugin.getConfig().getBoolean("general.use_nametag")){
				plugin.getTeamManager().reloadTags();
			}
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.reloaded"));
			return true;
		}else{
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_permission"));
			return true;
		}
	}
	
	@SuppressWarnings("deprecation")
	private boolean cmdTitleGrant(CommandSender sender, Command cmd, String commandlabel, String[] args){
		if(plugin.getPermissionManager().hasPermission(sender, "ds_title.grant")){
			if(plugin.getPermissionManager().isVaultEnabled()){
				if(args.length<4) return false;
				Type type = args[1].equalsIgnoreCase("suffix") ? Type.SUFFIX : Type.PREFIX;
				Title title = type.equals(Type.SUFFIX) ? plugin.getTitleManager().getSuffix(args[3]) : plugin.getTitleManager().getPrefix(args[3]);
				if(title!=null){
					plugin.getPermissionManager().getVaultPermissionInstance().playerAdd((String)null, args[2], title.permission);
					if(type.equals(Type.PREFIX)){
						plugin.sendMessage(sender, plugin.getConfig().getString("messages.prefix_granted").replace("{title}", title.name).replace("{name}", args[2]));
					}else{
						plugin.sendMessage(sender, plugin.getConfig().getString("messages.suffix_granted").replace("{title}", title.name).replace("{name}", args[2]));
					}
				}else{
					if(type.equals(Type.PREFIX)){
						plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_prefix_not_found"));
					}else{
						plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_suffix_not_found"));
					}
				}
				
			}else{
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_vault"));
			}			
			return true;
		}else{
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_permission"));
			return true;
		}
	}
	
	@SuppressWarnings("deprecation")
	private boolean cmdTitleUngrant(CommandSender sender, Command cmd, String commandlabel, String[] args){
		if(plugin.getPermissionManager().hasPermission(sender, "ds_title.ungrant")){
			if(plugin.getPermissionManager().isVaultEnabled()){
				if(args.length<4) return false;
				Type type = args[1].equalsIgnoreCase("suffix") ? Type.SUFFIX : Type.PREFIX;
				Title title = type.equals(Type.SUFFIX) ? plugin.getTitleManager().getSuffix(args[3]) : plugin.getTitleManager().getPrefix(args[3]);
				if(title!=null){
					plugin.getPermissionManager().getVaultPermissionInstance().playerRemove((String)null, args[2], title.permission);
					if(type.equals(Type.PREFIX)){
						plugin.sendMessage(sender, plugin.getConfig().getString("messages.prefix_ungranted").replace("{title}", title.name).replace("{name}", args[2]));
					}else{
						plugin.sendMessage(sender, plugin.getConfig().getString("messages.suffix_ungranted").replace("{title}", title.name).replace("{name}", args[2]));
					}
				}else{
					if(type.equals(Type.PREFIX)){
						plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_prefix_not_found"));
					}else{
						plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_suffix_not_found"));
					}
				}				
			}else{
				plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_vault"));
			}			
			return true;
		}else{
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.error_no_permission"));
			return true;
		}
	}

	private void sendTitleList(CommandSender sender, SortedSet<Title> titles, boolean prefix){
		List<String> available = new ArrayList<String>();
		List<String> unavailable = new ArrayList<String>();
		List<String> availableTitles = new ArrayList<String>();
		List<String> unavailableTitles = new ArrayList<String>();
		String hoverTextMask = plugin.getConfig().getString("messages.title_available_hovertext");
		String description, preview, listitem;
		
		for(Title title: titles){
			description = title.description==null ? "-" : title.description;
			if(title.chatTag!=null && title.chatTag!=""){
				preview = title.chatTag;
			}else{
				preview = "";
			}
			if(title.headTag!=null && plugin.getConfig().getBoolean("general.use_nametag")){
				if(title.chatTag!=null && title.chatTag!=""){
					preview = preview + "&r&8/&r" +  title.headTag;
				}else{
					preview = preview + title.headTag;
				}			  
			}			
			if(title.permission==null || plugin.getPermissionManager().hasPermission(sender, title.permission)){
				String listitemMask = plugin.getConfig().getString("messages.title_listitem_available");
				listitem = ChatColor.WHITE + ": " + ChatColor.RESET + listitemMask
						.replace("{preview}", preview + "&r")
						.replace("{description}", description);
				available.add(listitem);
				availableTitles.add(title.name);
			}else{
				String listitemMask = plugin.getConfig().getString("messages.title_listitem_unavailable");
				listitem = ChatColor.WHITE + ": " + ChatColor.RESET + listitemMask
						.replace("{preview}", preview + "&r")
						.replace("{description}", description);
				unavailable.add(listitem);
				unavailableTitles.add(title.name);
			}
		}
		plugin.sendMessage(sender, plugin.getConfig().getString("messages.available_header"));
		if(available.size()!=0 && available!=null){
			for(int i = 0; i < available.size(); i++){
				if(sender instanceof Player){
					TextComponent message = new TextComponent(TextComponent.fromLegacyText(ChatColor.BOLD + availableTitles.get(i) + ChatColor.RESET));
					TextComponent extra = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', available.get(i))));
					String hoverText = hoverTextMask
							.replace("{title}", availableTitles.get(i));
					if(prefix)
						message.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, ("/title prefix set " + availableTitles.get(i)) ) );						
					else
						message.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, ("/title suffix set " + availableTitles.get(i)) ) );
					message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',hoverText)) ) );				
					message.addExtra(extra);
					((Player) sender).spigot().sendMessage( message );
				}		
			}
		}		
		if(plugin.getConfig().getBoolean("general.show_unavailable_titles")){
			plugin.sendMessage(sender, plugin.getConfig().getString("messages.unavailable_header"));
			if(unavailable.size()!=0 && unavailable!=null){
				for(int i = 0; i < unavailable.size(); i++){
					plugin.sendMessage(sender, ChatColor.BOLD + unavailableTitles.get(i) + ChatColor.RESET + unavailable.get(i));
				}
			}		
		}
	}
}
