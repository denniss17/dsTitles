package com.kaltiz.dsTitle.storage;

import com.kaltiz.dsTitle.TitleManager;

import denniss17.dsTitle.DSTitle;
import denniss17.dsTitle.Title;

import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.logging.Level;

public class SQLTitleStorage extends TitleStorage {

    protected DatabaseType driver;
    protected String url = "";
    protected String username = "";
    protected String password = "";

    private Connection conn = null;

    public SQLTitleStorage(DSTitle plugin, TitleManager manager) throws SQLException
    {
        super(plugin,manager);

        this.driver = DatabaseType.match(plugin.getConfig().getString("storage.database.driver"));
        if(this.driver!=null) {
        	if(this.driver.equals(DatabaseType.SQLITE)){
            	this.url = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + System.getProperty("file.separator") + plugin.getConfig().getString("storage.database.url");
            }else{
            	this.url = "jdbc:" + plugin.getConfig().getString("storage.database.url") + "?useSSL=" + plugin.getConfig().getString("storage.database.useSSL");
            }
            this.username = plugin.getConfig().getString("storage.database.username");
            this.password = plugin.getConfig().getString("storage.database.password");

            if (!loadDriver()) {
                throw new SQLException("Couldn't load driver");
            }

            this.conn = getConnection();
            
            if (conn==null) {
                throw new SQLException("Couldn't connect to the database");
            }

            // Create table
            String qry = "CREATE TABLE IF NOT EXISTS `players` (`name` VARCHAR(16) NOT NULL PRIMARY KEY, `prefix` VARCHAR(32), `suffix` VARCHAR(32));";
            Statement stmt = this.conn.createStatement();
            stmt.execute(qry);
        }else {
        	plugin.getLogger().info("Database needs a type set. Possible values: H2, MYSQL, POSTGRE, SQLITE");
        }      
    }

    private boolean loadDriver()
    {
        try {
            this.getClass().getClassLoader().loadClass(this.driver.driver).newInstance();
            return true;
        } catch (IllegalAccessException e) {
            // Constructor is private, OK for DriverManager contract
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Connection getConnection() throws SQLException{
        /*if (conn != null) {
            // Make a dummy query to check the connection is alive.
            try {
                if (conn.isClosed()) {
                    conn = null;
                } else {
                    conn.prepareStatement("SELECT 1;").execute();
                }
            } catch (SQLException ex) {

            }
        }*/
        if (conn == null || conn.isClosed()) {
            conn = (username.isEmpty() && password.isEmpty()) ? DriverManager.getConnection(url) : DriverManager.getConnection(url, username, password);
        }
        // The connection could be null here (!)
        return conn;
    }
    
    public void closeConnection() throws SQLException{
    	 if (conn != null && !conn.isClosed()) conn.close();
    }

    @Override
    public void loadTitlesPlayer(OfflinePlayer target)
    {
        String prefix = plugin.getTitleManager().getDefaultPrefix();
        String suffix = plugin.getTitleManager().getDefaultSuffix();
        String qry = "SELECT * FROM `players` WHERE `name` = ?;";
        try {
            PreparedStatement stmt = this.getConnection().prepareStatement(qry);
            stmt.setString(1, target.getName());
            ResultSet result = stmt.executeQuery();

            if(result.next())
            {
                prefix = result.getString("prefix");
                suffix = result.getString("suffix");
            }
        }
        catch (SQLException ex)
        {
            plugin.getLogger().log(Level.SEVERE,"Could not load titles of player " + target.getName());
            plugin.getLogger().log(Level.SEVERE,"Reason: " + ex.getMessage());
        }

        manager.setPlayerPrefix(prefix, target);
        manager.setPlayerSuffix(suffix, target);
    }

    @SuppressWarnings("resource")
	@Override
    public void saveTitlesPlayer(OfflinePlayer target)
    {
        Title p = manager.getPlayerPrefix(target);
        Title s = manager.getPlayerSuffix(target);

        String prefix = p == null ? null : p.name;
        String suffix = s == null ? null : s.name;

        // Check if the Player has an Existing Row
        try{
            String existing;
            String qry = "SELECT `name` FROM `players` WHERE `name` = ?;";
            PreparedStatement stmt = this.conn.prepareStatement(qry);
            stmt.setString(1, target.getName());
            
            ResultSet result = stmt.executeQuery();
            existing = result.next() ? result.getString("name") : null;
            stmt.close();

            if(existing != null){
            	stmt = this.getConnection().prepareStatement("UPDATE `players` SET `prefix` = ?, `suffix` = ? WHERE `name` = ?;");
            	stmt.setString(1, prefix);
            	stmt.setString(2, suffix);
            	stmt.setString(3, target.getName());
            }else{
            	stmt = this.getConnection().prepareStatement("INSERT INTO `players` VALUES (?, ?, ?);");
            	stmt.setString(1, target.getName());
            	stmt.setString(2, prefix);
            	stmt.setString(3, suffix);
            }
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException ex) {
        	plugin.getLogger().log(Level.SEVERE,"Could not save titles of player " + target.getName());
            plugin.getLogger().log(Level.SEVERE,"Reason: " + ex.getMessage());
        }
    }
}
