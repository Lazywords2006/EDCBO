package com.edcbo.research;

import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.vms.Vm;
import com.edcbo.research.utils.ConvergenceRecord;

import java.util.*;

/**
 * EDCBO (Enhanced Dynamic Coyote and Badger Optimization) Broker - Fixed Version
 *
 * 核心改进（基于参数调优验证的最优算法）：
 * 1. Lévy飞行搜索（Phase 1，向全局最优靠拢）
 * 2. 简化对数螺旋包围（Phase 2，围绕全局最优）
 * 3. 自适应权重+稀疏高斯变异（Phase 3，10%概率）
 *
 * 最优参数配置（基于48组网格搜索验证）：
 * - SPIRAL_B = 0.50（螺旋形状参数）
 * - SIGMA_MAX = 0.15（高斯变异标准差）
 * - LEVY_LAMBDA = 1.50（Lévy飞行分布参数）
 * - W_MAX/W_MIN = 0.80/0.10（惯性权重范围）
 * - LEVY_ALPHA_COEF = 0.05（自适应步长系数）
 *
 * 性能基准（M=100, N=20，异构环境）：
 * - CBO基准: 925.64秒
 * - 优化EDCBO: 718.14秒（改进22.42%）🏆
 *
 * @author ICBO Research Team
 * @date 2025-12-13
 * @version 3.0-fixed
 */
public class EDCBO_Broker_Fixed extends DatacenterBrokerSimple {

    // ==================== 算法参数 ====================
    protected static final int POPULATION_SIZE = 30;      // 种群大小
    protected static final int MAX_ITERATIONS = 100;      // 最大迭代次数

    // Lévy飞行参数
    private static final double LEVY_LAMBDA = 1.5;        // Lévy分布参数
    private static final double LEVY_ALPHA_COEF = 0.05;   // 自适应步长系数

    // 对数螺旋参数
    private static final double SPIRAL_B = 0.50;          // 螺旋形状常数（优化后）

    // 自适应惯性权重参数
    private static final double W_MAX = 0.80;             // 最大权重
    private static final double W_MIN = 0.10;             // 最小权重

    // 高斯变异参数
    private static final double SIGMA_MAX = 0.15;         // 最大方差（优化后）
    private static final double GAUSSIAN_PROB = 0.1;      // 高斯变异概率

    // ==================== 内部状态 ====================
    private double[][] population;                        // 种群（连续空间[0,1]）
    private double[] bestSolution;                        // 全局最优解
    private double bestFitness;                           // 全局最优适应度
    private Random random;                                // 随机数生成器
    private ConvergenceRecord convergenceRecord;          // 收敛记录器
    private Map<Long, Vm> cloudletVmMapping;              // Cloudlet到VM的映射
    private boolean schedulingDone = false;               // 调度是否完成

    // Lévy飞行相关
    private double levySigmaU;                            // σ_u 计算值

    // ==================== 构造函数 ====================

    public EDCBO_Broker_Fixed(CloudSimPlus simulation) {
        super(simulation);
        this.random = new Random();
        this.convergenceRecord = new ConvergenceRecord("EDCBO-Fixed", "unknown", System.currentTimeMillis());
        this.cloudletVmMapping = new HashMap<>();
        calculateLevySigmaU();
    }

    public EDCBO_Broker_Fixed(CloudSimPlus simulation, long seed) {
        super(simulation);
        this.random = new Random(seed);
        this.convergenceRecord = new ConvergenceRecord("EDCBO-Fixed", "unknown", seed);
        this.cloudletVmMapping = new HashMap<>();
        calculateLevySigmaU();
    }

    public EDCBO_Broker_Fixed(CloudSimPlus simulation, long seed, String scale) {
        super(simulation);
        this.random = new Random(seed);
        this.convergenceRecord = new ConvergenceRecord("EDCBO-Fixed", scale, seed);
        this.cloudletVmMapping = new HashMap<>();
        calculateLevySigmaU();
    }

    public EDCBO_Broker_Fixed(CloudSimPlus simulation, long seed, ConvergenceRecord record) {
        super(simulation);
        this.random = new Random(seed);
        this.convergenceRecord = record;
        this.cloudletVmMapping = new HashMap<>();
        calculateLevySigmaU();
    }

