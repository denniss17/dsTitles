package denniss17.dsTitle;

/**
 * Class representing a single title
 */
public class Title implements Comparable<Title>
{
	public enum Type {
		PREFIX("prefixes"), 
		SUFFIX("suffixes");
		
		private String key;

		Type(String key){
			this.key = key;
		}
	
		public String getKey() {
			return this.key;
		}
	};
	public String name = "";
	public Type type;
	public String chatTag = "";
	public String headTag = "";
	public String permission = "";
	public String description = "";
	
	public Title(String name, Type type, String chatTag, String headTag, String permission, String description)
    {
		this.name = name;
		this.type = type;
		this.chatTag = chatTag;
		this.headTag = headTag;
		this.permission = permission;
		this.description = description;
	}
	
	@Override
	public int compareTo(Title otherTitle) {
		return otherTitle.name.compareTo(this.name);
	}
	
}
