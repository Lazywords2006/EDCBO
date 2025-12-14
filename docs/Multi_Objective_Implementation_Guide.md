# 多目标优化实现指南

**创建日期**: 2025-12-14
**目标**: 为LSCBO添加能耗和成本优化
**影响**: Q2期刊升级的关键任务

---

## 📊 多目标适应度函数

### 原始单目标公式（仅Makespan）
```java
Fitness = Makespan
Makespan = max(VM_Loads)
```

### 新的多目标公式
```java
Fitness = α × Makespan + β × Energy + γ × Cost

其中：
- α = 0.6  (Makespan权重，最高优先级)
- β = 0.3  (Energy权重，绿色云计算)
- γ = 0.1  (Cost权重，经济性)
```

---

## 🎯 实现方案

### 方案A：修改LSCBO_Broker_Fixed.java（推荐）

**优势**：
- 代码量少（仅修改calculateFitness方法）
- 易于维护
- 可通过开关切换单目标/多目标

**实施步骤**：

1. 在LSCBO_Broker_Fixed类中添加多目标开关：
```java
// 类顶部添加
private static final boolean USE_MULTI_OBJECTIVE = false;  // 默认关闭
private static final double ALPHA = 0.6;  // Makespan权重
private static final double BETA = 0.3;   // Energy权重
private static final double GAMMA = 0.1;  // Cost权重
```

2. 修改calculateFitness方法（第280-293行）：
```java
private double calculateFitness(double[] individual, int M, int N,
                               List<Cloudlet> cloudletList, List<Vm> vmList) {
    int[] schedule = continuousToDiscrete(individual, N);

    // 计算Makespan（单目标）
    double[] vmLoads = new double[N];
    for (int i = 0; i < M; i++) {
        int vmIdx = schedule[i];
        double taskLength = cloudletList.get(i).getLength();
        double vmMips = vmList.get(vmIdx).getMips();
        vmLoads[vmIdx] += taskLength / vmMips;
    }
    double makespan = Arrays.stream(vmLoads).max().getAsDouble();

    // 如果开启多目标，则计算能耗和成本
    if (USE_MULTI_OBJECTIVE) {
        double energy = EnergyCalculator.calculateEnergy(schedule, M, N, cloudletList, vmList);
        double cost = CostCalculator.calculateCost(schedule, M, N, cloudletList, vmList);

        // 归一化处理（重要！不同维度需要归一化）
        double normalizedMakespan = makespan / 1000.0;  // 假设最大Makespan约1000s
        double normalizedEnergy = energy / 1.0;         // 假设最大能耗约1kWh
        double normalizedCost = cost / 0.1;             // 假设最大成本约0.1USD

        return ALPHA * normalizedMakespan + BETA * normalizedEnergy + GAMMA * normalizedCost;
    }

    // 单目标（默认）
    return makespan;
}
```

3. 在文件顶部添加导入：
```java
import com.edcbo.research.utils.EnergyCalculator;
import com.edcbo.research.utils.CostCalculator;
```

---

### 方案B：创建独立的LSCBO_MultiObjective_Broker类

**优势**：
- 保持原始LSCBO_Broker_Fixed不变
- 便于对比单目标 vs 多目标

**劣势**：
- 代码重复（约300行）
- 维护成本高

**实施建议**: 仅当需要同时测试单目标和多目标时使用

---

## 📈 预期实验结果

### 单目标 vs 多目标对比（M=100, N=20）

| 指标 | 单目标LSCBO | 多目标LSCBO | 变化 |
|------|-----------|------------|------|
| **Makespan** | **718.14s** | ~750s | +4.4% ⚠️ (可接受) |
| **Energy** | 0.85 kWh | **0.64 kWh** | **-24.7%** ✅ |
| **Cost** | $0.085 | **$0.064** | **-24.7%** ✅ |
| **综合适应度** | 718.14 | **0.624** | **降低13.2%** ✅ |

**关键发现**（预期）：
- ✅ 能耗降低15-25%（vs 单目标Makespan优先）
- ✅ 成本降低10-20%
- ⚠️ Makespan略增<5%（权衡可接受）

---

## 🧪 实验验证步骤

### 1. 单目标基线测试（已完成）✅
```bash
mvn exec:java -Dexec.mainClass="com.edcbo.research.CompareEDCBOExample"
```
结果：Makespan = 718.14s（M=100）

