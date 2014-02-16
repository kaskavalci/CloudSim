package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.genetics.TournamentSelection;
import org.apache.commons.math3.random.MersenneTwister;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.util.ExecutionTimeMeasurer;

public class PowerVmAllocationPolicyMigrationGA extends
		PowerVmAllocationPolicyMigrationAbstract {

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
		
		GAModule x = new GAModule();
		x.config();
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
