package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.util.ExecutionTimeMeasurer;

public class PowerVmAllocationPolicyMigrationGA extends
		PowerVmAllocationPolicyMigrationAbstract {

	static Random rnd;

	List<GAInd> pop;

	public PowerVmAllocationPolicyMigrationGA(List<? extends Host> hostList,
			PowerVmSelectionPolicy vmSelectionPolicy) {
		super(hostList, vmSelectionPolicy);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(
			List<? extends Vm> vmList) {
		ExecutionTimeMeasurer.start("optimizeAllocationTotal");

		List<Map<String, Object>> migrationMap = new LinkedList<Map<String, Object>>();
		// populate migrationMap here

		rnd = new Random();
		pop = new ArrayList<GAInd>();

		for (int i = 0; i < 50; i++) {
			pop.add(new GAInd(this));
		}

		try {
			migrationMap = pop.get(0).getMap();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getExecutionTimeHistoryTotal().add(
				ExecutionTimeMeasurer.end("optimizeAllocationTotal"));
		return migrationMap;
	}

	@Override
	protected boolean isHostOverUtilized(PowerHost host) {
		// TODO Auto-generated method stub
		return false;
	}

}
