package com.edcbo.research.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 6算法CEC2017基准测试完整对比实验
 *
 * 测试配置：
 * - 6个算法：Random, PSO, GWO, WOA, CBO, LSCBO-Fixed
 * - 30个函数：F1-F30（完整CEC2017函数集）
 * - 30次独立运行（CEC2017标准配置）
 *
 * 实验规模：
 * - 快速验证：6算法 × 3函数 × 5次 = 90次测试（~8分钟）
 * - 完整实验：6算法 × 30函数 × 30次 = 5400次测试（~3-4小时）
 *
 * 输出格式：
 * - 统计数据CSV：算法×函数的平均值、标准差等
 * - 原始数据CSV：每次运行的详细结果
 * - 对比报告MD：Markdown格式的结果表格
 *
 * @author EDCBO Research Team
 * @version 1.0 (6算法纯净版)
 * @date 2025-12-14
 */
public class SixAlgorithmCEC2017Test {

    // 实验配置
    private static final int MAX_ITERATIONS = 1000;  // CEC2017标准迭代次数
    private static final int NUM_RUNS = 30;          // 独立运行次数（完整实验）
    private static final int QUICK_NUM_RUNS = 5;     // 快速验证运行次数

    /**
     * 主函数 - 运行完整实验
     */
    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║  CEC2017 Benchmark Test - 6 Algorithms Comparison           ║");
        System.out.println("║  Random | PSO | GWO | WOA | CBO | LSCBO-Fixed              ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // 选择实验模式
        boolean quickMode = false;  // true=快速验证（90次测试），false=完整实验（5400次测试）

        if (quickMode) {
            runQuickTest();
        } else {
            runFullExperiment();
        }
    }

    /**
     * 运行快速验证测试
     * 6算法 × 3函数 × 5次 = 90次测试
     */
    public static void runQuickTest() {
        System.out.println("【快速验证模式】\n");
        System.out.println("测试配置：");
        System.out.println("  - 算法：Random, PSO, GWO, WOA, CBO, LSCBO-Fixed");
        System.out.println("  - 函数：Sphere, Rastrigin, Ackley（代表性函数）");
        System.out.println("  - 运行次数：" + QUICK_NUM_RUNS);
        System.out.println("  - 迭代次数：" + MAX_ITERATIONS);
        System.out.println("  - 总测试量：6 × 3 × " + QUICK_NUM_RUNS + " = " + (6 * 3 * QUICK_NUM_RUNS) + " 次\n");

        // 创建算法列表（6个算法，按性能预期排序）
        List<BenchmarkRunner.BenchmarkOptimizer> algorithms = new ArrayList<>();
        algorithms.add(new Random_Lite());     // 基线算法
        algorithms.add(new PSO_Lite());        // 成熟元启发式
        algorithms.add(new GWO_Lite());        // 成熟元启发式
        algorithms.add(new WOA_Lite());        // 成熟元启发式
        algorithms.add(new CBO_Lite());        // 基准CBO
        algorithms.add(new LSCBO_Fixed_Lite()); // LSCBO-Fixed（CloudSim验证22.42%改进）

        // 获取快速测试函数（3个代表性函数）
        List<BenchmarkFunction> functions = BenchmarkRunner.getQuickTestFunctions();

        // 运行实验
        List<BenchmarkRunner.BenchmarkResult> allResults = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (BenchmarkFunction function : functions) {
            System.out.println("\n========================================");
            System.out.println("Testing: " + function.getName());
            System.out.println("========================================");

            for (BenchmarkRunner.BenchmarkOptimizer algorithm : algorithms) {
                BenchmarkRunner.BenchmarkResult result = BenchmarkRunner.runMultipleTests(
                    function, algorithm, MAX_ITERATIONS, QUICK_NUM_RUNS
                );
                result.printSummary();
                allResults.add(result);
            }
        }

        long endTime = System.currentTimeMillis();
        double elapsedMinutes = (endTime - startTime) / 60000.0;

        // 保存结果
        try {
            String baseFilename = "CEC2017_SixAlgorithm_QuickTest";
            BenchmarkResultWriter.writeAllFormats(allResults, baseFilename);
            System.out.println("\n✅ 快速验证完成！用时: " + String.format("%.2f", elapsedMinutes) + " 分钟");
        } catch (IOException e) {
            System.err.println("❌ 结果保存失败: " + e.getMessage());
        }
    }

