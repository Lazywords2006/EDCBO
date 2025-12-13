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
 * 完整对比测试：CBO vs EDCBO vs EDCBO-Fixed
 * 在两个数据集上测试：固定参数 + 异构参数
 */
public class CompleteComparisonTest {

    private static final int NUM_VMS = 20;
    private static final int NUM_CLOUDLETS = 100;
    private static final long SEED = 42L;
    private static final int NUM_RUNS = 10;

    // 固定参数配置
    private static final int FIXED_VM_MIPS_MIN = 500;
    private static final int FIXED_VM_MIPS_STEP = 50;
    private static final long FIXED_CLOUDLET_LENGTH = 10000;

    // 异构参数配置
    private static final int HETERO_VM_MIPS_MIN = 100;
    private static final int HETERO_VM_MIPS_MAX = 500;
    private static final long HETERO_CLOUDLET_LENGTH_MIN = 10000;
    private static final long HETERO_CLOUDLET_LENGTH_MAX = 50000;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║   EDCBO完整对比测试：两个数据集                                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println("\n测试配置：");
        System.out.println("  - 算法数量: 3 (CBO, EDCBO, EDCBO-Fixed)");
        System.out.println("  - 数据集: 2 (固定参数 + 异构参数)");
        System.out.println("  - 每个配置运行次数: " + NUM_RUNS);
        System.out.println("  - VM数量: " + NUM_VMS);
        System.out.println("  - 任务数量: " + NUM_CLOUDLETS);
        System.out.println("  - 随机种子: " + SEED);
        System.out.println();

        // 数据集1：固定参数
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║   数据集1：固定参数测试                                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println("配置：");
        System.out.println("  - VM MIPS: 递增 (" + FIXED_VM_MIPS_MIN + "+" + FIXED_VM_MIPS_STEP + "*i)");
        System.out.println("  - 任务长度: 固定 " + FIXED_CLOUDLET_LENGTH + " MI");
        System.out.println();

        Map<String, DatasetResult> fixedResults = testDataset1_Fixed();

        // 数据集2：异构参数
        System.out.println("\n\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║   数据集2：异构参数测试                                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println("配置：");
        System.out.println("  - VM MIPS: 随机 [" + HETERO_VM_MIPS_MIN + ", " + HETERO_VM_MIPS_MAX + "]");
        System.out.println("  - 任务长度: 随机 [" + HETERO_CLOUDLET_LENGTH_MIN + ", " + HETERO_CLOUDLET_LENGTH_MAX + "] MI");
        System.out.println();

        Map<String, DatasetResult> heteroResults = testDataset2_Heterogeneous();

        // 综合对比
        printFinalComparison(fixedResults, heteroResults);
    }

    /**
     * 数据集1：固定参数测试
     */
    private static Map<String, DatasetResult> testDataset1_Fixed() {
        Map<String, DatasetResult> results = new LinkedHashMap<>();

        // 生成固定参数
        int[] vmMips = new int[NUM_VMS];
        long[] cloudletLengths = new long[NUM_CLOUDLETS];

        for (int i = 0; i < NUM_VMS; i++) {
            vmMips[i] = FIXED_VM_MIPS_MIN + i * FIXED_VM_MIPS_STEP;
        }
        Arrays.fill(cloudletLengths, FIXED_CLOUDLET_LENGTH);

        // 测试三个算法
        results.put("CBO", runMultipleTests("CBO", vmMips, cloudletLengths, "固定参数"));
        results.put("EDCBO", runMultipleTests("EDCBO", vmMips, cloudletLengths, "固定参数"));
        results.put("EDCBO-Fixed", runMultipleTests("EDCBO-Fixed", vmMips, cloudletLengths, "固定参数"));

        printDatasetSummary("固定参数", results);
        return results;
    }

    /**
     * 数据集2：异构参数测试
     */
    private static Map<String, DatasetResult> testDataset2_Heterogeneous() {
        Map<String, DatasetResult> results = new LinkedHashMap<>();

        Random random = new Random(SEED);

        // 生成异构参数
        int[] vmMips = new int[NUM_VMS];
        long[] cloudletLengths = new long[NUM_CLOUDLETS];

        for (int i = 0; i < NUM_VMS; i++) {
            vmMips[i] = HETERO_VM_MIPS_MIN + random.nextInt(HETERO_VM_MIPS_MAX - HETERO_VM_MIPS_MIN + 1);
        }

        for (int i = 0; i < NUM_CLOUDLETS; i++) {
            cloudletLengths[i] = HETERO_CLOUDLET_LENGTH_MIN +
                Math.abs(random.nextLong() % (HETERO_CLOUDLET_LENGTH_MAX - HETERO_CLOUDLET_LENGTH_MIN + 1));
            if (cloudletLengths[i] < HETERO_CLOUDLET_LENGTH_MIN) {
                cloudletLengths[i] = HETERO_CLOUDLET_LENGTH_MIN;
            }
        }

        // 测试三个算法
        results.put("CBO", runMultipleTests("CBO", vmMips, cloudletLengths, "异构参数"));
        results.put("EDCBO", runMultipleTests("EDCBO", vmMips, cloudletLengths, "异构参数"));
        results.put("EDCBO-Fixed", runMultipleTests("EDCBO-Fixed", vmMips, cloudletLengths, "异构参数"));

        printDatasetSummary("异构参数", results);
        return results;
    }

