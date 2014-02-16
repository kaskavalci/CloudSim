package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.List;

import org.opt4j.core.genotype.PermutationGenotype;
import org.opt4j.core.problem.Decoder;

public class GADecoder implements Decoder<PermutationGenotype<PowerHost>, List<PowerHost>> {

	public GADecoder() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<PowerHost> decode(PermutationGenotype<PowerHost> genotype) {
		List<PowerHost> list = new ArrayList<>(genotype);
		return list;
	}

}
