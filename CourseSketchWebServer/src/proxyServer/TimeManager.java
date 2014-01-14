package proxyServer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class TimeManager {
	public static final int EXPERATION_TIME= 1800000;

	private Queue<ProxyConnectionState> userTimeManager = new LinkedList<ProxyConnectionState>();	
	private ActionListener expiredListener;

	public TimeManager() {
		new Thread() {
			public void run() {
				while(true) {
					if(userTimeManager.isEmpty())
						try {
							Thread.sleep(180000);
						}catch(InterruptedException e) {}
					
					while(userTimeManager.peek().getTimeSinceLastActive() >= EXPERATION_TIME)
					{
						ProxyConnectionState removedUser = userTimeManager.poll();
						if (expiredListener != null) {
							expiredListener.actionPerformed(new ActionEvent(this, 0, removedUser.getKey()));
						}
						// call listener in proxy 
					}
					try {
						Thread.sleep(120000);
					}catch(InterruptedException e) {}
				}
			}
		};
	}
	
	public void updateManager(ProxyConnectionState userID){
		for(int i = 0;i < userTimeManager.size(); i++)
		{
			if(((LinkedList<ProxyConnectionState>) userTimeManager).get(i).getUserId() == userID.getUserId())
			{
				userTimeManager.remove(i);
				userTimeManager.add(userID);
			}
		}
		
	}
	
	public void deleteManager(ProxyConnectionState userID){
		userTimeManager.remove(userID);
		
	}
	
	public void addToManager(ProxyConnectionState userID){
		userTimeManager.add(userID);
		
	}
	
	public setExpiredListiner(){
		
		
	}
	
}
