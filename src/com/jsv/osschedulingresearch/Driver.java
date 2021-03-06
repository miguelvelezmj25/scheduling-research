package com.jsv.osschedulingresearch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import com.jsv.osschedulingresearch.command.Command;
import com.jsv.osschedulingresearch.hardware.CPUList;
import com.jsv.osschedulingresearch.hardware.OTH;
import com.jsv.osschedulingresearch.instance.*;

public class Driver {
	  
	private static final String NEW = "NEW";
	private static final String CPU = "CPU";
	private static final String OTH = "OTH";
	private static final int NUM_CPUS = 16;
	public static int clock = 0; //Start at time 0
	public static int QUANTUM_SIZE = 5;
		
	
	/** Alternate CPU and other call. Always start with CPU. 
	 * @throws IOException 
	 * */
	public static void main(String[] args) throws IOException {				
		List<Instance> instanceTable = new LinkedList<Instance>();
		
		Driver.readInput(instanceTable);
						
		Queue<OTH> othQueue = new PriorityQueue<OTH>();
		ArrayList<Instance> finishedList = new ArrayList<Instance>();
		
		int originalLength = instanceTable.size();
		CPUList cpuList = new CPUList(Driver.NUM_CPUS);
		
		int command;
		
		while(finishedList.size() != originalLength)
		{
			Driver.clock = nextImportantEvent(othQueue, instanceTable, cpuList);
			command = Driver.checkTimes(othQueue, instanceTable, cpuList, Driver.clock);
			
			//System.out.println("\tCommand: " + command);
			Instance instance;
			
			if(command==2) //0 = CPU, 1 = OTH, 2 = New
			{
				cpuList.add(instanceTable.remove(0));
			}
			else if(command==1) //0 = CPU, 1 = OTH, 2 = New
			{
				instance = othQueue.poll().getInstance();
				instance.removeCommand();
				
				if(instance.isEmpty())
				{
//					System.out.println("Instance " + instance.getPid() + " is done");
					finishedList.add(instance);
				}
				else
				{
					instance.zeroQuanta();
					cpuList.add(instance);
				}				

			}
			else if(command==0) //0 = CPU, 1 = OTH, 2 = New
			{
				for(int i = 0; i < NUM_CPUS; i++) {
					if(cpuList.getCPU(i).getCurrentInstanceFinishTime() == Driver.clock) {
						instance = cpuList.pop(i);
						
						if(instance.isEmpty())
						{
//							System.out.println("Instance " + instance.getPid() + " is done");
							finishedList.add(instance);
						}
						else
						{
							if(instance.getNextCommand() == "OTH")
							{
								othQueue.add(new OTH(instance));
							}
						}											
					}
				}
				
			}
			else {
				throw new IllegalArgumentException("-1");
			}
			
//		System.out.println(cpuList.getCPU(0));	
		}
		
		System.out.println("\n########### We are done broski at time " + Driver.clock + " ###########");
		
	}
			
	public static void readInput(List<Instance> instanceTable) throws IOException {
		int pid = 0;
		String packageName = "src/com/jsv/osschedulingresearch/input/";
		
//		StringBuilder filePath = new StringBuilder(packageName + "src/input.txt");
		StringBuilder filePath = new StringBuilder(packageName + "random1.txt");
		BufferedReader reader = new BufferedReader(new FileReader(filePath.toString()));
		
		String[] commandTime = new String[2];
		String line = reader.readLine();
		
		int minCPUTime = 0;
		
		
		while(line != null) {
			commandTime = line.split(" ");
			
			// Make a new Instance
			if(commandTime[0].equals(NEW)) {
//				instanceTable.add(new FCFS(pid, Integer.parseInt(commandTime[1])));
//				instanceTable.add(new ShortestTotalTime(pid, Integer.parseInt(commandTime[1])));
//				instanceTable.add(new ShortestJobTime(pid, Integer.parseInt(commandTime[1])));
//				instanceTable.add(new LowestCPURatio(pid, Integer.parseInt(commandTime[1])));
//				instanceTable.add(new HighestCPURatio(pid, Integer.parseInt(commandTime[1])));
				instanceTable.add(new RoundRobin(pid, Integer.parseInt(commandTime[1])));
				pid++;
			}
			
			// CPU command
			if(commandTime[0].equals(CPU)) {
				// Get the last Instance and add a new command
				instanceTable.get(pid-1).addCommand(new Command(1, Integer.parseInt(commandTime[1])));
				if(Integer.parseInt(commandTime[1])!=0)
				{
					minCPUTime = Math.max(Integer.parseInt(commandTime[1]),minCPUTime);
				}
			}
						
			// Other command
			if(commandTime[0].equals(OTH)) {
				// Get the last Instance and add a new command
				instanceTable.get(pid-1).addCommand(new Command(2, Integer.parseInt(commandTime[1])));
			}
					
			// Read a new line
			line = reader.readLine();	
			
		} //Reads file in
		
		// Close the reader
		reader.close();
		
		Driver.QUANTUM_SIZE = (minCPUTime)/2;
		System.out.println(minCPUTime);
		/*
		for(Instance instance :instanceTable) {
			System.out.println("Pid: " + instance.getPid());
			System.out.println("Start time: " + instance.getStartTime());
			System.out.println("Commands: ");
			
			for(Command command :instance.getCommandList()) {
				System.out.println("\t" + command.getCommandType() + " - " + command.getTimeCommand());				
			}
			
			System.out.println("");
		}*/
	}
	
	public static int checkTimes(Queue<OTH> othQueue, List<Instance> instanceList, CPUList cpuList,int importantTime)
	{
		
		int nextCpuTime;
		int nextOthTime;
		int nextNewTime;
		
		if(othQueue.isEmpty()){
			nextOthTime = Integer.MAX_VALUE;
		}else{
			nextOthTime = othQueue.peek().getExitTime();
		}
		
		nextCpuTime = cpuList.getNextFinishTime();

		if(instanceList.isEmpty())
		{
			nextNewTime = Integer.MAX_VALUE;
		}else{
			nextNewTime = instanceList.get(0).getStartTime();
		}
		
		if(importantTime == nextCpuTime)
		{
			return 0;
		}
		if(importantTime == nextOthTime)
		{
			return 1;
		}
		
		if(importantTime == nextNewTime)
		{
			return 2;
		}
		return -1;
	}
	
	public static int nextImportantEvent(Queue<OTH> othQueue, List<Instance> instanceList, CPUList cpuList)
	{
		int nextImportantTime = Integer.MAX_VALUE;
		
		int nextCpuTime;
		int nextOthTime;
		int nextNewTime;
		
		if(othQueue.isEmpty()){
			nextOthTime = Integer.MAX_VALUE;
		}else{
			nextOthTime = othQueue.peek().getExitTime();
		}
				
		nextCpuTime = cpuList.getNextFinishTime();

		if(instanceList.isEmpty())
		{
			nextNewTime = Integer.MAX_VALUE;
		}else{
			nextNewTime = instanceList.get(0).getStartTime();
		}
		
		nextImportantTime = Math.min(nextOthTime, nextCpuTime);
		nextImportantTime = Math.min(nextImportantTime, nextNewTime);
		
//		System.out.println("\n################## NEXT IMPORTANT TIME: " + nextImportantTime);
		
		return nextImportantTime;
	}
	
}
