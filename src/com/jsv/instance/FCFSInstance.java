package com.jsv.instance;

/**
 * It implements a First Come First Serve. Sorts by id. 
 */
public class FCFSInstance extends Instance {

	public FCFSInstance(int pid, int startTime) {
		super(pid, startTime);
	}
	
	@Override
	public int compareTo(Instance o) {
		if(super.getPid() < o.getPid()) {
			return -1;
		}
		
		return 1;
	}

}