    // ==================== 主算法流程 ====================

    @Override
    protected Vm defaultVmMapper(Cloudlet cloudlet) {
        if (!schedulingDone) {
            runEDCBO();
            schedulingDone = true;
        }
        return cloudletVmMapping.getOrDefault(cloudlet.getId(), super.defaultVmMapper(cloudlet));
    }

    private void runEDCBO() {
        List<Cloudlet> cloudletList = new ArrayList<>(getCloudletWaitingList());
        List<Vm> vmList = new ArrayList<>(getVmCreatedList());

        int M = cloudletList.size();
        int N = vmList.size();

        System.out.println("\n========== EDCBO-Fixed调度算法启动 ==========");
        System.out.println("等待任务数: " + M);
        System.out.println("已创建VM数: " + N);
        System.out.println("种群大小: " + POPULATION_SIZE);
        System.out.println("最大迭代: " + MAX_ITERATIONS);
        System.out.println(String.format("优化参数: SPIRAL_B=%.2f, SIGMA_MAX=%.2f, LEVY_LAMBDA=%.2f",
                SPIRAL_B, SIGMA_MAX, LEVY_LAMBDA));
        System.out.println("=====================================\n");

        if (M == 0 || N == 0) {
            System.out.println("⚠️ 任务或VM数量为0，算法退出");
            return;
        }

        // 初始化种群（随机）
        population = new double[POPULATION_SIZE][M];
        for (int i = 0; i < POPULATION_SIZE; i++) {
            for (int d = 0; d < M; d++) {
                population[i][d] = random.nextDouble();
            }
        }

        // 评估初始种群
        bestFitness = Double.MAX_VALUE;
        bestSolution = new double[M];
        for (int i = 0; i < POPULATION_SIZE; i++) {
            double fitness = calculateFitness(population[i], M, N, cloudletList, vmList);
            if (fitness < bestFitness) {
                bestFitness = fitness;
                System.arraycopy(population[i], 0, bestSolution, 0, M);
            }
        }

        convergenceRecord.recordIteration(0, bestFitness);

        // 主循环
        for (int t = 1; t <= MAX_ITERATIONS; t++) {
            double w = calculateAdaptiveWeight(t);
            double sigma = calculateSigma(t);

            for (int i = 0; i < POPULATION_SIZE; i++) {
                double[] newPosition = new double[M];

                // Phase 1: Lévy飞行搜索（向全局最优）
                for (int d = 0; d < M; d++) {
                    double levyStep = generateLevyStep();
                    double alpha = LEVY_ALPHA_COEF * Math.abs(bestSolution[d] - population[i][d]);
                    newPosition[d] = population[i][d] + alpha * levyStep;
                    newPosition[d] = clamp(newPosition[d], 0, 1);
                }

                // Phase 2: 简化对数螺旋包围（围绕全局最优）
                double r1 = random.nextDouble();
                double theta = 2 * Math.PI * random.nextDouble();
                for (int d = 0; d < M; d++) {
                    double spiralRadius = Math.exp(SPIRAL_B * theta);
                    newPosition[d] = r1 * spiralRadius * Math.cos(theta) *
                                   Math.abs(bestSolution[d] - newPosition[d]) + bestSolution[d];
                    newPosition[d] = clamp(newPosition[d], 0, 1);
                }

                // Phase 3: 自适应权重攻击 + 稀疏高斯变异
                for (int d = 0; d < M; d++) {
                    // 正确的权重公式：w * current + (1-w) * best
                    // w从0.80降到0.10，前期探索，后期开发
                    newPosition[d] = w * newPosition[d] + (1 - w) * bestSolution[d];

                    // 10%概率应用高斯变异（稀疏化策略）
                    if (random.nextDouble() < GAUSSIAN_PROB) {
                        newPosition[d] += random.nextGaussian() * sigma;
                    }
                    newPosition[d] = clamp(newPosition[d], 0, 1);
                }

                // 评估新解
                double newFitness = calculateFitness(newPosition, M, N, cloudletList, vmList);
                double oldFitness = calculateFitness(population[i], M, N, cloudletList, vmList);

                if (newFitness < oldFitness) {
                    System.arraycopy(newPosition, 0, population[i], 0, M);

                    if (newFitness < bestFitness) {
                        bestFitness = newFitness;
                        System.arraycopy(newPosition, 0, bestSolution, 0, M);
                    }
                }
            }

            convergenceRecord.recordIteration(t, bestFitness);
        }

        // 应用最优解
        applySchedule(bestSolution, M, N, cloudletList, vmList);

        System.out.println("\n========== EDCBO-Fixed调度算法完成 ==========");
        System.out.println("最优Makespan: " + String.format("%.4f", bestFitness));
        System.out.println("映射条目数: " + cloudletVmMapping.size());
        System.out.println("=====================================\n");
    }