    /**
     * 运行多次测试（10次）
     */
    private static DatasetResult runMultipleTests(String algorithm, int[] vmMips,
                                                  long[] cloudletLengths, String datasetName) {
        System.out.println("----------------------------------------");
        System.out.println("测试算法: " + algorithm);
        System.out.println("----------------------------------------");

        List<Double> makespans = new ArrayList<>();
        List<Long> runtimes = new ArrayList<>();

        for (int run = 1; run <= NUM_RUNS; run++) {
            System.out.print(String.format("  运行 %d/%d ... ", run, NUM_RUNS));

            long startTime = System.currentTimeMillis();
            double makespan = runSingleTest(algorithm, vmMips, cloudletLengths);
            long endTime = System.currentTimeMillis();

            makespans.add(makespan);
            runtimes.add(endTime - startTime);

            System.out.println(String.format("Makespan=%.2f, 用时=%dms", makespan, endTime - startTime));
        }

        double avgMakespan = makespans.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double stdMakespan = calculateStd(makespans);
        double minMakespan = makespans.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double maxMakespan = makespans.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        long avgRuntime = (long) runtimes.stream().mapToLong(Long::longValue).average().orElse(0.0);

        System.out.println(String.format("  平均Makespan: %.2f ± %.2f 秒", avgMakespan, stdMakespan));
        System.out.println(String.format("  范围: [%.2f, %.2f] 秒", minMakespan, maxMakespan));
        System.out.println(String.format("  平均运行时间: %d ms\n", avgRuntime));

        return new DatasetResult(avgMakespan, stdMakespan, minMakespan, maxMakespan, avgRuntime);
    }

