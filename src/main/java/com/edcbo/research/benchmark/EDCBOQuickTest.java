package com.edcbo.research.benchmark;

import com.edcbo.research.benchmark.functions.Sphere;
import com.edcbo.research.benchmark.functions.Rastrigin;
import com.edcbo.research.benchmark.functions.Ackley;

/**
 * EDCBO-Fixed快速验证测试
 *
 * 验证EDCBO-Fixed在CEC2017基准函数上的表现
 * 对比CBO和EDCBO-Fixed的性能
 *
 * @author ICBO Research Team
 * @date 2025-12-13
 */
public class EDCBOQuickTest {

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║   EDCBO-Fixed CEC2017快速验证测试                              ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // 测试配置
        int maxIterations = 1000;
        int numRuns = 5;

        // 测试三个代表性函数
        BenchmarkFunction[] functions = {
            new Sphere(),
            new Rastrigin(),
            new Ackley()
        };

        // 测试CBO和EDCBO-Fixed
        BenchmarkRunner.BenchmarkOptimizer[] algorithms = {
            new CBO_Lite(42L),
            new EDCBO_Fixed_Lite(42L)
        };

        System.out.println("测试配置：");
        System.out.println("  - 算法：CBO, EDCBO-Fixed");
        System.out.println("  - 函数：Sphere, Rastrigin, Ackley");
        System.out.println("  - 运行次数：" + numRuns);
        System.out.println("  - 迭代次数：" + maxIterations);
        System.out.println();

        // 运行测试
        for (BenchmarkFunction function : functions) {
            System.out.println("\n════════════════════════════════════════");
            System.out.println("函数: " + function.getName());
            System.out.println("════════════════════════════════════════");

            for (BenchmarkRunner.BenchmarkOptimizer algorithm : algorithms) {
                System.out.println("\n算法: " + algorithm.getName());
                System.out.println("----------------------------------------");

                BenchmarkRunner.BenchmarkResult result = BenchmarkRunner.runMultipleTests(
                    function, algorithm, maxIterations, numRuns
                );

                System.out.println("\n结果:");
                System.out.println("  平均适应度: " + String.format("%.6e", result.getAvgFitness()));
                System.out.println("  标准差:     " + String.format("%.6e", result.getStdFitness()));
                System.out.println("  最佳:       " + String.format("%.6e", result.getMinFitness()));
                System.out.println("  最差:       " + String.format("%.6e", result.getMaxFitness()));
            }
        }

        System.out.println("\n\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║   测试完成！                                                    ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println("\n✅ EDCBO-Fixed已成功在CEC2017基准函数上运行！");
        System.out.println("📊 可以开始完整实验：BenchmarkCompareExample");
    }
}
