	package org.cloudbus.cloudsim.power;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.opt4j.core.genotype.PermutationGenotype;
import org.opt4j.core.problem.Creator;

public class GACreator implements Creator<PermutationGenotype<PowerHost>> {
	private List<PowerHost> elements;
	private Random rnd;
	
	public GACreator(List<PowerHost> elements, Random rnd) {
		this.elements = elements;
		this.rnd = rnd;
	}
	
	@Override
	public PermutationGenotype<PowerHost> create() {
		List<PowerVm> VmList = new LinkedList<PowerVm>();
		//save all vms
		for (PowerHost ph : this.elements) {
			for (PowerVm pv : ph.<PowerVm> getVmList()) {
				VmList.add(pv);
			}
		}
		//shuffle the vms
		Collections.shuffle(VmList, rnd);
		PermutationGenotype<PowerHost> individual = new PermutationGenotype<>();
		//create empty hostlist
		for (PowerHost powerHost : elements) {
			powerHost.vmDestroyAll();
			individual.add(powerHost);
		}
		//randomly allocate VMs
		for (PowerHost powerHost : individual) {
			int max = rnd.nextInt(powerHost.getPeList().size());
			for (int i = 0; i < max; i++) {
				int pickRandom = rnd.nextInt(VmList.size());
				if (powerHost.vmCreate(VmList.get(pickRandom))) {
					VmList.remove(pickRandom);
				}
			}
		}
		return individual;
	}

}
