package denniss17.dsTitle;

import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.kaltiz.dsTitle.TitleManager;
import com.kaltiz.dsTitle.storage.SQLTitleStorage;
import com.kaltiz.dsTitle.storage.TitleStorage;
import com.kaltiz.dsTitle.storage.YMLTitleStorage;

public class DSTitle extends JavaPlugin{	
	private PermissionManager permissionManager;
	private TeamManager teamManager;
    private TitleManager titleManager;
    private TitleStorage storage;
	
	private static final int projectID = 51865;
	public static VersionChecker versionChecker;
	public static DSTitle title;
	public boolean placeHolders = false;

	/**
	 * Enable this plugin
	 */
	public void onEnable(){
		title = this;
		super.onEnable();
		if(Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI"))
		{
			if(denniss17.dsTitle.Placeholders.mvdwPlaceholderAPIHook.dsTitlePrefixHook()&&
			denniss17.dsTitle.Placeholders.mvdwPlaceholderAPIHook.dsTitleSuffixHook()){
				getLogger().info("dsTitle was successfully registered with mvdwPlaceholderAPI!");
				placeHolders = true;
			}
		}
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
		{
			if (denniss17.dsTitle.Placeholders.PlaceholderAPIHook.RegisterPlaceHolderHooks(title)) {
		        getLogger().info("dsTitle was successfully registered with PlaceHolderAPI!");
		        placeHolders = true;
		    }
		}
		if(Bukkit.getPluginManager().isPluginEnabled("DeluxeChat"))
		{
			try {
				if (denniss17.dsTitle.Placeholders.DeluxeChatHook.RegisterDeluxeChatHooks(title)) {
				    getLogger().info("dsTitle was successfully registered with DeluxeChat!");
				    placeHolders = true;
				}
			} catch (ClassNotFoundException e) {
				getLogger().info("dsTitle found DeluxeChat, but it does not seem to be the correct version.");
			}
		}
		// Register listeners
		Listener playerListener = new PlayerListener(this, !this.getConfig().getBoolean("general.use_deprecated_listener"));
		this.getServer().getPluginManager().registerEvents(playerListener, this);
		
		// Set the command executors
		CommandExec commandExec = new CommandExec(this);
		this.getCommand("title").setExecutor(commandExec);
		
		this.permissionManager = new PermissionManager(this);
		this.teamManager = new TeamManager(this);
		
		// Load the config values
		reloadConfiguration();
		
		if(getConfig().getBoolean("general.use_nametag")){
			teamManager.reloadTags();
		}
		// Clean up teams, especialy when name tags are disabled
		teamManager.cleanUpTeams(!getConfig().getBoolean("general.use_nametag"));		
		
		// Check for newer versions
		if(this.getConfig().getBoolean("general.check_for_updates")){
			activateVersionChecker();
        }
        this.getLogger().info("Loaded!");
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		if(placeHolders){
			if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
			{
				if (denniss17.dsTitle.Placeholders.PlaceholderAPIHook.unRegisterPlaceHolderHooks(title)) {
					getLogger().info("dsTitle was successfully unregistered with PlaceHolderAPI!");
				}
			}
			if(Bukkit.getPluginManager().isPluginEnabled("DeluxeChat"))
			{
				if (denniss17.dsTitle.Placeholders.DeluxeChatHook.unRegisterDeluxeChatHooks(title)) {
			        getLogger().info("dsTitle was successfully unregistered with DeluxeChat!");
			    }
			}
		}
		if(this.storage instanceof SQLTitleStorage){
			try {
				((SQLTitleStorage)this.storage).closeConnection();
			} catch (SQLException e) {
				
			}
		}
	}
	
	/**
	 * Activate the versionCheckerThread to run on a timer
	 */
	private void activateVersionChecker(){
		versionChecker = new VersionChecker(this, projectID);
        if(versionChecker!=null)
        	versionChecker.activate(this.getConfig().getInt("general.update_check_interval") * 60 * 20);
	}
	
	/**
	 * Reload the configuration
	 */
	public void reloadConfiguration(){
		// Reload config
		this.reloadConfig();
		this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        // Reload the Storage
        String type = getConfig().getString("storage.type");

        // Reload the TitleManager
        this.titleManager = new TitleManager(this);

        // Default to YML Storage
        if((type.equalsIgnoreCase("database")))
        {
            try {
                this.storage = new SQLTitleStorage(this,titleManager);
            }
            catch( SQLException ex){
                getLogger().log(Level.SEVERE,"Could not create SQLStorage, falling back to file storage");
                getLogger().log(Level.SEVERE,"Reason: " + ex.getMessage());
                // Fall Back to YML
                this.storage = new YMLTitleStorage(this,titleManager);
            }
        }
        else
        {
            this.storage = new YMLTitleStorage(this,titleManager);
        }
        
        // Reset buffers
        PlayerListener.prefixTag = getConfig().getString("general.chat_format_prefix_tag", "[titleprefix]");
		PlayerListener.suffixTag = getConfig().getString("general.chat_format_suffix_tag", "[titlesuffix]");
		PlayerListener.playerTag = getConfig().getString("general.chat_format_player_tag", "%1$s");
	}
	
	/**
	 * Get the PermissionManager responsible for managing permissions
	 * @return PermissionManager
	 */
	public PermissionManager getPermissionManager(){
		return this.permissionManager;
	}
	
	public TeamManager getTeamManager() {
		return this.teamManager;
	}

    public TitleStorage getStorage()
    {
        return this.storage;
    }

    public TitleManager getTitleManager()
    {
        return this.titleManager;
    }

	/** 
	 * Send a message to a receiver.
	 * This method applies chat styles to the message
	 * @param receiver CommandSender the receiver of the message
	 * @param message The message to send
	 */
	public void sendMessage(CommandSender receiver, String message){
		receiver.sendMessage(ChatStyler.setTotalStyle(message));
	}
	
	
}
