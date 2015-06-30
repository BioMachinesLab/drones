package gui.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.DefaultListModel;

public class SortedListModel extends DefaultListModel<String> {
	private static final long serialVersionUID = 4810992893657248672L;

	private Vector<String> model = new Vector<String>();

	public int getSize() {
		return model.size();
	}

	@Override
	public String getElementAt(int index) {
		return model.get(index);
	}

	public void addElement(String element) {
		int index = model.size();
		model.add(element);
		Collections.sort(model, new SortIpList());
		fireIntervalAdded(this, index, index);
	}

	public String remove(int index) {
		String rv = model.get(index);
		model.remove(index);
		fireIntervalRemoved(this, index, index);
		return rv;
	}

	public boolean contains(String element) {
		return model.contains(element);
	}

	public Object[] toArray() {
		Object[] rv = new Object[model.size()];
		model.copyInto(rv);
		return rv;
	}

	public boolean isEmpty() {
		return model.isEmpty();
	}

	public String get(int index) {
		return model.get(index);
	}

	class SortIpList implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			Long value1 = ipToInt(o1);
			Long value2 = ipToInt(o2);
			
			return value1.compareTo(value2);
		}

		private Long ipToInt(String addr) {
			String[] addrArray = addr.split("\\.");

			long num = 0;
			for (int i = 0; i < addrArray.length; i++) {
				int power = 3 - i;

				num += ((Integer.parseInt(addrArray[i]) % 256 * Math.pow(256,power)));
			}
			return num;
		}

	}

}