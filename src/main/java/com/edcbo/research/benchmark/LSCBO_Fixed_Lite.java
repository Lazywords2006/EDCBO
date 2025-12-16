package com.edcbo.research.benchmark;

import org.apache.commons.math3.special.Gamma;
import java.util.Random;

/**
 * LSCBO-Fixed轻量级版本 - 专用于CEC2017基准测试
 *
 * 基于LSCBO_Broker_Fixed的核心算法，移除CloudSim依赖
 * 实现BenchmarkOptimizer接口，直接优化数学函数
 *
 * 核心改进（基于参数调优验证的最优算法）：
 * 1. Lévy飞行搜索（Phase 1，向全局最优靠拢）
 * 2. 简化对数螺旋包围（Phase 2，围绕全局最优）
 * 3. 自适应权重+稀疏高斯变异（Phase 3，10%概率）
 *
 * 最优参数配置（CEC2017基准优化v3 - 激进版）：
 * - SPIRAL_B = 1.0（螺旋形状参数，v3: 0.80→1.0）
 * - SIGMA_MAX = 0.25（高斯变异标准差，v3: 0.20→0.25）
 * - LEVY_LAMBDA = 1.50（Lévy飞行分布参数）
 * - W_MAX/W_MIN = 0.90/0.10（惯性权重范围，v3: 0.85→0.90）
 * - LEVY_ALPHA_COEF = 0.20（自适应步长系数，v3: 0.12→0.20）
 * - GAUSSIAN_PROB = 0.20（高斯变异概率，v3: 0.15→0.20）
 *
 * 优化历程：
 * - v1 (CloudSim优化): LEVY_ALPHA=0.05, SPIRAL_B=0.50, W_MAX=0.80
 * - v2 (CEC2017初步): LEVY_ALPHA=0.12, SPIRAL_B=0.80, W_MAX=0.85
 * - v3 (CEC2017激进): LEVY_ALPHA=0.20, SPIRAL_B=1.0, W_MAX=0.90
 *
 * 性能基准（CloudSim M=100, N=20，异构环境）：
 * - CBO基准: 925.64秒
 * - 优化LSCBO: 718.14秒（改进22.42%）🏆
 *
 * @author LSCBO Research Team
 * @date 2025-12-16
 * @version 1.0-stable
 */
public class LSCBO_Fixed_Lite implements BenchmarkRunner.BenchmarkOptimizer {

    // ==================== 算法参数 ====================
    private static final int POPULATION_SIZE = 30;      // 种群大小

    // Lévy飞行参数（CEC2017优化v3 - 激进版）
    private static final double LEVY_LAMBDA = 1.5;        // Lévy分布参数
    private static final double LEVY_ALPHA_COEF = 0.20;   // 自适应步长系数（v3: 0.12→0.20, +67%）

    // 对数螺旋参数
    private static final double SPIRAL_B = 1.0;           // 螺旋形状常数（v3: 0.80→1.0, +25%）

    // 自适应惯性权重参数
    private static final double W_MAX = 0.90;             // 最大权重（v3: 0.85→0.90, +6%）
    private static final double W_MIN = 0.10;             // 最小权重

    // 高斯变异参数
    private static final double SIGMA_MAX = 0.25;         // 最大方差（v3: 0.20→0.25, +25%）
    private static final double GAUSSIAN_PROB = 0.20;     // 高斯变异概率（v3: 0.15→0.20, +33%）

    // ==================== 内部状态 ====================
    private double[][] population;                        // 种群
    private double[] fitness;                             // 适应度
    private double[] bestSolution;                        // 全局最优解
    private double bestFitness;                           // 全局最优适应度
    private final Random random;                          // 随机数生成器
    private final long seed;                              // 随机种子

    // Lévy飞行相关
    private double levySigmaU;                            // σ_u 计算值

    // ==================== 构造函数 ====================

    /**
     * 构造函数（带随机种子）
     * @param seed 随机种子
     */
    public LSCBO_Fixed_Lite(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
        calculateLevySigmaU();
    }

    /**
     * 构造函数（向后兼容，使用默认种子42）
     */
    public LSCBO_Fixed_Lite() {
        this(42L);
    }

