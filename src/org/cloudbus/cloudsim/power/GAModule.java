package org.cloudbus.cloudsim.power;

import java.util.List;

import org.opt4j.core.problem.ProblemModule;
import org.opt4j.core.start.Constant;

import com.google.inject.Provides;

public class GAModule extends ProblemModule {
	//@Constant(value = "elements")
	protected List<PowerHost> elements;

	@Provides List<PowerHost> getElements() {
		return elements;
	}

	public void setElements(List<PowerHost> elements) {
		this.elements = elements;
	}

	protected void config() {
		bindProblem(GACreator.class, GADecoder.class, GAEvaluator.class);
	}

}
