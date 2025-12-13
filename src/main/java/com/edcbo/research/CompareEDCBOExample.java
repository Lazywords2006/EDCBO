package com.edcbo.research;

import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;

import java.util.*;

/**
 * EDCBO性能对比测试程序
 * 在相同环境下对比 CBO、EDCBO、EDCBO-Fixed 三个算法
 */
public class CompareEDCBOExample {

    private static final int NUM_VMS = 20;
    private static final int NUM_CLOUDLETS = 100;
    private static final long SEED = 42L;  // 固定随机种子保证可重复性

    // VM异构参数范围
    private static final int VM_MIPS_MIN = 100;
    private static final int VM_MIPS_MAX = 500;

    // 任务异构参数范围
    private static final long CLOUDLET_LENGTH_MIN = 10000;
    private static final long CLOUDLET_LENGTH_MAX = 50000;

    public static void main(String[] args) {
        System.out.println("======================================");
        System.out.println("   EDCBO性能对比测试程序");
        System.out.println("======================================\n");

        Random random = new Random(SEED);

        // 预生成VM和Cloudlet参数（保证所有算法使用相同的环境）
        int[] vmMips = new int[NUM_VMS];
        long[] cloudletLengths = new long[NUM_CLOUDLETS];

        for (int i = 0; i < NUM_VMS; i++) {
            vmMips[i] = VM_MIPS_MIN + random.nextInt(VM_MIPS_MAX - VM_MIPS_MIN + 1);
        }

        for (int i = 0; i < NUM_CLOUDLETS; i++) {
            cloudletLengths[i] = CLOUDLET_LENGTH_MIN +
                Math.abs(random.nextLong() % (CLOUDLET_LENGTH_MAX - CLOUDLET_LENGTH_MIN + 1));
            if (cloudletLengths[i] < CLOUDLET_LENGTH_MIN) {
                cloudletLengths[i] = CLOUDLET_LENGTH_MIN;
            }
        }

        System.out.println("测试环境:");
        System.out.println("  VM数量: " + NUM_VMS);
        System.out.println("  任务数量: " + NUM_CLOUDLETS);
        System.out.println("  VM MIPS范围: " + VM_MIPS_MIN + "-" + VM_MIPS_MAX);
        System.out.println("  任务长度范围: " + CLOUDLET_LENGTH_MIN + "-" + CLOUDLET_LENGTH_MAX + " MI");
        System.out.println("  随机种子: " + SEED);
        System.out.println();

        // 测试结果存储
        Map<String, TestResult> results = new LinkedHashMap<>();

        // 测试1: CBO
        System.out.println("========================================");
        System.out.println("测试 1/3: CBO算法");
        System.out.println("========================================");
        results.put("CBO", runAlgorithm("CBO", vmMips, cloudletLengths));

        // 测试2: EDCBO（旧版）
        System.out.println("\n========================================");
        System.out.println("测试 2/3: EDCBO算法（旧版）");
        System.out.println("========================================");
        results.put("EDCBO", runAlgorithm("EDCBO", vmMips, cloudletLengths));

        // 测试3: EDCBO-Fixed（新版）
        System.out.println("\n========================================");
        System.out.println("测试 3/3: EDCBO-Fixed算法（优化版）");
        System.out.println("========================================");
        results.put("EDCBO-Fixed", runAlgorithm("EDCBO-Fixed", vmMips, cloudletLengths));

        // 输出对比结果
        printComparisonResults(results);
    }

    private static TestResult runAlgorithm(String algorithmName, int[] vmMips, long[] cloudletLengths) {
        long startTime = System.currentTimeMillis();

        CloudSimPlus simulation = new CloudSimPlus();
        Datacenter datacenter = createDatacenter(simulation);

        // 根据算法名称创建对应的Broker
        Object broker;
        if (algorithmName.equals("CBO")) {
            broker = new CBO_Broker(simulation, SEED);
        } else if (algorithmName.equals("EDCBO")) {
            broker = new EDCBO_Broker(simulation, SEED);
        } else if (algorithmName.equals("EDCBO-Fixed")) {
            broker = new EDCBO_Broker_Fixed(simulation, SEED);
        } else {
            throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
        }

        // 创建VM列表
        List<Vm> vmList = new ArrayList<>();
        for (int i = 0; i < NUM_VMS; i++) {
            Vm vm = new VmSimple(vmMips[i], 1)
                .setRam(2048).setBw(1000).setSize(10000);
            vmList.add(vm);
        }

        // 创建Cloudlet列表
        List<Cloudlet> cloudletList = new ArrayList<>();
        for (int i = 0; i < NUM_CLOUDLETS; i++) {
            Cloudlet cloudlet = new CloudletSimple(cloudletLengths[i], 1)
                .setFileSize(300).setOutputSize(300)
                .setUtilizationModelCpu(new UtilizationModelFull());
            cloudletList.add(cloudlet);
        }

        // 提交任务和VM
        if (broker instanceof CBO_Broker) {
            ((CBO_Broker) broker).submitVmList(vmList);
            ((CBO_Broker) broker).submitCloudletList(cloudletList);
        } else if (broker instanceof EDCBO_Broker) {
            ((EDCBO_Broker) broker).submitVmList(vmList);
            ((EDCBO_Broker) broker).submitCloudletList(cloudletList);
        } else if (broker instanceof EDCBO_Broker_Fixed) {
            ((EDCBO_Broker_Fixed) broker).submitVmList(vmList);
            ((EDCBO_Broker_Fixed) broker).submitCloudletList(cloudletList);
        }

        simulation.start();

        long endTime = System.currentTimeMillis();
        long runTime = endTime - startTime;

        // 计算Makespan（所有任务完成的最大时间）
        double makespan = 0.0;
        for (Cloudlet cloudlet : cloudletList) {
            double finishTime = cloudlet.getFinishTime();
            if (finishTime > makespan) {
                makespan = finishTime;
            }
        }

        // 获取收敛曲线
        List<Double> convergenceCurve = new ArrayList<>();
        if (broker instanceof CBO_Broker) {
            convergenceCurve = ((CBO_Broker) broker).getConvergenceRecord().getIterationBestFitness();
        } else if (broker instanceof EDCBO_Broker) {
            convergenceCurve = ((EDCBO_Broker) broker).getConvergenceRecord().getIterationBestFitness();
        } else if (broker instanceof EDCBO_Broker_Fixed) {
            convergenceCurve = ((EDCBO_Broker_Fixed) broker).getConvergenceRecord().getIterationBestFitness();
        }

        System.out.println("\n结果:");
        System.out.println("  Makespan: " + String.format("%.2f", makespan) + " 秒");
        System.out.println("  运行时间: " + runTime + " ms");
        System.out.println("  收敛曲线长度: " + convergenceCurve.size());

        return new TestResult(makespan, runTime, convergenceCurve);
    }

