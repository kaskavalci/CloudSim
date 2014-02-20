package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.CloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class GAInd {

	List<PowerHost> hosts, originalHosts;
	List<PowerVm> vmList, originalVMs;
	Random rnd;
	PowerVmAllocationPolicyMigrationGA GA;
	// int[] genotype;
	SortedMap<Integer, PowerHost> gen;
	int geneSize;
	double[] fitness;
	public boolean isPareto;

	public GAInd(PowerVmAllocationPolicyMigrationGA ga) throws Exception {
		initialize(ga);

		for (int i = 0; i < vmList.size(); i++) {
			PowerHost foundHost;
			PowerVm vm = vmList.get(i);

			do {
				foundHost = findRandomHost(vm);
			} while (!assignVMtoHost(vm, foundHost));
		}
		getFitness();
	}

	// crossover
	public GAInd(GAInd p1, GAInd p2) throws Exception {
		initialize(p1.GA);

		int crossoverPoint = rnd.nextInt(GA.getVmTable().size());
		/*
		 * for (int i = 0; i < geneSize; i++) { if (i < crossoverPoint) {
		 * genotype[i] = p1.genotype[i]; } else { genotype[i] = p2.genotype[i];
		 * } }
		 */
		// copy everything
		SortedMap<Integer, PowerHost> tempGen = new TreeMap<>();
		int count = 0;
		for (Iterator<Entry<Integer, PowerHost>> iterator = p1.gen.entrySet()
				.iterator(); iterator.hasNext() && count < crossoverPoint; count++) {
			Entry<Integer, PowerHost> entry = iterator.next();
			PowerHost host = findHostByID(entry.getValue().getId(), hosts);
			tempGen.put(entry.getKey(), host);
		}

		count = 0;
		for (Iterator<Entry<Integer, PowerHost>> iterator = p2.gen.entrySet()
				.iterator(); iterator.hasNext(); count++) {
			if (count < crossoverPoint)
				continue;
			Entry<Integer, PowerHost> entry = iterator.next();
			PowerHost host = findHostByID(entry.getValue().getId(), hosts);
			tempGen.put(entry.getKey(), host);
		}

		// handle invalid assignments
		List<PowerVm> unassignedVMs = new ArrayList<PowerVm>();
		for (Iterator<Entry<Integer, PowerHost>> iterator = tempGen.entrySet()
				.iterator(); iterator.hasNext();) {
			Entry<Integer, PowerHost> entry = iterator.next();
			int vmID = entry.getKey();
			PowerVm theVm = findVmByID(vmID, vmList);
			PowerHost candidateHost = entry.getValue();
			if (candidateHost.isSuitableForVm(theVm)) {
				assignVMtoHost(theVm, candidateHost);
			} else if (theVm.getHost() == null
					|| (theVm.getHost() != null && !theVm.getHost().getVmList()
							.contains(theVm))) {
				unassignedVMs.add(theVm);
			}
		}
		Collections.shuffle(hosts, rnd);
		for (PowerVm powerVm : unassignedVMs) {
			boolean found = false;
			for (PowerHost ph : hosts) {
				if (ph.isSuitableForVm(powerVm)) {
					assignVMtoHost(powerVm, ph);
					found = true;
					break;
				}
			}
			if (!found) {
				throw new Exception("Cannot find a sutiable host!");
			}
		}
		getFitness();
	}

	private void initialize(PowerVmAllocationPolicyMigrationGA ga) {
		this.GA = ga;
		originalHosts = ga.<PowerHost> getHostList();
		this.hosts = new ArrayList<PowerHost>();

		originalVMs = new ArrayList<PowerVm>();
		vmList = new ArrayList<PowerVm>();
		gen = new TreeMap<>();

		for (PowerHost ph : originalHosts) {
			for (PowerVm pv : ph.<PowerVm> getVmList()) {
				vmList.add(copyVM(pv));
				originalVMs.add(pv);

				gen.put(pv.getId(), null);
			}
		}

		geneSize = vmList.size();
		fitness = new double[] { 0., 0., 0., 0. };
		// genotype = new int[geneSize];

		// copy all hosts
		copyHosts(originalHosts, hosts);

		isPareto = false;
		rnd = PowerVmAllocationPolicyMigrationGA.rnd;
	}

	public double[] getFitness() {
		for (int i = 0; i < fitness.length; i++) {
			fitness[i] = 0;
		}
		double utilRam, utilBw;

		for (PowerHost host : hosts) {
			utilBw = host.getUtilizationOfBw() / (double) host.getBw();
			utilRam = host.getUtilizationOfRam() / (double) host.getRam();
			if (host.getUtilizationOfCpu() == 0)
				fitness[0]++;
			if (utilBw > 0.5 && utilBw < 0.9)
				fitness[1]++;
			if (utilRam > 0.5 && utilRam < 0.9)
				fitness[2]++;
		}
		for (PowerVm vm : vmList) {
			Host oldHost = findVmByID(vm.getId(), originalVMs).getHost();
			if (oldHost.getId() == vm.getHost().getId())
				fitness[3]++;
		}

		return fitness;
	}
	
	public Domination dominates(GAInd target) {
		if (fitness[0] > target.fitness[0] && fitness[1] > target.fitness[1] &&
				fitness[2] > target.fitness[2] && fitness[3] > target.fitness[3]) {
			return Domination.True;
		}
		if (fitness[0] <= target.fitness[0] && fitness[1] <= target.fitness[1] &&
				fitness[2] <= target.fitness[2] && fitness[3] <= target.fitness[3]) {
			return Domination.False;
		}
		return Domination.NoDomination;
	}

	/**
	 * 
	 * @param powerHost
	 * @return
	 */
	private PowerHost copyHost(PowerHost powerHost) {
		VmScheduler sch = powerHost.getVmScheduler(), newSch;
		if (sch instanceof VmSchedulerTimeShared) {
			newSch = new VmSchedulerTimeShared(powerHost.getPeList());
		} else if (sch instanceof VmSchedulerTimeSharedOverSubscription) {
			newSch = new VmSchedulerTimeSharedOverSubscription(
					powerHost.getPeList());
		} else {
			newSch = new VmSchedulerSpaceShared(powerHost.getPeList());
		}

		return new PowerHost(powerHost.getId(), new RamProvisionerSimple(
				powerHost.getRam()),
				new BwProvisionerSimple(powerHost.getBw()),
				powerHost.getStorage(), powerHost.getPeList(), newSch,
				powerHost.getPowerModel());
	}

	private void copyHosts(List<PowerHost> src, List<PowerHost> target) {
		for (PowerHost powerHost : src) {
			target.add(copyHost(powerHost));
		}
	}

	/**
	 * Creates a new PowerVm based on the argument. This is done because Vm does not 
	 * implement Cloneable interface.
	 * @param pv
	 * @return
	 */
	private PowerVm copyVM(PowerVm pv) {
		CloudletScheduler sch = pv.getCloudletScheduler(), newSch;
		if (sch instanceof CloudletSchedulerDynamicWorkload) {
			newSch = new CloudletSchedulerDynamicWorkload(
					((CloudletSchedulerDynamicWorkload) sch).getMips(),
					((CloudletSchedulerDynamicWorkload) sch).getNumberOfPes());
		} else if (sch instanceof CloudletSchedulerSpaceShared) {
			newSch = new CloudletSchedulerSpaceShared();
		} else {
			newSch = new CloudletSchedulerTimeShared();
		}
		PowerVm vm = new PowerVm(pv.getId(), pv.getUserId(), pv.getMips(),
				pv.getNumberOfPes(), pv.getRam(), pv.getBw(), pv.getSize(), 0,
				pv.getVmm(), newSch, pv.getSchedulingInterval());
		return vm;
	}

	private void copyVMs(List<PowerHost> src, List<PowerVm>... target) {
		for (PowerHost ph : src) {
			for (PowerVm pv : ph.<PowerVm> getVmList()) {
				target[0].add(copyVM(pv));
				if (target.length > 1) {
					target[1].add(pv);
				}
			}
		}
	}

	private boolean assignVMtoHost(PowerVm vm, PowerHost host) {
		boolean result;
		Host prevHost = vm.getHost();
		if (prevHost != null) {
			prevHost.vmDestroy(vm);
		}
		Log.setDisabled(true);
		if (host.vmCreate(vm)) {
			gen.put(vm.getId(), host);

			host.updateVmsProcessing(0);
			// genotype[vmID] = host.getId();
			result = true;
		} else {
			if (prevHost != null) {
				prevHost.vmCreate(vm);
				host.updateVmsProcessing(0);
			}
			result = false;
		}
		//Log.setDisabled(false);
		return result;
	}

	private PowerHost findHostByID(int id, List<PowerHost> list) {
		for (PowerHost iter : list) {
			if (iter.getId() == id)
				return iter;
		}
		return null;
	}

	private PowerVm findVmByID(int id, List<PowerVm> list) {
		for (PowerVm iter : list) {
			if (iter.getId() == id)
				return iter;
		}
		return null;
	}

	public List<Map<String, Object>> getMap() throws Exception {
		List<Map<String, Object>> migrationMap = new LinkedList<Map<String, Object>>();

		for (PowerVm pv : vmList) {
			Map<String, Object> migrate = new HashMap<String, Object>();

			PowerVm foundVM = findVmByID(pv.getId(), originalVMs);
			PowerHost foundHost = findHostByID(pv.getHost().getId(),
					originalHosts);

			if (foundHost == null || foundVM == null) {
				throw new Exception();
			}
			migrate.put("vm", foundVM);
			migrate.put("host", foundHost);
			migrationMap.add(migrate);
		}

		return migrationMap;
	}

	PowerHost findRandomHost(PowerVm vm) throws Exception {
		Collections.shuffle(hosts, rnd);
		for (PowerHost host : hosts) {
			if (host.isSuitableForVm(vm)) {
				return host;
			}
		}

		// GA.isHostOverUtilizedAfterAllocation(hosts.get(foundHost), vm)
		throw new Exception("Cannot find a host!");
	}

	public void Mutation() throws Exception {
		int vmID = rnd.nextInt(vmList.size());
		PowerVm rndVm = vmList.get(vmID);
		PowerHost foundHost = findRandomHost(rndVm);

		assignVMtoHost(rndVm, foundHost);
		getFitness();
	}

}
