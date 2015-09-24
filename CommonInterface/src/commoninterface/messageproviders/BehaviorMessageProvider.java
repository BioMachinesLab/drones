package commoninterface.messageproviders;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import commoninterface.CIBehavior;
import commoninterface.RobotCI;
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.utils.CIArguments;
import commoninterface.utils.ClassLoadHelper;

public class BehaviorMessageProvider implements MessageProvider {

	private RobotCI robot;

	public BehaviorMessageProvider(RobotCI drone) {
		this.robot = drone;
	}

	@Override
	public Message getMessage(Message m) {

		if (m instanceof BehaviorMessage) {
			BehaviorMessage bm = (BehaviorMessage) m;
			
			if (bm.getSelectedStatus()) {
				try {

					ArrayList<Class<?>> classes = ClassLoadHelper
							.findRelatedClasses(bm.getSelectedBehavior());

					if (classes == null || classes.isEmpty()) {
						return null;
					}
					
					Class<CIBehavior> chosenClass = null;
					
					for(Object b : classes) {
						Class<CIBehavior> ci = (Class<CIBehavior>) b;
						if(ci.getCanonicalName().endsWith("."+bm.getSelectedBehavior()))
							chosenClass = ci;
					}
					
					Constructor<CIBehavior> constructor = chosenClass
							.getConstructor(new Class[] { CIArguments.class,
									RobotCI.class });
					CIBehavior ctArgs = constructor.newInstance(new Object[] {
							new CIArguments(bm.getArguments()), robot });
					robot.startBehavior(ctArgs);
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			} else {
				robot.stopActiveBehavior();
			}
			Message response = new BehaviorMessage(bm.getSelectedBehavior(),
					"", bm.getSelectedStatus(), robot.getNetworkAddress());
			return response;
		}

		return null;
	}

}
