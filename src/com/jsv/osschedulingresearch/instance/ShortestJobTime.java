package com.jsv.osschedulingresearch.instance;

/**
 * The first command of an Instance is CPU. We take the first command and
 * sort by the shortest time
 */
public class ShortestJobTime extends Instance {

	public ShortestJobTime(int pid, int startTime) {
		super(pid, startTime);
	}
	
	@Override
	/** Comparing the first command in the instance and sorting by that 
	 * time.
	 */
	public int compareTo(Instance instance) {
		//System.out.println("This: " + this.getPid() + " - instance: " + instance.getPid());
		
		int thisNextTime = super.getCommandList().get(0).getTimeCommand();
		int otherNextTime = instance.getCommandList().get(0).getTimeCommand();
		
		if(thisNextTime < otherNextTime) {
			return -1;
		}
		else if(thisNextTime > otherNextTime) {
			return 1;
		}
		
		return 0;
	}

}
