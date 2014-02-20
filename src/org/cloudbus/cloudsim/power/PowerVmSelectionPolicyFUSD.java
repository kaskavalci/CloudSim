/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;

/**
 * The Minimum Migration Time (MMT) VM selection policy.
 * 
 * If you are using any algorithms, policies or workload included in the power package, please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 3.0
 */
public class PowerVmSelectionPolicyFUSD extends PowerVmSelectionPolicy {

	/*
	 * (non-Javadoc)
	 * @see
	 * org.cloudbus.cloudsim.experiments.power.PowerVmSelectionPolicy#getVmsToMigrate(org.cloudbus
	 * .cloudsim.power.PowerHost)
	 */
	@Override
	public Vm getVmToMigrate(PowerHost host) {
		Log.printLine("\n ********************* Fast Up Slow Down Algortihm - Vm Selection Policy **********************\n ");
		List<PowerVm> migratableVms = getMigratableVms(host);
		if (migratableVms.isEmpty()) {
			return null;
		}
		double currentTemperature = host.hotTemperatureOfServer();
		double currentSkewness = host.skewnessOfServer();
		double tempWithoutVM;
		double skewWithoutVM;
		double tempDif;
		double skewDif;
		double difference=0;
		Vm vmToMigrate = null;
		Vm tempVM =null ;
		for (Vm vm : migratableVms) {
			if (vm.isInMigration()) {
				continue;
			}
			tempWithoutVM = calculateNewTemp(host,vm);
			skewWithoutVM= calculateNewSkew(host,vm);
			tempDif = currentTemperature - tempWithoutVM;
			skewDif = currentSkewness - skewWithoutVM;
			if (currentTemperature ==0 && currentSkewness ==0){
				tempVM=vm;
			}
			if ((tempDif + skewDif)> difference){
				difference = tempDif + skewDif;
				tempVM = vm;
			}
			}
		vmToMigrate = tempVM;
		return vmToMigrate;
	}

	public double calculateNewSkew(PowerHost host, Vm vm){
		
		// current resource utilization, only bottleneck resources are considered
		double currentRAMutilization = (host.getRamProvisioner().getUsedRam() - vm.getRam())/(host.getRamProvisioner().getRam());
		double currentBWutilization = (host.getBwProvisioner().getUsedBw() - vm.getBw())/(host.getBwProvisioner().getBw());
		double currentMIPSutilization = 1- ( (host.getAvailableMips() - vm.getMips())/host.getTotalMips());
		
		// average resourse utilization is needed for the skewness formula
		double averageUtilization = (currentRAMutilization + currentBWutilization + currentMIPSutilization)/3;
		
		// applying skewness formula
		double ram = Math.pow(((currentRAMutilization/averageUtilization)-1), 2);
		double bw = Math.pow(((currentBWutilization/averageUtilization)-1), 2);
		double cpu = Math.pow(((currentMIPSutilization/averageUtilization)-1), 2);
		double skewness = Math.sqrt(ram + bw + cpu);
		
		return skewness;
	}
	
	public double calculateNewTemp(PowerHost host, Vm vm){
		
		// current resource utilization, only bottleneck resources are considered
		double currentRAMutilization = (host.getRamProvisioner().getUsedRam() - vm.getRam())/(host.getRamProvisioner().getRam());
		double currentBWutilization = (host.getBwProvisioner().getUsedBw() - vm.getBw())/(host.getBwProvisioner().getBw());
		double currentMIPSutilization = 1- ( (host.getAvailableMips() - vm.getMips())/host.getTotalMips());
		
		// definitions for threshold values for resources (as stated in corresponding paper)
		double hotThreshold = 0.90;
		//double coldThreshold = 0.25;
		//double warmThreshold = 0.65;
		//double greenComputingThreshold = 0.40;
		//double consolidationLimit = 0.05;
		
		double temperatureHot=0;
		
		//applying temperature formula for hotThreshold
		if (currentRAMutilization >= hotThreshold){
			temperatureHot = temperatureHot + Math.pow((currentRAMutilization-hotThreshold), 2);
		}
		if (currentBWutilization >= hotThreshold){
			temperatureHot = temperatureHot + Math.pow((currentBWutilization-hotThreshold), 2);
		}
		if (currentMIPSutilization >= hotThreshold){
			temperatureHot = temperatureHot + Math.pow((currentMIPSutilization-hotThreshold), 2);
		}
		
		return temperatureHot;
	}
}
