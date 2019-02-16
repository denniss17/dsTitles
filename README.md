dsTitles
========

This is a bukkit plugin which adds titles to Minecraft.

New Features to this Fork :
* Registers Placeholders into PlaceholderAPI/DeluxeChat for dsTitle Suffixes/Prefixes. Use these in PlaceholderAPI and DeluxeChat by using %dsTitle_prefix% for prefix or %dsTitle_suffix% for suffix
* Registers PlaceHolders into mvdwPlaceholderAPI for dsTitle Suffixes/Prefixes. Use these in any mvdw plugin if you have mvdwPlaceholderAPI installed by using {dstitle_prefix}
* Ability to set a default prefix and default suffix
* Able to set prefix/suffix just by clicking on the prefix/suffix in the titles list (Json style messages)
* Fixed a few bugs, including the invisible players still having their nametags shown when dsTitle is handling nametags

For more info go to http://dev.bukkit.org/server-mods/dstitle/

To compile, make sure you are running at least java 1.7 and Apache Maven.
Download the contents of this repo, then, from the command line,
navigate to the where you've stored the contents of this repo and run

mvn clean package

From the Command Line. The result will be located in the target directory.
