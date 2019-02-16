package com.kaltiz.dsTitle;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import denniss17.dsTitle.DSTitle;
import denniss17.dsTitle.Title;
import denniss17.dsTitle.Title.Type;

/**
 * This class is responsible for managing titles of players.
 * It also functions as a buffer to avoid unnecessary memory access.
 * @author Kaltiz (and denniss17)
 */
public class TitleManager {
	private static final String TITLE_CONFIG_FILENAME = "titleConfig.yml";
	
	/**
	 * The main plugin object
	 */
    private final DSTitle plugin;

    /** The FileConfiguration containing all titles */
    private FileConfiguration titleConfig = null;
    /** The File which contains the titleConfig */
    private File titleConfigFile = null;

    /** Buffer containing prefixes */
    private HashMap<String,Title> prefixes = new HashMap<>();
    /** Buffer containing suffixes */
    private HashMap<String,Title> suffixes = new HashMap<>();
    /** Buffer containing who has which prefix (by name) */
    private HashMap<String,String> playerPrefixes = new HashMap<>();
    /** Buffer containing who has which suffix (by name) */
    private HashMap<String,String> playerSuffixes = new HashMap<>();

    public TitleManager(DSTitle plugin)
    {
        this.plugin = plugin;
        titleConfigFile = new File(plugin.getDataFolder(), TITLE_CONFIG_FILENAME);
        titleConfig = YamlConfiguration.loadConfiguration(titleConfigFile);
        
        // Set correct keys in the file
        if (titleConfigFile.exists()) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(titleConfigFile);
            titleConfig.setDefaults(defConfig);
        }
    	titleConfig.options().copyDefaults(true);
    	this.saveTitleConfig();
    	