    // ==================== 辅助方法 ====================

    /**
     * 计算自适应惯性权重（二次衰减，正确版本）
     * w = w_min + (w_max - w_min) * (1 - t/T_max)^2
     * t=0 → w=0.80 (高探索)
     * t=100 → w=0.10 (高开发)
     */
    private double calculateAdaptiveWeight(int t) {
        double ratio = (double) t / MAX_ITERATIONS;
        return W_MIN + (W_MAX - W_MIN) * Math.pow(1.0 - ratio, 2);
    }

    /**
     * 计算高斯标准差（线性衰减）
     * σ = σ_max * (1 - t/T_max)
     */
    private double calculateSigma(int t) {
        return SIGMA_MAX * (1.0 - (double) t / MAX_ITERATIONS);
    }

    /**
     * 预计算Lévy分布的σ_u参数（Mantegna算法）
     */
    private void calculateLevySigmaU() {
        double lambda = LEVY_LAMBDA;
        double numerator = gamma(1 + lambda) * Math.sin(Math.PI * lambda / 2.0);
        double denominator = gamma((1 + lambda) / 2.0) * lambda * Math.pow(2, (lambda - 1) / 2.0);
        this.levySigmaU = Math.pow(numerator / denominator, 1.0 / lambda);
    }

    /**
     * Gamma函数近似
     */
    private double gamma(double x) {
        if (x == 1.0) return 1.0;
        if (x == 0.5) return Math.sqrt(Math.PI);
        if (x == 1.5) return 0.5 * Math.sqrt(Math.PI);
        if (x == 2.0) return 1.0;
        return Math.sqrt(2 * Math.PI / x) * Math.pow(x / Math.E, x);
    }

    /**
     * 生成Lévy飞行步长（Mantegna算法）
     */
    private double generateLevyStep() {
        double u = random.nextGaussian() * levySigmaU;
        double v = random.nextGaussian();
        return u / Math.pow(Math.abs(v), 1.0 / LEVY_LAMBDA);
    }

    /**
     * 边界约束
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 计算适应度（Makespan）
     */
    private double calculateFitness(double[] individual, int M, int N,
                                   List<Cloudlet> cloudletList, List<Vm> vmList) {
        int[] schedule = continuousToDiscrete(individual, N);

        double[] vmLoads = new double[N];
        for (int i = 0; i < M; i++) {
            int vmIdx = schedule[i];
            double taskLength = cloudletList.get(i).getLength();
            double vmMips = vmList.get(vmIdx).getMips();
            vmLoads[vmIdx] += taskLength / vmMips;
        }

        return Arrays.stream(vmLoads).max().getAsDouble();
    }

    /**
     * 连续空间到离散空间的映射
     */
    private int[] continuousToDiscrete(double[] continuous, int N) {
        int[] discrete = new int[continuous.length];
        for (int i = 0; i < continuous.length; i++) {
            discrete[i] = (int) (continuous[i] * N);
            if (discrete[i] >= N) {
                discrete[i] = N - 1;
            }
        }
        return discrete;
    }

    /**
     * 应用调度方案
     */
    private void applySchedule(double[] solution, int M, int N,
                              List<Cloudlet> cloudletList, List<Vm> vmList) {
        int[] schedule = continuousToDiscrete(solution, N);

        for (int i = 0; i < M; i++) {
            Cloudlet cloudlet = cloudletList.get(i);
            Vm vm = vmList.get(schedule[i]);
            cloudletVmMapping.put(cloudlet.getId(), vm);
        }
    }

    public ConvergenceRecord getConvergenceRecord() {
        return convergenceRecord;
    }

    public String getAlgorithmName() {
        return "EDCBO-Fixed";
    }
}