    private static Datacenter createDatacenter(CloudSimPlus simulation) {
        List<Host> hostList = new ArrayList<>();

        for (int i = 0; i < 40; i++) {
            List<Pe> peList = new ArrayList<>();
            peList.add(new PeSimple(2000));

            Host host = new HostSimple(16384, 100000, 100000, peList);
            hostList.add(host);
        }

        return new DatacenterSimple(simulation, hostList);
    }

    private static void printComparisonResults(Map<String, TestResult> results) {
        System.out.println("\n\n");
        System.out.println("========================================");
        System.out.println("   性能对比结果汇总");
        System.out.println("========================================\n");

        // 找到最优Makespan
        double bestMakespan = Double.MAX_VALUE;
        String bestAlgorithm = "";
        for (Map.Entry<String, TestResult> entry : results.entrySet()) {
            if (entry.getValue().makespan < bestMakespan) {
                bestMakespan = entry.getValue().makespan;
                bestAlgorithm = entry.getKey();
            }
        }

        // 表格输出
        System.out.println("算法性能对比表:");
        System.out.println("┌──────────────────┬──────────────┬──────────────┬──────────────┐");
        System.out.println("│ 算法             │ Makespan(秒) │ 运行时间(ms) │ vs 最优(%)   │");
        System.out.println("├──────────────────┼──────────────┼──────────────┼──────────────┤");

        for (Map.Entry<String, TestResult> entry : results.entrySet()) {
            String name = entry.getKey();
            TestResult result = entry.getValue();
            double improvement = ((result.makespan - bestMakespan) / bestMakespan) * 100;
            String marker = name.equals(bestAlgorithm) ? " 🏆" : "";

            System.out.printf("│ %-16s │ %12.2f │ %12d │ %11.2f%% │%s%n",
                name, result.makespan, result.runTime, improvement, marker);
        }
        System.out.println("└──────────────────┴──────────────┴──────────────┴──────────────┘");

        System.out.println("\n收敛速度对比（前10次迭代）:");
        System.out.println("┌─────┬──────────┬──────────┬──────────────┐");
        System.out.println("│ 迭代│   CBO    │  EDCBO   │ EDCBO-Fixed  │");
        System.out.println("├─────┼──────────┼──────────┼──────────────┤");

        for (int i = 0; i <= 10; i++) {
            System.out.printf("│ %3d │", i);
            for (String algorithm : Arrays.asList("CBO", "EDCBO", "EDCBO-Fixed")) {
                TestResult result = results.get(algorithm);
                if (result != null && result.convergenceCurve.size() > i) {
                    System.out.printf(" %8.2f │", result.convergenceCurve.get(i));
                } else {
                    System.out.print("    N/A  │");
                }
            }
            System.out.println();
        }
        System.out.println("└─────┴──────────┴──────────┴──────────────┘");

        // 改进率分析
        System.out.println("\n改进率分析（相对于CBO）:");
        double cboMakespan = results.get("CBO").makespan;
        for (Map.Entry<String, TestResult> entry : results.entrySet()) {
            if (!entry.getKey().equals("CBO")) {
                double improvement = ((cboMakespan - entry.getValue().makespan) / cboMakespan) * 100;
                String direction = improvement > 0 ? "改进" : "退化";
                System.out.printf("  %s: %.2f%% (%s)%n", entry.getKey(), Math.abs(improvement), direction);
            }
        }

        // 结论
        System.out.println("\n========================================");
        System.out.println("结论:");
        System.out.println("========================================");
        System.out.println("✅ 最优算法: " + bestAlgorithm);
        System.out.println("✅ 最优Makespan: " + String.format("%.2f", bestMakespan) + " 秒");

        double edcboImprovement = ((cboMakespan - results.get("EDCBO").makespan) / cboMakespan) * 100;
        if (edcboImprovement > 0) {
            System.out.println("✅ EDCBO相对CBO改进: " + String.format("%.2f%%", edcboImprovement));
        } else {
            System.out.println("⚠️ EDCBO相对CBO退化: " + String.format("%.2f%%", Math.abs(edcboImprovement)));
        }

        System.out.println("========================================\n");
    }

    static class TestResult {
        double makespan;
        long runTime;
        List<Double> convergenceCurve;

        TestResult(double makespan, long runTime, List<Double> convergenceCurve) {
            this.makespan = makespan;
            this.runTime = runTime;
            this.convergenceCurve = convergenceCurve;
        }
    }
}
