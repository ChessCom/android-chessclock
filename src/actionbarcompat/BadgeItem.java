package actionbarcompat;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 31.12.12
 * Time: 7:40
 */
public class BadgeItem {
	private int menuItemId;
	private int value;

	public BadgeItem(int menuItemId, int value) {
		this.menuItemId = menuItemId;
		this.value = value;
	}

	public int getMenuItemId() {
		return menuItemId;
	}

	public int getValue() {
		return value;
	}
}