    /**
     * 运行完整实验
     * 6算法 × 30函数 × 30次 = 5400次测试
     */
    public static void runFullExperiment() {
        System.out.println("【完整实验模式】\n");
        System.out.println("测试配置：");
        System.out.println("  - 算法：Random, PSO, GWO, WOA, CBO, LSCBO-Fixed");
        System.out.println("  - 函数：F1-F30（全部30个CEC2017函数）");
        System.out.println("  - 运行次数：" + NUM_RUNS);
        System.out.println("  - 迭代次数：" + MAX_ITERATIONS);
        System.out.println("  - 总测试量：6 × 30 × " + NUM_RUNS + " = " + (6 * 30 * NUM_RUNS) + " 次");
        System.out.println("  - 预计用时：3-4 小时\n");
        System.out.println("警告：这将是一个长时间运行的实验！\n");

        // 创建算法列表（6个算法）
        List<BenchmarkRunner.BenchmarkOptimizer> algorithms = new ArrayList<>();
        algorithms.add(new Random_Lite());     // 基线算法
        algorithms.add(new PSO_Lite());        // 成熟元启发式
        algorithms.add(new GWO_Lite());        // 成熟元启发式
        algorithms.add(new WOA_Lite());        // 成熟元启发式
        algorithms.add(new CBO_Lite());        // 基准CBO
        algorithms.add(new LSCBO_Fixed_Lite()); // LSCBO-Fixed（CloudSim验证22.42%改进）

        // 获取全部CEC2017函数
        List<BenchmarkFunction> functions = BenchmarkRunner.getAllFunctions();

        // 运行实验
        List<BenchmarkRunner.BenchmarkResult> allResults = new ArrayList<>();

        long startTime = System.currentTimeMillis();
        int totalTests = algorithms.size() * functions.size();
        int completedTests = 0;

        for (BenchmarkFunction function : functions) {
            System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║  Testing: " + String.format("%-50s", function.getName()) + " ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝");

            for (BenchmarkRunner.BenchmarkOptimizer algorithm : algorithms) {
                completedTests++;
                System.out.println(String.format("\n[进度: %d/%d] 算法: %s",
                                                completedTests, totalTests, algorithm.getName()));

                BenchmarkRunner.BenchmarkResult result = BenchmarkRunner.runMultipleTests(
                    function, algorithm, MAX_ITERATIONS, NUM_RUNS
                );
                result.printSummary();
                allResults.add(result);

                // 计算剩余时间估计
                long currentTime = System.currentTimeMillis();
                double elapsedMinutes = (currentTime - startTime) / 60000.0;
                double avgTimePerTest = elapsedMinutes / completedTests;
                double remainingTests = totalTests - completedTests;
                double estimatedRemainingMinutes = avgTimePerTest * remainingTests;

                System.out.println(String.format("已用时间: %.2f 分钟 | 预计剩余: %.2f 分钟",
                                                elapsedMinutes, estimatedRemainingMinutes));
            }
        }

        long endTime = System.currentTimeMillis();
        double elapsedMinutes = (endTime - startTime) / 60000.0;

        // 保存结果
        try {
            String baseFilename = "CEC2017_SixAlgorithm_FullExperiment";
            BenchmarkResultWriter.writeAllFormats(allResults, baseFilename);

            System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║              实验完成！                                         ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝");
            System.out.println("总用时: " + String.format("%.2f", elapsedMinutes) + " 分钟 (" +
                              String.format("%.2f", elapsedMinutes / 60.0) + " 小时)");
            System.out.println("总测试次数: " + (algorithms.size() * functions.size() * NUM_RUNS));

            // 打印排名概览
            printRankingSummary(allResults, functions);

        } catch (IOException e) {
            System.err.println("❌ 结果保存失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 打印6算法排名概览
     */
    private static void printRankingSummary(List<BenchmarkRunner.BenchmarkResult> allResults,
                                           List<BenchmarkFunction> functions) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║           6算法整体排名概览                                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // 统计每个算法在30个函数上的平均排名
        String[] algorithmNames = {"Random", "PSO", "GWO", "WOA", "CBO", "LSCBO-Fixed"};
        double[] avgRanks = new double[6];

        // 为每个函数计算排名
        for (BenchmarkFunction function : functions) {
            // 获取该函数上的6个结果
            List<BenchmarkRunner.BenchmarkResult> functionResults = new ArrayList<>();
            for (BenchmarkRunner.BenchmarkResult result : allResults) {
                if (result.getFunctionName().equals(function.getName())) {
                    functionResults.add(result);
                }
            }

            // 按平均适应度排序获取排名
            functionResults.sort((a, b) -> Double.compare(a.getAvgFitness(), b.getAvgFitness()));

            // 记录每个算法的排名
            for (int rank = 0; rank < functionResults.size(); rank++) {
                String algorithmName = functionResults.get(rank).getAlgorithmName();
                for (int i = 0; i < algorithmNames.length; i++) {
                    if (algorithmName.equals(algorithmNames[i])) {
                        avgRanks[i] += (rank + 1);  // 排名从1开始
                        break;
                    }
                }
            }
        }

        // 计算平均排名
        for (int i = 0; i < 6; i++) {
            avgRanks[i] /= functions.size();
        }

        // 按平均排名排序并打印
        Integer[] indices = {0, 1, 2, 3, 4, 5};
        java.util.Arrays.sort(indices, (a, b) -> Double.compare(avgRanks[a], avgRanks[b]));

        System.out.println("排名 | 算法 | 平均排名");
        System.out.println("-----|------|--------");
        for (int i = 0; i < 6; i++) {
            int idx = indices[i];
            String medal = (i == 0) ? "🥇" : (i == 1) ? "🥈" : (i == 2) ? "🥉" : " ";
            System.out.println(String.format("%s %d  | %-12s | %.2f",
                medal, i + 1, algorithmNames[idx], avgRanks[idx]));
        }

        System.out.println("\n说明：排名越低越好（1.00为最优）");
        System.out.println("详细结果请查看生成的CSV和MD文件\n");
    }
}
