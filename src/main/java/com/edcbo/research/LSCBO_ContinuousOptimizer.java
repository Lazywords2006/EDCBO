package com.edcbo.research;

import com.edcbo.research.benchmark.BenchmarkFunction;
import org.apache.commons.math3.special.Gamma;
import java.util.Random;

/**
 * LSCBO-Lévy-Random 连续优化器（用于CEC2017基准测试）
 *
 * 核心机制：
 * - Phase 1: Lévy飞行全局搜索
 * - Phase 2: CBO旋转矩阵包围
 * - Phase 3: CBO动态攻击
 *
 * 参数配置：
 * - 种群大小: 30（参照ERTH论文）
 * - 最大迭代: 10,000（参照ERTH论文）
 * - Lévy参数: λ=1.5, α=0.05×(1-t/T)
 * - 初始化: 随机初始化（无混沌映射）
 */
public class LSCBO_ContinuousOptimizer {

    // 算法参数（参照ERTH论文Table 4）
    private static final int POPULATION_SIZE = 30;
    private static final int MAX_ITERATIONS = 10000;

    // Lévy飞行参数
    private static final double LEVY_LAMBDA = 1.5;      // 重尾分布指数
    private static final double LEVY_ALPHA_COEF = 0.05; // 步长系数

    private final Random random;
    private final BenchmarkFunction function;
    private final int dimensions;
    private final double lowerBound;
    private final double upperBound;

    // 种群和适应度
    private double[][] population;
    private double[] fitness;

    // 最优解
    private double[] bestSolution;
    private double bestFitness;

    // Lévy飞行相关
    private double levySigmaU;                            // σ_u 预计算值

    /**
     * 构造函数
     * @param function 基准函数
     * @param dimensions 维度数（D=30 for CEC2017）
     * @param seed 随机种子
     */
    public LSCBO_ContinuousOptimizer(BenchmarkFunction function, int dimensions, long seed) {
        this.function = function;
        this.dimensions = dimensions;
        this.lowerBound = function.getLowerBound();
        this.upperBound = function.getUpperBound();
        this.random = new Random(seed);

        this.population = new double[POPULATION_SIZE][dimensions];
        this.fitness = new double[POPULATION_SIZE];
        this.bestSolution = new double[dimensions];
        this.bestFitness = Double.MAX_VALUE;
        calculateLevySigmaU();
    }

    /**
     * 运行优化算法
     * @return 最优适应度值
     */
    public double optimize() {
        // 1. 随机初始化种群
        initializePopulation();

        // 2. 评估初始适应度
        evaluatePopulation();

        // 3. 主循环：10,000次迭代
        for (int t = 0; t < MAX_ITERATIONS; t++) {
            // 阶段概率：前33%搜索，中33%包围，后34%攻击
            double progress = (double) t / MAX_ITERATIONS;

            for (int i = 0; i < POPULATION_SIZE; i++) {
                double[] newPosition = new double[dimensions];

                if (progress < 0.33) {
                    // Phase 1: Lévy飞行全局搜索
                    levyFlightSearch(i, t, newPosition);
                } else if (progress < 0.66) {
                    // Phase 2: 旋转矩阵包围
                    rotationMatrixEncircling(i, t, newPosition);
                } else {
                    // Phase 3: 动态攻击
                    dynamicAttacking(i, newPosition);
                }

                // 边界处理
                for (int d = 0; d < dimensions; d++) {
                    newPosition[d] = clamp(newPosition[d], lowerBound, upperBound);
                }

                // 评估新位置
                double newFitness = function.evaluate(newPosition);

                // 贪婪选择
                if (newFitness < fitness[i]) {
                    population[i] = newPosition.clone();
                    fitness[i] = newFitness;

                    // 更新全局最优
                    if (newFitness < bestFitness) {
                        bestFitness = newFitness;
                        bestSolution = newPosition.clone();
                    }
                }
            }
        }

        return bestFitness;
    }

