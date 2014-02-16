package org.cloudbus.cloudsim.power;

import org.opt4j.core.problem.ProblemModule;

public class GAModule extends ProblemModule {
	
	@Override
	protected void config() {
		bindProblem(GACreator.class, GADecoder.class, GAEvaluator.class);
	}

}
