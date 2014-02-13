package quickaction;

import com.chess.R;
import com.chess.model.SelectionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 16.12.13
 * Time: 11:14
 */
public class MultiActionItem {

	private int iconId;
	private List<SelectionItem> items;

	public MultiActionItem() {
		iconId = R.string.ic_blocking;
		items = new ArrayList<SelectionItem>();
	}

	public MultiActionItem(int iconId, List<SelectionItem> items) {
		this.iconId = iconId;
		this.items = items;
	}

	public int getIconId() {
		return iconId;
	}

	public void setIconId(int iconId) {
		this.iconId = iconId;
	}

	public List<SelectionItem> getItems() {
		return items;
	}

	public void setItems(List<SelectionItem> items) {
		this.items = items;
	}
}