    @Override
    public double optimize(BenchmarkFunction function, int maxIterations) {
        int dimensions = function.getDimensions();

        // 初始化种群
        initializePopulation(function);

        // LSCBO-Fixed迭代
        for (int t = 0; t < maxIterations; t++) {
            double w = calculateAdaptiveWeight(t, maxIterations);
            double sigma = calculateSigma(t, maxIterations);

            for (int i = 0; i < POPULATION_SIZE; i++) {
                double[] newPosition = new double[dimensions];

                // Phase 1: Lévy飞行搜索（向全局最优）
                for (int d = 0; d < dimensions; d++) {
                    double levyStep = generateLevyStep();
                    double alpha = LEVY_ALPHA_COEF * Math.abs(bestSolution[d] - population[i][d]);
                    newPosition[d] = population[i][d] + alpha * levyStep;
                    newPosition[d] = clamp(newPosition[d], function.getLowerBound(), function.getUpperBound());
                }

                // Phase 2: 简化对数螺旋包围（围绕全局最优）
                double r1 = random.nextDouble();
                double theta = 2 * Math.PI * random.nextDouble();
                for (int d = 0; d < dimensions; d++) {
                    double spiralRadius = Math.exp(SPIRAL_B * theta);
                    newPosition[d] = r1 * spiralRadius * Math.cos(theta) *
                                   Math.abs(bestSolution[d] - newPosition[d]) + bestSolution[d];
                    newPosition[d] = clamp(newPosition[d], function.getLowerBound(), function.getUpperBound());
                }

                // Phase 3: 自适应权重攻击 + 稀疏高斯变异
                for (int d = 0; d < dimensions; d++) {
                    // 正确的权重公式：w * current + (1-w) * best
                    // w从0.80降到0.10，前期探索，后期开发
                    newPosition[d] = w * newPosition[d] + (1 - w) * bestSolution[d];

                    // 10%概率应用高斯变异（稀疏化策略）
                    if (random.nextDouble() < GAUSSIAN_PROB) {
                        newPosition[d] += random.nextGaussian() * sigma;
                    }
                    newPosition[d] = clamp(newPosition[d], function.getLowerBound(), function.getUpperBound());
                }

                // 评估新解
                double newFitness = function.evaluate(newPosition);

                if (newFitness < fitness[i]) {
                    System.arraycopy(newPosition, 0, population[i], 0, dimensions);
                    fitness[i] = newFitness;

                    if (newFitness < bestFitness) {
                        bestFitness = newFitness;
                        System.arraycopy(newPosition, 0, bestSolution, 0, dimensions);
                    }
                }
            }

            // 打印进度（每100次迭代）
            if ((t + 1) % 100 == 0 || t == 0) {
                System.out.println(String.format("  [LSCBO-Fixed Iter %4d/%d] Best=%.6e",
                    t + 1, maxIterations, bestFitness));
            }
        }

        return bestFitness;
    }

    @Override
    public String getName() {
        return "LSCBO-Fixed";
    }

    // ==================== 初始化 ====================

    /**
     * 初始化种群（随机生成）
     */
    private void initializePopulation(BenchmarkFunction function) {
        int dimensions = function.getDimensions();
        population = new double[POPULATION_SIZE][dimensions];
        fitness = new double[POPULATION_SIZE];

        for (int i = 0; i < POPULATION_SIZE; i++) {
            // 生成随机个体
            for (int j = 0; j < dimensions; j++) {
                double value = function.getLowerBound() +
                              random.nextDouble() * (function.getUpperBound() - function.getLowerBound());
                population[i][j] = value;
            }

            // 评估适应度
            fitness[i] = function.evaluate(population[i]);
        }

        // 初始化最优解
        int bestIdx = 0;
        for (int i = 1; i < POPULATION_SIZE; i++) {
            if (fitness[i] < fitness[bestIdx]) {
                bestIdx = i;
            }
        }
        bestSolution = population[bestIdx].clone();
        bestFitness = fitness[bestIdx];
    }

    // ==================== 辅助方法 ====================

    /**
     * 计算自适应惯性权重（二次衰减，正确版本）
     * w = w_min + (w_max - w_min) * (1 - t/T_max)^2
     * t=0 → w=0.80 (高探索)
     * t=T_max → w=0.10 (高开发)
     */
    private double calculateAdaptiveWeight(int t, int maxIterations) {
        double ratio = (double) t / maxIterations;
        return W_MIN + (W_MAX - W_MIN) * Math.pow(1.0 - ratio, 2);
    }

    /**
     * 计算高斯标准差（线性衰减）
     * σ = σ_max * (1 - t/T_max)
     */
    private double calculateSigma(int t, int maxIterations) {
        return SIGMA_MAX * (1.0 - (double) t / maxIterations);
    }

    /**
     * 计算Lévy飞行分布的σ_u参数（Mantegna方法）
     *
     * 理论基础：
     * - Mantegna, R. N. (1994). Fast, accurate algorithm for numerical
     *   simulation of Lévy stable stochastic processes.
     *   Physical Review E, 49(5), 4677-4683.
     *
     * 公式：σ_u = [Γ(1+λ)sin(πλ/2) / (Γ((1+λ)/2) × λ × 2^((λ-1)/2))]^(1/λ)
     *
     * 使用Apache Commons Math 3.6.1的Gamma函数替代Stirling近似，
     * 提供更高的数值精度。
     */
    private void calculateLevySigmaU() {
        double lambda = LEVY_LAMBDA;
        double numerator = Gamma.gamma(1 + lambda) * Math.sin(Math.PI * lambda / 2.0);
        double denominator = Gamma.gamma((1 + lambda) / 2.0) * lambda * Math.pow(2, (lambda - 1) / 2.0);
        this.levySigmaU = Math.pow(numerator / denominator, 1.0 / lambda);
    }

    /**
     * 生成Lévy飞行步长（Mantegna算法）
     */
    private double generateLevyStep() {
        double u = random.nextGaussian() * levySigmaU;
        double v = random.nextGaussian();
        return u / Math.pow(Math.abs(v) + 1e-10, 1.0 / LEVY_LAMBDA);
    }

    /**
     * 边界约束
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
