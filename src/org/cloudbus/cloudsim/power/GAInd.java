package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cloudbus.cloudsim.Host;

public class GAInd {

	List<PowerHost> hosts, originalHosts;
	List<PowerVm> vmList, originalVMs;
	Random rnd;
	PowerVmAllocationPolicyMigrationGA GA;
	int[] genotype;

	public GAInd(PowerVmAllocationPolicyMigrationGA ga) {
		this.GA = ga;
		originalHosts = ga.<PowerHost> getHostList();
		this.hosts = new ArrayList<PowerHost>();

		originalVMs = new ArrayList<PowerVm>();
		vmList = new ArrayList<PowerVm>();
		for (PowerHost ph : originalHosts) {
			for (PowerVm pv : ph.<PowerVm> getVmList()) {
				PowerVm yeni = new PowerVm(pv.getId(), pv.getUserId(),
						pv.getMips(), pv.getNumberOfPes(), pv.getRam(),
						pv.getBw(), pv.getSize(), 0, pv.getVmm(),
						pv.getCloudletScheduler(), pv.getSchedulingInterval());
				vmList.add(yeni);

				originalVMs.add(pv);
			}
		}

		genotype = new int[vmList.size()];

		// deallocate vms from hosts
		for (PowerHost powerHost : originalHosts) {
			PowerHost yeni = new PowerHost(powerHost.getId(),
					powerHost.getRamProvisioner(),
					powerHost.getBwProvisioner(), powerHost.getStorage(),
					powerHost.getPeList(), powerHost.getVmScheduler(),
					powerHost.getPowerModel());
			yeni.vmDestroyAll();
			hosts.add(yeni);
		}

		rnd = PowerVmAllocationPolicyMigrationGA.rnd;

		// initialize
		for (int i = 0; i < vmList.size(); i++) {
			PowerHost foundHost;
			PowerVm vm = vmList.get(i);
			
			do {
				foundHost = findRandomHost(vm);
			} while (!assignVMtoHost(vm, foundHost));
		}
	}
	
	private boolean assignVMtoHost(PowerVm vm, PowerHost host) {
		Host prevHost = vm.getHost();
		if (prevHost != null) {
			prevHost.vmDestroy(vm);
		}
		
		if (host.vmCreate(vm)) {
			genotype[vm.getId()] = host.getId();
			return true;
		} else {
			if (prevHost != null) {
				prevHost.vmCreate(vm);
			}
			return false;
		}
	}
	
	//crossover
	public GAInd(GAInd p1, GAInd p2) {
		int crossoverPoint = rnd.nextInt(p1.vmList.size());
		
	}

	public List<Map<String, Object>> getMap() throws Exception {
		List<Map<String, Object>> migrationMap = new LinkedList<Map<String, Object>>();

		for (PowerVm pv : vmList) {
			Map<String, Object> migrate = new HashMap<String, Object>();
			PowerVm foundVM = null;
			PowerHost foundHost = null;
			for (PowerVm iter : originalVMs) {
				if (iter.getId() == pv.getId()) foundVM = iter;
			}
			for (PowerHost iter : originalHosts) {
				if (iter.getId() == pv.getHost().getId()) foundHost = iter;
			}
			if (foundHost == null || foundVM == null) {
				throw new Exception();
			}
			migrate.put("vm", foundVM);
			migrate.put("host", foundHost);
			migrationMap.add(migrate);
		}

		return migrationMap;
	}
	
	PowerHost findRandomHost(PowerVm vm) {
		PowerHost foundHost;
		do {
			foundHost = hosts.get(rnd.nextInt(hosts.size()));
		} while (!foundHost.isSuitableForVm(vm));
		//GA.isHostOverUtilizedAfterAllocation(hosts.get(foundHost), vm)
		return foundHost;
		
	}
	
	public void Mutation() {
		PowerVm rndVm = vmList.get(rnd.nextInt(vmList.size()));
		PowerHost foundHost = findRandomHost(rndVm);
		assignVMtoHost(rndVm, foundHost);
	}

}
