package gui.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.DefaultListModel;

public class SortedListModel extends DefaultListModel<DroneIP> {
	private static final long serialVersionUID = 4810992893657248672L;

	private Vector<DroneIP> model = new Vector<DroneIP>();

	public int getSize() {
		return model.size();
	}

	@Override
	public DroneIP getElementAt(int index) {
		return model.get(index);
	}

	public void addElement(DroneIP element) {
		int index = model.size();
		model.add(element);
		Collections.sort(model, new SortIpList());
		fireIntervalAdded(this, index, index);
	}

	public DroneIP remove(int index) {
		DroneIP rv = model.get(index);
		model.remove(index);
		fireIntervalRemoved(this, index, index);
		return rv;
	}

	public boolean contains(DroneIP element) {
		return model.contains(element);
	}
	
	public DroneIP getDroneWithIP(String address){
		for (DroneIP droneIP : model) {
			if(droneIP.getIp().equals(address))
				return droneIP;
		}
		return null;
	}

	public Object[] toArray() {
		Object[] rv = new Object[model.size()];
		model.copyInto(rv);
		return rv;
	}

	public boolean isEmpty() {
		return model.isEmpty();
	}

	public DroneIP get(int index) {
		return model.get(index);
	}

	class SortIpList implements Comparator<DroneIP> {
		@Override
		public int compare(DroneIP o1, DroneIP o2) {
			Long value1 = ipToInt(o1.getIp());
			Long value2 = ipToInt(o2.getIp());
			
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