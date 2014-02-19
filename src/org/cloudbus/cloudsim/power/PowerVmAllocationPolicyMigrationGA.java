package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.util.ExecutionTimeMeasurer;

public class PowerVmAllocationPolicyMigrationGA extends
		PowerVmAllocationPolicyMigrationAbstract {

	public static Random rnd;

	List<GAInd> pop, pareto;

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

		initGA();
		while(true) {
			try {
				migrationMap = pareto.get(rnd.nextInt(pareto.size())).getMap();
				break;
			} catch (Exception e) {
			}
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
	
	private void initGA() {
		rnd = new Random();
		pop = new ArrayList<GAInd>();
		pareto = new ArrayList<>();

		for (int i = 0; i < 50; i++) {
			try {
				addToPopulation(new GAInd(this));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println(e.getMessage());
			}
		}
		
		for (int i = 0; i < 500; i++) {
			mutation();
			crossover();
		}
	}

	private void crossover() {
		GAInd p1, p2;
		p1 = pareto.get(rnd.nextInt(pareto.size()));
		do {
			p2 = pareto.get(rnd.nextInt(pareto.size()));
		} while (p1.equals(p2));
		
		try {
			addToPopulation(new GAInd(p1, p2));
		} catch (Exception e) {
			
		}
	}

	private void mutation() {
		GAInd randInd;
		do {
			randInd = pop.get(rnd.nextInt(pop.size()));
		} while (!randInd.isPareto);
		
		try {
			randInd.Mutation();
		} catch (Exception e) {
			
		}
	}

	private void removeIndividual(GAInd ind) {
		pop.remove(ind);
		pareto.remove(ind);
	}

	private boolean addToPareto(GAInd ind) {
		List<GAInd> dominatedInds = new ArrayList<>();
		for (GAInd target : pareto) {
			if (ind.dominates(target) == Domination.True) {
				dominatedInds.add(target);
			} else if (ind.dominates(target) == Domination.False) {
				return false;
			}
		}

		for (GAInd gaInd : dominatedInds) {
			removeIndividual(gaInd);
		}
		if (pareto.size() < 20) {
			pareto.add(ind);
			ind.isPareto = true;
			return true;
		}
		return false;
	}

	private boolean addToPopulation(GAInd ind) {
		if (pop.size() < 50) {
			pop.add(ind);
			addToPareto(ind);
			return true;
		}
		List<GAInd> dominatedInds = new ArrayList<>();
		for (GAInd gaInd : pop) {
			if (ind.dominates(gaInd) == Domination.True) {
				dominatedInds.add(gaInd);
			}
		}

		if (dominatedInds.size() > 0) {
			GAInd random = dominatedInds.get(rnd.nextInt(dominatedInds.size()));
			removeIndividual(random);
			pop.add(ind);
			addToPareto(ind);
			return true;
		}
		return false;
	}

}
