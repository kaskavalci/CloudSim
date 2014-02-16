	package org.cloudbus.cloudsim.power;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.opt4j.core.genotype.PermutationGenotype;
import org.opt4j.core.problem.Creator;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class GACreator implements Creator<PermutationGenotype<PowerHost>> {
	Provider<List<PowerHost>> hosts;
	private Random rnd;
	
	@Inject
	public GACreator( Provider<List<PowerHost>> hosts) {
		this.hosts = hosts;
		this.rnd = new Random();
	}
	
	@Override
	public PermutationGenotype<PowerHost> create() {
		PermutationGenotype<PowerHost> individual = new PermutationGenotype<>();
		List<PowerHost> elements = hosts.get();
		List<PowerVm> VmList = new LinkedList<PowerVm>();
		//save all vms
		for (PowerHost ph : elements) {
			for (PowerVm pv : ph.<PowerVm> getVmList()) {
				VmList.add(pv);
			}
		}
		//shuffle the vms
		Collections.shuffle(VmList, rnd);
		//create empty hostlist
		for (PowerHost powerHost : elements) {
			powerHost.vmDestroyAll();
			individual.add(powerHost);
		}
		//randomly allocate VMs
		for (PowerHost powerHost : individual) {
			int max = rnd.nextInt(powerHost.getPeList().size());
			for (int i = 0; i < max && VmList.size() > 0; i++) {
				int pickRandom = rnd.nextInt(VmList.size());
				if (powerHost.vmCreate(VmList.get(pickRandom))) {
					VmList.remove(pickRandom);
				}
			}
		}
		return individual;
	}

}