    /**
     * Phase 1: Lévy飞行全局搜索
     * 公式: x^(t+1) = x^t + α(t) × Lévy(λ) × (x_prey - x^t)
     */
    private void levyFlightSearch(int i, int t, double[] newPosition) {
        // 选择猎物（50%最优个体，50%随机个体）
        int preyIdx = random.nextDouble() < 0.5 ?
                      findBestIndividualIndex() :
                      random.nextInt(POPULATION_SIZE);

        // 自适应步长系数：α(t) = α_0 × (1 - t/T)
        double alpha = LEVY_ALPHA_COEF * (1.0 - (double) t / MAX_ITERATIONS);

        for (int d = 0; d < dimensions; d++) {
            double levyStep = generateLevyStep();
            double distance = population[preyIdx][d] - population[i][d];

            newPosition[d] = population[i][d] + alpha * levyStep * distance;
        }
    }

    /**
     * Phase 2: 旋转矩阵包围
     * 使用2D旋转矩阵收紧包围圈
     */
    private void rotationMatrixEncircling(int i, int t, double[] newPosition) {
        // 旋转角度：θ = 2π × t / T_max
        double theta = 2.0 * Math.PI * t / MAX_ITERATIONS;
        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);

        // 对每对相邻维度应用旋转矩阵
        for (int d = 0; d < dimensions - 1; d += 2) {
            double diff1 = bestSolution[d] - population[i][d];
            double diff2 = bestSolution[d + 1] - population[i][d + 1];

            // 旋转矩阵: [cos(θ) -sin(θ)] × [diff1]
            //           [sin(θ)  cos(θ)]   [diff2]
            newPosition[d] = population[i][d] + (cosTheta * diff1 - sinTheta * diff2);
            newPosition[d + 1] = population[i][d + 1] + (sinTheta * diff1 + cosTheta * diff2);
        }

        // 奇数维度：使用线性收敛
        if (dimensions % 2 == 1) {
            double r = random.nextDouble();
            newPosition[dimensions - 1] = population[i][dimensions - 1]
                + r * (bestSolution[dimensions - 1] - population[i][dimensions - 1]);
        }
    }

    /**
     * Phase 3: 动态攻击（领导者跟随）
     * 公式: x^(t+1) = 0.5 × x^t + 0.5 × x_best
     */
    private void dynamicAttacking(int i, double[] newPosition) {
        for (int d = 0; d < dimensions; d++) {
            newPosition[d] = 0.5 * population[i][d] + 0.5 * bestSolution[d];
        }
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
     * 公式: Lévy ~ u / |v|^(1/λ)
     * 其中 u ~ N(0, σ_u^2), v ~ N(0, 1)
     * σ_u 已预计算
     */
    private double generateLevyStep() {
        // 生成正态分布随机数
        double u = random.nextGaussian() * levySigmaU;
        double v = random.nextGaussian();

        // Lévy步长（添加epsilon保护）
        return u / Math.pow(Math.abs(v) + 1e-10, 1.0 / LEVY_LAMBDA);
    }

    /**
     * 随机初始化种群（在[lowerBound, upperBound]范围内均匀分布）
     */
    private void initializePopulation() {
        double range = upperBound - lowerBound;
        for (int i = 0; i < POPULATION_SIZE; i++) {
            for (int d = 0; d < dimensions; d++) {
                population[i][d] = lowerBound + random.nextDouble() * range;
            }
        }
    }

    /**
     * 评估整个种群的适应度
     */
    private void evaluatePopulation() {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            fitness[i] = function.evaluate(population[i]);

            // 更新全局最优
            if (fitness[i] < bestFitness) {
                bestFitness = fitness[i];
                bestSolution = population[i].clone();
            }
        }
    }

    /**
     * 查找当前最优个体索引
     */
    private int findBestIndividualIndex() {
        int bestIdx = 0;
        for (int i = 1; i < POPULATION_SIZE; i++) {
            if (fitness[i] < fitness[bestIdx]) {
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    /**
     * 边界限制
     */
    private double clamp(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * 获取最优解
     */
    public double[] getBestSolution() {
        return bestSolution.clone();
    }

    /**
     * 获取最优适应度
     */
    public double getBestFitness() {
        return bestFitness;
    }

    /**
     * 获取最优解与理论最优值的误差
     */
    public double getError() {
        return Math.abs(bestFitness - function.getOptimalValue());
    }
}