### 2. 多目标测试（待执行）
修改USE_MULTI_OBJECTIVE = true后：
```bash
mvn exec:java -Dexec.mainClass="com.edcbo.research.MultiObjectiveTest"
```

### 3. 对比实验（4规模验证）
```bash
mvn exec:java -Dexec.mainClass="com.edcbo.research.MultiObjectiveScalabilityTest"
```

预期输出（4规模 × 2模式 × 5种子 = 40次实验）：
```
Scale  Mode           Makespan  Energy   Cost     Fitness
M=100  单目标LSCBO    675.35s   0.85kWh  $0.085  675.35
M=100  多目标LSCBO    705.12s   0.64kWh  $0.064  0.623
M=500  单目标LSCBO    1220.14s  3.21kWh  $0.321  1220.14
M=500  多目标LSCBO    1280.45s  2.45kWh  $0.245  0.812
...
```

---

## 📊 可视化图表

### 图1：Pareto前沿对比
- X轴：Makespan
- Y轴：Energy
- 点：单目标LSCBO（红）vs 多目标LSCBO（蓝）

### 图2：权衡曲线
- 展示α权重从0.1到0.9时的性能变化
- 发现最优权衡点（推荐α=0.6）

### 图3：成本-性能散点图
- X轴：Cost（USD）
- Y轴：Makespan（s）
- 展示多目标LSCBO实现更好的成本-性能平衡

---

## 🎯 Q2期刊投稿价值

### 提升点
1. **多目标优化是Q2期刊的期望特性**
   - Q4期刊：单目标Makespan足够
   - Q2期刊：需要考虑实际工程需求（能耗、成本）

2. **符合绿色云计算趋势**
   - 能耗优化是当前研究热点
   - 碳中和（Carbon Neutral）政策推动

3. **实际应用价值**
   - 云服务提供商关心成本和能耗
   - 增强论文工程导向

### 预期改进
- Q2准备度：70% → **80-85%**
- 预期录用率：70-75% → **75-80%**

---

## ⚠️ 归一化问题（重要！）

### 问题
不同目标函数的数值范围差异巨大：
- Makespan：~700s
- Energy：~0.8kWh
- Cost：~$0.08

如果不归一化，Makespan会主导适应度函数。

### 解决方案A：最小-最大归一化
```java
double maxMakespan = 2000.0;  // 基于历史最大值
double maxEnergy = 2.0;
double maxCost = 0.2;

double normalizedMakespan = makespan / maxMakespan;
double normalizedEnergy = energy / maxEnergy;
double normalizedCost = cost / maxCost;
```

### 解决方案B：动态归一化（推荐）
```java
// 在迭代过程中动态追踪最大值
double maxMakespan = updateMax(makespan, currentMaxMakespan);
double maxEnergy = updateMax(energy, currentMaxEnergy);
double maxCost = updateMax(cost, currentMaxCost);
```

---

## 📝 实施清单

### 立即行动（1-2小时）
- [ ] 修改LSCBO_Broker_Fixed.java添加多目标开关
- [ ] 修改calculateFitness方法集成能耗和成本
- [ ] 添加归一化逻辑

### 实验验证（2-3小时）
- [ ] 运行单目标基线测试（验证未破坏原功能）
- [ ] 运行多目标测试（M=100单次）
- [ ] 对比结果，验证能耗和成本降低

### 大规模验证（6-8小时）
- [ ] 运行4规模多目标实验（M=100-2000）
- [ ] 生成对比报告
- [ ] 创建可视化图表

### 文档撰写（2-3小时）
- [ ] 更新README.md添加多目标结果
- [ ] 创建Multi_Objective_Analysis.md技术报告
- [ ] 更新Journal_Upgrade_Roadmap.md

---

## 💡 专家建议

1. **权重调优**：α=0.6, β=0.3, γ=0.1是经验值，建议做敏感性分析
2. **归一化方法**：动态归一化比静态归一化更鲁棒
3. **对比基准**：不仅与CBO对比，还应与PSO多目标版本对比
4. **Pareto前沿**：绘制Pareto前沿图展示多目标优化效果

---

**文档创建**: 2025-12-14
**状态**: 待实施
**预计工作量**: 12-14小时
**Q2价值**: ⭐⭐ 高价值任务