    /**
     * 运行单次测试
     */
    private static double runSingleTest(String algorithm, int[] vmMips, long[] cloudletLengths) {
        CloudSimPlus simulation = new CloudSimPlus();
        Datacenter datacenter = createDatacenter(simulation);

        Object broker;
        if (algorithm.equals("CBO")) {
            broker = new CBO_Broker(simulation, SEED);
        } else if (algorithm.equals("EDCBO")) {
            broker = new EDCBO_Broker(simulation, SEED);
        } else if (algorithm.equals("EDCBO-Fixed")) {
            broker = new EDCBO_Broker_Fixed(simulation, SEED);
        } else {
            throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }

        List<Vm> vmList = new ArrayList<>();
        for (int i = 0; i < NUM_VMS; i++) {
            Vm vm = new VmSimple(vmMips[i], 1)
                .setRam(2048).setBw(1000).setSize(10000);
            vmList.add(vm);
        }

        List<Cloudlet> cloudletList = new ArrayList<>();
        for (int i = 0; i < NUM_CLOUDLETS; i++) {
            Cloudlet cloudlet = new CloudletSimple(cloudletLengths[i], 1)
                .setFileSize(300).setOutputSize(300)
                .setUtilizationModelCpu(new UtilizationModelFull());
            cloudletList.add(cloudlet);
        }

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

        double makespan = 0.0;
        for (Cloudlet cloudlet : cloudletList) {
            double finishTime = cloudlet.getFinishTime();
            if (finishTime > makespan) {
                makespan = finishTime;
            }
        }

        return makespan;
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

    private static double calculateStd(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    /**
     * 打印单个数据集的结果摘要
     */
    private static void printDatasetSummary(String datasetName, Map<String, DatasetResult> results) {
        System.out.println("\n========================================");
        System.out.println("  " + datasetName + "数据集结果汇总");
        System.out.println("========================================\n");

        System.out.println("┌──────────────────┬──────────────┬──────────────┬──────────────┐");
        System.out.println("│ 算法             │ 平均Makespan │ 标准差       │ 平均用时(ms) │");
        System.out.println("├──────────────────┼──────────────┼──────────────┼──────────────┤");

        double cboMakespan = results.get("CBO").avgMakespan;

        for (Map.Entry<String, DatasetResult> entry : results.entrySet()) {
            String name = entry.getKey();
            DatasetResult result = entry.getValue();
            double improvement = ((cboMakespan - result.avgMakespan) / cboMakespan) * 100;

            System.out.printf("│ %-16s │ %12.2f │ %12.2f │ %12d │%n",
                name, result.avgMakespan, result.stdMakespan, result.avgRuntime);
        }
        System.out.println("└──────────────────┴──────────────┴──────────────┴──────────────┘");

        System.out.println("\n改进率分析（相对CBO）：");
        for (Map.Entry<String, DatasetResult> entry : results.entrySet()) {
            if (!entry.getKey().equals("CBO")) {
                double improvement = ((cboMakespan - entry.getValue().avgMakespan) / cboMakespan) * 100;
                String direction = improvement > 0 ? "改进" : "退化";
                System.out.printf("  %s: %.2f%% (%s)%n", entry.getKey(), Math.abs(improvement), direction);
            }
        }
    }

    /**
     * 打印最终综合对比
     */
    private static void printFinalComparison(Map<String, DatasetResult> fixedResults,
                                            Map<String, DatasetResult> heteroResults) {
        System.out.println("\n\n");
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║   综合对比报告：两个数据集完整结果                               ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        System.out.println("┌──────────────────┬────────────────────────┬────────────────────────┐");
        System.out.println("│ 算法             │ 固定参数 Makespan(秒) │ 异构参数 Makespan(秒) │");
        System.out.println("├──────────────────┼────────────────────────┼────────────────────────┤");

        for (String algorithm : Arrays.asList("CBO", "EDCBO", "EDCBO-Fixed")) {
            DatasetResult fixed = fixedResults.get(algorithm);
            DatasetResult hetero = heteroResults.get(algorithm);

            System.out.printf("│ %-16s │ %22.2f │ %22.2f │%n",
                algorithm, fixed.avgMakespan, hetero.avgMakespan);
        }
        System.out.println("└──────────────────┴────────────────────────┴────────────────────────┘");

        System.out.println("\n改进率对比表（相对CBO基准）：");
        System.out.println("┌──────────────────┬────────────────────────┬────────────────────────┐");
        System.out.println("│ 算法             │ 固定参数改进率(%)     │ 异构参数改进率(%)     │");
        System.out.println("├──────────────────┼────────────────────────┼────────────────────────┤");

        double cboFixed = fixedResults.get("CBO").avgMakespan;
        double cboHetero = heteroResults.get("CBO").avgMakespan;

        for (String algorithm : Arrays.asList("CBO", "EDCBO", "EDCBO-Fixed")) {
            DatasetResult fixed = fixedResults.get(algorithm);
            DatasetResult hetero = heteroResults.get(algorithm);

            double improvementFixed = ((cboFixed - fixed.avgMakespan) / cboFixed) * 100;
            double improvementHetero = ((cboHetero - hetero.avgMakespan) / cboHetero) * 100;

            String fixedStr = String.format("%+.2f%%", improvementFixed);
            String heteroStr = String.format("%+.2f%%", improvementHetero);

            System.out.printf("│ %-16s │ %22s │ %22s │%n",
                algorithm, fixedStr, heteroStr);
        }
        System.out.println("└──────────────────┴────────────────────────┴────────────────────────┘");

        System.out.println("\n✅ 关键发现：");

        DatasetResult edcboFixedFixed = fixedResults.get("EDCBO-Fixed");
        DatasetResult edcboFixedHetero = heteroResults.get("EDCBO-Fixed");

        double improvementFixed = ((cboFixed - edcboFixedFixed.avgMakespan) / cboFixed) * 100;
        double improvementHetero = ((cboHetero - edcboFixedHetero.avgMakespan) / cboHetero) * 100;

        System.out.println("  1. 固定参数数据集：");
        System.out.printf("     - CBO基准: %.2f秒%n", cboFixed);
        System.out.printf("     - EDCBO-Fixed: %.2f秒%n", edcboFixedFixed.avgMakespan);
        System.out.printf("     - 改进率: %.2f%%%n", improvementFixed);

        System.out.println("\n  2. 异构参数数据集：");
        System.out.printf("     - CBO基准: %.2f秒%n", cboHetero);
        System.out.printf("     - EDCBO-Fixed: %.2f秒%n", edcboFixedHetero.avgMakespan);
        System.out.printf("     - 改进率: %.2f%%%n", improvementHetero);

        System.out.println("\n  3. 算法稳定性：");
        System.out.printf("     - 固定参数标准差: %.2f秒%n", edcboFixedFixed.stdMakespan);
        System.out.printf("     - 异构参数标准差: %.2f秒%n", edcboFixedHetero.stdMakespan);

        if (improvementFixed > 15.0 && improvementHetero > 15.0) {
            System.out.println("\n🎉 优秀！EDCBO-Fixed在两个数据集上都实现了显著改进（>15%）！");
        } else if (improvementFixed > 10.0 && improvementHetero > 10.0) {
            System.out.println("\n✅ 良好！EDCBO-Fixed在两个数据集上都实现了可观改进（>10%）！");
        }
    }

    /**
     * 数据集结果记录
     */
    static class DatasetResult {
        double avgMakespan;
        double stdMakespan;
        double minMakespan;
        double maxMakespan;
        long avgRuntime;

        DatasetResult(double avg, double std, double min, double max, long runtime) {
            this.avgMakespan = avg;
            this.stdMakespan = std;
            this.minMakespan = min;
            this.maxMakespan = max;
            this.avgRuntime = runtime;
        }
    }
}
