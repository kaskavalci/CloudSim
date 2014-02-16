package org.cloudbus.cloudsim.power;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.util.ExecutionTimeMeasurer;
import org.opt4j.benchmarks.dtlz.DTLZModule;
import org.opt4j.core.Individual;
import org.opt4j.core.optimizer.Archive;
import org.opt4j.core.start.Opt4JTask;
import org.opt4j.optimizers.ea.EvolutionaryAlgorithmModule;
import org.opt4j.viewer.ViewerModule;

public class PowerVmAllocationPolicyMigrationGA extends
		PowerVmAllocationPolicyMigrationAbstract {

	public PowerVmAllocationPolicyMigrationGA(List<? extends Host> hostList,
			PowerVmSelectionPolicy vmSelectionPolicy) {
		super(hostList, vmSelectionPolicy);
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> optimizeAllocation(
			List<? extends Vm> vmList) {
		ExecutionTimeMeasurer.start("optimizeAllocationTotal");

		List<Map<String, Object>> migrationMap = new LinkedList<Map<String, Object>>();
		// populate migrationMap here
/*
		// initial population
		CandidateFactory<List<PowerHost>> candidateFactory = new GACandidateFactory(
				new LinkedList<PowerHost>(this.<PowerHost> getHostList()));

		Random rng = new MersenneTwister();
		// operators
		List<EvolutionaryOperator<List<PowerHost>>> operators = new ArrayList<EvolutionaryOperator<List<PowerHost>>>(
				2);
		// crossover
		operators.add(new ListOrderCrossover<PowerHost>());
		// mutation
		operators.add(new ListOrderMutation<PowerHost>(new PoissonGenerator(
				1.5, rng), new PoissonGenerator(1.5, rng)));

		// pipeline
		EvolutionaryOperator<List<PowerHost>> pipeline = new EvolutionPipeline<List<PowerHost>>(
				operators);

		// create the engine
		Probability prob = new Probability(0.8);
		EvolutionEngine<List<PowerHost>> engine = new GenerationalEvolutionEngine<List<PowerHost>>(
				candidateFactory, pipeline, new Fitness(),
				new TournamentSelection(prob), rng);
		
		//go!
		engine.evolve(100, // 100 individuals in the population.
				5, // 5% elitism.
				new TargetFitness(0, false));
*/
		
		EvolutionaryAlgorithmModule ea = new EvolutionaryAlgorithmModule();
		ea.setGenerations(500);
		ea.setAlpha(100);
		GAModule GAmod = new GAModule();
		GAmod.setElements(this.<PowerHost>getHostList());
		ViewerModule viewer = new ViewerModule();
		viewer.setCloseOnStop(true);
		Opt4JTask task = new Opt4JTask(false);
		task.init(ea,GAmod);
		List<PowerHost> solution = null;
		try {
		        task.execute();
		        Archive archive = task.getInstance(Archive.class);
		        for (Individual individual : archive) {
		        	solution = (List<PowerHost> ) individual.getPhenotype();
		                
		        }
		} catch (Exception e) {
		        e.printStackTrace();
		} finally {
		        task.close();
		} 
		
		for (PowerHost powerHost : solution) {
			List<PowerVm> VMs = powerHost.getVmList();
			for (PowerVm vm : VMs) {
				Map<String, Object> migrate = new HashMap<String, Object>();
				migrate.put("vm", vm);
				migrate.put("host", powerHost);
				migrationMap.add(migrate);
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

}