        prefixes.clear();
        suffixes.clear();
        loadTitles();
    }

    /**
     * Loads all Title into the Character Storage
     */
    private void loadTitles()
    {
        Title title;
        if(titleConfig.contains(Type.SUFFIX.getKey()))
        {
            Set<String> titles = titleConfig.getConfigurationSection(Type.SUFFIX.getKey()).getKeys(false);
            for(String name: titles){
                title = loadTitle(Type.SUFFIX, name);
                if (title != null) suffixes.put(name,title);
            }
        }

        if(titleConfig.contains(Type.PREFIX.getKey()))
        {
            Set<String> titles = titleConfig.getConfigurationSection(Type.PREFIX.getKey()).getKeys(false);
            for(String name: titles){
                title = loadTitle(Type.PREFIX, name);
                if (title != null) prefixes.put(name,title);
            }
        }
    }

    /**
     * Loads a Title from the TitleConfig
     * @param type Suffix or Prefix
     * @param name name of the title to load
     * @return void
     */
    private Title loadTitle(Type type, String name)
    {
        String path = type.getKey() + '.' + name;

        if(path==null||path.equals("")){
            return null;
        }

        ConfigurationSection titleSection = titleConfig.getConfigurationSection(path);

        if(titleSection!=null)
        {
        	// These values could be null !
            String permission   = titleSection.getString("permission");
            String description  = titleSection.getString("description");
            String chatTag      = titleSection.getString("chattag");
            String headTag      = titleSection.getString("headtag");
            if (headTag!=null && headTag.length() > 16)
            {
                plugin.getLogger().warning("Title  '" + name + "has been disabled!");
                plugin.getLogger().warning("The headtag cannot be longer than 16 characters, as this would kick every online player from the server");
                return null;
            }
            return new Title(name, type, chatTag, headTag, permission, description);
        }
        else
        {
            plugin.getLogger().warning(name + "' not good configured and can't be used!");
            return null;
        }
    }

    /**
     * Get the Title a player has set currently as prefix
     * @param target the player
     * @return Title or null if the player has no prefix
     */
    public Title getPlayerPrefix(OfflinePlayer target)
    {	
    	if(target!=null){
    		if(playerPrefixes.containsKey(target.getName())){
                String title = playerPrefixes.get(target.getName());
                return prefixes.get(title);
            }          
    	}
        return null;
    }
    
    /**
     * Get the Prefix a player has set currently
     * @param target the player
     * @return Title or null if the player has no prefix
     */
    public String getPrefixChatTag(OfflinePlayer target){
    	if(plugin.getConfig().getBoolean("general.use_chattag") || plugin.placeHolders){
	    	String chatTag;
	    	if(target!=null){
	    		if(getPlayerPrefix(target).chatTag!=null){
	    			chatTag = getPlayerPrefix(target).chatTag;
			    	if(!chatTag.equals(null) && !chatTag.equals("")){
			    		return chatTag;
			    	}
	    		}    		
	    	}    	
	    	return "";
    	}
    	return "ChatTags are Disabled";
    }

    /**
     * Get the Title a player has set currently as suffix
     * @param target the player
     * @return Title or null if the player has no suffix
     */
    public Title getPlayerSuffix(OfflinePlayer target)
    {
    	if(target!=null){
    		if(playerSuffixes.containsKey(target.getName())){
                String title = playerSuffixes.get(target.getName());
                return suffixes.get(title);
            }
    	}
        return null;
    }
    
    /**
     * Get the Suffix a player has set currently
     * @param target the player
     * @return Title or null if the player has no suffix
     */

    public String getSuffixChatTag(OfflinePlayer target){
    	if(plugin.getConfig().getBoolean("general.use_chattag") || plugin.placeHolders){
	    	String chatTag;
	    	if(target!=null){
	    		if(getPlayerSuffix(target).chatTag!=null){
	    			chatTag = getPlayerSuffix(target).chatTag;
			    	if(!chatTag.equals(null) && !chatTag.equals("")){
			    		return chatTag;
			    	}
	    		}    		
	    	}    	
	    	return "";
    	}
    	return "ChatTags are Disabled";
    }

    /**
     * Sets the Players Prefix, Only in Memory
     * @param title the title to set
     * @param target The Player
     */
    @SuppressWarnings("deprecation")
	public void setPlayerPrefix(Title title,OfflinePlayer target)
    {
        if(plugin.getConfig().getBoolean("general.use_nametag"))
        {
            plugin.getTeamManager().getTeam(title, getPlayerSuffix(target), target.getPlayer()).addPlayer(target);
        }
        if(title==null){
        	playerPrefixes.remove(target.getName());
        }else{
        	playerPrefixes.put(target.getName(), title.name);
        }
        // Save Changes
        plugin.getStorage().saveTitlesPlayer(target);
    }

    /**
     * Sets the Players Suffix, Only in Memory
     * @param title the title to set
     * @param target The Player
     */
    public void setPlayerPrefix(String title, OfflinePlayer target)
    {
        setPlayerPrefix(getPrefix(title), target);
    }

    /**
     * Sets the Players Suffix, Only in Memory
     * @param title the title to set
     * @param target The Player
     */
    @SuppressWarnings("deprecation")
	public void setPlayerSuffix(Title title, OfflinePlayer target)
    {
    	if(plugin.getConfig().getBoolean("general.use_nametag"))
        {
            plugin.getTeamManager().getTeam(getPlayerPrefix(target), title, target.getPlayer()).addPlayer(target);
        }
        if(title==null){
        	playerSuffixes.remove(target.getName());
        }else{
        	playerSuffixes.put(target.getName(), title.name);
        }
        // Save Changes
        plugin.getStorage().saveTitlesPlayer(target);
    }

    /**
     * Sets the Players Suffix, Only in Memory
     * @param title the title to set
     * @param target The Player
     */
    public void setPlayerSuffix(String title,OfflinePlayer target)
    {
    	setPlayerSuffix(getSuffix(title), target);
    }

    /**
     * Clears the Players titles
     * @param target player to remove titles from
     */
    public void clearPlayerTitle(OfflinePlayer target)
    {
        playerPrefixes.remove(target.getName());
        playerSuffixes.remove(target.getName());
        // Save Changes
        plugin.getStorage().saveTitlesPlayer(target);
    }

    /**
     * Get an ordered set of prefixes.
     * @return SortedSet: a sorted set of all suffixes
     */
    public SortedSet<Title> getPrefixes()
    {
        return new TreeSet<Title>(prefixes.values());
    }

    /**
     * Get an ordered set of suffixes.
     * @return SortedSet: a sorted set of all suffixes
     */
    public SortedSet<Title> getSuffixes()
    {
        return new TreeSet<Title>(suffixes.values());
    }

    /**
     * Check if a prefix with this name exists
     * @param name The name of the title to search for
     * @return true if it exists, false otherwise
     */
    public boolean prefixExists(String name)
    {
        return prefixes.containsKey(name);
    }

    /**
     * Check if a suffix with this name exists
     * @param name The name of the title to search for
     * @return true if it exists, false otherwise
     */
    public boolean suffixExists(String name)
    {
        return suffixes.containsKey(name);
    }

    /**
     * Gets a Prefix
     * @param name The name of the suffix
     * @return A Title instance
     * @ensure result.name.equals(name)
     */
    public Title getPrefix(String name){
        if(name==null||name.equals(""))
            return null;

        return prefixes.get(name);
    }

    /**
     * Gets a Suffix
     * @param name The name of the suffix
     * @return A Title instance
     * @ensure result.name.equals(name)
     */
    public Title getSuffix(String name){
        if(name==null||name.equals(""))
            return null;

        return suffixes.get(name);
    }

    public void saveTitle(Title title)
    {
        // Get path
        String path;
        if(title.type.equals(Type.PREFIX))
            path = Type.PREFIX.getKey() + "." + title.name + ".";
        else
            path = Type.SUFFIX.getKey() + "." + title.name + ".";

        // Save title
        titleConfig.set(path + "permission", title.permission);
        titleConfig.set(path + "description", title.description);
        titleConfig.set(path + "chattag", title.chatTag);
        titleConfig.set(path + "headtag", title.headTag);

        saveTitleConfig();

        // Update it in the Hashmaps
        if(title.type.equals(Type.PREFIX))
            prefixes.put(title.name,title);
        else
            suffixes.put(title.name,title);

        // Reload headtags
        plugin.getTeamManager().reloadTags();
    }

    protected void saveTitleConfig() {
        if (titleConfig == null || titleConfigFile == null) {
            return;
        }
        try {
            titleConfig.save(titleConfigFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + titleConfigFile, ex);
        }
    }

	public String getDefaultPrefix() {
		return titleConfig.getString("default_prefix");
	}

	public String getDefaultSuffix() {
		return titleConfig.getString("default_prefix");
	}
}



