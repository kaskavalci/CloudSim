package org.cloudbus.cloudsim.power;

import java.util.List;

import org.opt4j.core.Objective.Sign;
import org.opt4j.core.Objectives;
import org.opt4j.core.problem.Evaluator;

public class GAEvaluator implements Evaluator<List<PowerHost>> {

	@Override
	public Objectives evaluate(List<PowerHost> phenotype) {
		int runningHosts = 0, greenHostsCPU = 0, greenHostsRAM = 0, greenHostsBW = 0;
		for (PowerHost ph : phenotype) {
			if (ph.getUtilizationOfCpu() == 0) runningHosts++;
			if (ph.getUtilizationOfCpu() < 0.9 && ph.getUtilizationOfCpu() > 0.25) greenHostsCPU++;
			if (ph.getUtilizationOfRam() < 0.9 && ph.getUtilizationOfRam() > 0.25) greenHostsRAM++;
			if (ph.getUtilizationOfBw() < 0.9 && ph.getUtilizationOfBw() > 0.25) greenHostsBW++;
		}
		Objectives objectives = new Objectives();
		objectives.add("RunningHosts", Sign.MAX, runningHosts);
		objectives.add("GreenHostsCPU", Sign.MAX, greenHostsCPU);
		objectives.add("GreenHostsRAM", Sign.MAX, greenHostsRAM);
		objectives.add("GreenHostsBW", Sign.MAX, greenHostsBW);
		return objectives;
	}

}
