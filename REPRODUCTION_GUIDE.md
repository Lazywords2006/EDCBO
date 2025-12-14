# 实验复现指南 (Reproduction Guide)

**项目**: EDCBO - Enhanced Dynamic Coyote and Badger Optimization
**目标期刊**: Cluster Computing (IF ~5.0, Q4)
**创建日期**: 2025-12-14

---

## 🎯 复现目标

本指南帮助审稿人和研究人员完全复现论文中的实验结果，包括：

1. **5算法对比实验**（M=100, 100次模拟）
2. **多目标优化实验**（4规模, 40次模拟）
3. **图表和表格生成**

**预计复现时间**: 1-2小时（不含Maven依赖下载）

---

## 📋 前置要求

### 系统要求

| 组件 | 要求 | 验证命令 |
|------|------|---------|
| **操作系统** | Windows 10/11, Linux, macOS | - |
| **Java** | JDK 11 或更高 | `java -version` |
| **Maven** | 3.6.0 或更高 | `mvn -version` |
| **Python** | 3.7+ (用于图表生成) | `python --version` |
| **内存** | 最少 4 GB RAM | - |
| **磁盘空间** | 最少 500 MB | - |

### 环境验证

运行以下命令验证环境：

```bash
# 验证Java
java -version

# 验证Maven
mvn -version

# 验证Python
python --version

# 验证Python库（可选）
python -c "import pandas; import matplotlib; import numpy; print('Python环境OK')"
```

**预期输出**:
```
java version "11.0.x" (或更高)
Apache Maven 3.6.x (或更高)
Python 3.7.x (或更高)
Python环境OK
```

---

## 📥 步骤1：获取源代码

### 方式A：从补充材料解压（推荐）

```bash
# 解压补充材料包
unzip EDCBO_Supplementary_Materials.zip
cd EDCBO_Supplementary_Materials

# 验证文件完整性
ls -R
```

### 方式B：从GitHub克隆（如提供）

```bash
# 克隆仓库
git clone https://github.com/Lazywords2006/EDCBO.git
cd EDCBO

# 切换到投稿版本分支
git checkout q4-submission-2025
```

---

## 🔧 步骤2：编译项目

### 2.1 清理并编译

```bash
# 清理旧构建
mvn clean

# 编译项目
mvn compile
```

**预期输出**:
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX.XXX s
```

### 2.2 验证编译结果

```bash
# 验证编译生成的class文件
ls target/classes/com/edcbo/research/

# 应该看到以下文件
# LSCBO_Broker_Fixed.class
# CBO_Broker.class
# HHO_Broker.class
# AOA_Broker.class
# GTO_Broker.class
# FiveAlgorithmComparisonTest.class
# MultiObjectiveScalabilityTest.class
```

---

## 🧪 步骤3：复现实验

### 实验A：5算法对比（M=100, 100次）

**目标**: 复现表3和表4的结果（LSCBO-Fixed排名1/5，+40.48%改进）

**运行命令**:
```bash
cd edcbo-cloudsim

# 运行5算法对比实验
mvn exec:java -Dexec.mainClass="com.edcbo.research.FiveAlgorithmComparisonTest"
```

**预计运行时间**: 10-15分钟

**输出文件**:
```
results/five_algorithm_comparison_YYYYMMDD_HHMMSS.csv
```

**验证步骤**:
1. 检查CSV文件是否包含100行数据
2. 确认5个算法（CBO, LSCBO-Fixed, HHO, AOA, GTO）
3. 验证4个规模（M=50, 100, 200, 300）
4. 验证5个种子（42, 123, 456, 789, 1024）

**预期关键结果**（M=100平均Makespan）:
```
LSCBO-Fixed: ~3.67e+10 秒 (最低，排名1/5)
HHO:         ~4.17e+10 秒
GTO:         ~4.24e+10 秒
CBO:         ~6.17e+10 秒 (基线)
AOA:         ~1.09e+11 秒
```

**计算改进率**:
```python
# 使用Python计算
import pandas as pd
df = pd.read_csv('results/five_algorithm_comparison_*.csv')
df_m100 = df[df['TaskCount'] == 100]

cbo_mean = df_m100[df_m100['Algorithm'] == 'CBO']['Makespan'].mean()
lscbo_mean = df_m100[df_m100['Algorithm'] == 'LSCBO-Fixed']['Makespan'].mean()
improvement = (cbo_mean - lscbo_mean) / cbo_mean * 100

print(f"LSCBO-Fixed vs CBO改进率: +{improvement:.2f}%")
# 预期输出: +40.48%
```

---

### 实验B：多目标优化（4规模, 40次）

**目标**: 复现表5的结果（平均改进+1.20%，M=1000最优+3.88%）

**运行命令**:
```bash
# 运行多目标优化实验
mvn exec:java -Dexec.mainClass="com.edcbo.research.MultiObjectiveScalabilityTest"
```

**预计运行时间**: 30-40分钟

**输出文件**:
```
results/multi_objective_scalability_part1_SingleObjective.csv
results/multi_objective_scalability_part2_MultiObjective.csv
```

**验证步骤**:
1. 检查Part1（单目标）是否包含20行数据
2. 检查Part2（多目标）是否包含20行数据
3. 确认4个规模（M=100, 500, 1000, 2000）
4. 确认5个种子（42, 123, 456, 789, 1024）

**预期关键结果**（平均Makespan）:

| 规模 | 单目标 (s) | 多目标 (s) | 改进率 | 状态 |
|------|-----------|-----------|--------|------|
| M=100 | 110.72 | 110.28 | +0.40% | ✅ |
| M=500 | 164.68 | 161.05 | +2.20% | ✅ |
| M=1000 | 208.90 | 200.79 | +3.88% | ✅ 最优 |
| M=2000 | 241.71 | 245.23 | -1.45% | ⚠️ |
| **平均** | 180.82 | 177.29 | **+1.20%** | ✅ |

**计算多目标改进率**:
```python
# 使用Python计算
import pandas as pd
import numpy as np

df_single = pd.read_csv('results/multi_objective_scalability_part1_SingleObjective.csv')
df_multi = pd.read_csv('results/multi_objective_scalability_part2_MultiObjective.csv')

scales = [100, 500, 1000, 2000]
for scale in scales:
    single_avg = df_single[df_single['Scale'] == scale]['Makespan'].mean()
    multi_avg = df_multi[df_multi['Scale'] == scale]['Makespan'].mean()
    improvement = (single_avg - multi_avg) / single_avg * 100
    print(f"M={scale}: 单目标={single_avg:.2f}s, 多目标={multi_avg:.2f}s, 改进={improvement:+.2f}%")

# 预期输出：
# M=100: 单目标=110.72s, 多目标=110.28s, 改进=+0.40%
# M=500: 单目标=164.68s, 多目标=161.05s, 改进=+2.20%
# M=1000: 单目标=208.90s, 多目标=200.79s, 改进=+3.88%
# M=2000: 单目标=241.71s, 多目标=245.23s, 改进=-1.45%
```

---

## 📊 步骤4：生成图表和表格

### 4.1 安装Python依赖（首次运行）

```bash
# 安装必需的Python库
pip install pandas matplotlib numpy seaborn
```

### 4.2 生成图表（4张PNG, 300 DPI）

```bash
# 运行图表生成脚本
python scripts/generate_q4_figures.py
```

**预期输出**:
```
Q4期刊投稿图表生成器
目标期刊: Cluster Computing (IF ~5.0)
================================================================================

生成图1: 5算法Makespan对比柱状图（M=100）...
  保存至: paper_figures/q4_submission/figures/figure1_five_algorithm_makespan_m100.png
  文件大小: 146.7 KB

生成图2: 5算法收敛曲线对比（M=100, Seed=42）...
  警告: 未找到收敛曲线数据，跳过此图表

生成图3: 多目标优化对比（4规模）...
  保存至: paper_figures/q4_submission/figures/figure3_multi_objective_comparison.png
  文件大小: 164.5 KB

多目标优化改进率：
  M= 100: +0.40% [OK]
  M= 500: +2.20% [OK]
  M=1000: +3.88% [OK]
  M=2000: -1.45% [--]
  平均改进率: +1.20%

生成图4: Load Balance Ratio对比（M=100）...
  保存至: paper_figures/q4_submission/figures/figure4_load_balance_ratio_m100.png
  文件大小: 141.9 KB

[OK] 成功生成 4 张图表！
```

### 4.3 生成表格（5张LaTeX）

```bash
# 运行表格生成脚本
python scripts/generate_q4_tables.py
```

**预期输出**:
```
Q4期刊投稿表格生成器
目标期刊: Cluster Computing (IF ~5.0)
================================================================================

生成表1: CloudSim实验配置表...
  保存至: paper_figures/q4_submission/tables/table1_experimental_configuration.tex
  文件大小: 0.9 KB

生成表2: 对比算法参数表...
  保存至: paper_figures/q4_submission/tables/table2_algorithm_parameters.tex
  文件大小: 0.8 KB

生成表3: 5算法Makespan对比（M=100, 5种子）...
  保存至: paper_figures/q4_submission/tables/table3_five_algorithm_makespan_m100.tex
  文件大小: 0.8 KB

生成表4: 5算法总体排名表...
  保存至: paper_figures/q4_submission/tables/table4_five_algorithm_ranking.tex
  文件大小: 0.5 KB

生成表5: 多目标优化对比表（4规模）...
  保存至: paper_figures/q4_submission/tables/table5_multi_objective_comparison.tex
  文件大小: 0.5 KB

[OK] 成功生成 5 张表格！
```

---

## ✅ 步骤5：结果验证

### 验证清单

| 项目 | 预期结果 | 验证方法 |
|------|---------|---------|
| **实验A数据** | 100行CSV数据 | `wc -l results/five_algorithm_*.csv` |
| **实验B数据** | 2个CSV文件各20行 | `wc -l results/multi_objective_*.csv` |
| **图表** | 4张PNG文件（300 DPI） | `ls paper_figures/q4_submission/figures/*.png` |
| **表格** | 5个LaTeX文件 | `ls paper_figures/q4_submission/tables/*.tex` |

### 核心结果对比

#### 结果1：LSCBO-Fixed vs CBO改进率（M=100）

**论文声称**: +40.48%
**复现结果**: _____% （填写你的结果）

**可接受误差**: ±5%（由于随机性）

#### 结果2：多目标平均改进率

**论文声称**: +1.20%
**复现结果**: _____% （填写你的结果）

**可接受误差**: ±1%

#### 结果3：LSCBO-Fixed排名（M=100）

**论文声称**: 1/5（第1名）
**复现结果**: ____/5 （填写你的结果）

**可接受误差**: 排名应为1或2（由于随机性）

---

## 🐛 常见问题排除

### 问题1：Maven编译失败

**症状**:
```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin
```

**解决方法**:
```bash
# 清理Maven缓存
mvn clean

# 删除Maven本地仓库中的CloudSim Plus
rm -rf ~/.m2/repository/org/cloudsimplus

# 重新编译
mvn compile
```

### 问题2：内存不足错误

**症状**:
```
java.lang.OutOfMemoryError: Java heap space
```

**解决方法**:
```bash
# 增加Maven堆内存
export MAVEN_OPTS="-Xmx4096m"

# 重新运行实验
mvn exec:java -Dexec.mainClass="..."
```

### 问题3：Python库缺失

**症状**:
```
ModuleNotFoundError: No module named 'pandas'
```

**解决方法**:
```bash
# 安装所有必需的Python库
pip install pandas matplotlib numpy seaborn
```

### 问题4：实验运行时间过长

**症状**: 实验运行超过1小时仍未完成

**解决方法**:
1. 检查CPU使用率（应接近100%）
2. 检查日志文件中是否有错误
3. 如使用虚拟机，增加分配的CPU核心数

### 问题5：结果与论文差异较大

**原因**: 随机性导致的正常变异

**解决方法**:
1. **可接受误差**: 改进率±5%，排名±1名
2. **增加运行次数**: 如果差异仍大，增加实验次数（修改种子数量）
3. **检查Java版本**: 确保使用JDK 11或更高（JDK 8可能有细微差异）

---

## 📧 反馈和支持

如果遇到无法解决的问题，请联系：

1. **GitHub Issues**: （如提供GitHub链接）
2. **Email**: （论文对应作者邮箱）
3. **提供信息**:
   - 操作系统和版本
   - Java和Maven版本（`java -version`, `mvn -version`）
   - 错误日志完整输出
   - 实验运行到的步骤

---

## 📚 参考文献

### 算法参考

- **CBO**: Khatab et al. (2025). Coyote and badger co-optimization algorithm. *Ain Shams Engineering Journal*.
- **HHO**: Heidari et al. (2019). Harris hawks optimization. *Future Generation Computer Systems*.
- **AOA**: Abualigah et al. (2021). Arithmetic optimization algorithm. *Computer Methods in Applied Mechanics and Engineering*.
- **GTO**: Abdollahzadeh et al. (2021). Gorilla troops optimizer. *Advanced Engineering Informatics*.

### CloudSim参考

- **CloudSim Plus**: Manoel Campos da Silva Filho et al. CloudSim Plus 8.0.0. https://cloudsimplus.org/

---

## ✅ 复现完成确认

完成所有步骤后，你应该获得：

- [ ] 100行实验A数据（5算法对比）
- [ ] 40行实验B数据（多目标优化）
- [ ] 4张高质量PNG图表（300 DPI）
- [ ] 5张LaTeX表格
- [ ] LSCBO-Fixed vs CBO改进率：~40% (±5%)
- [ ] 多目标平均改进率：~1.2% (±1%)
- [ ] LSCBO-Fixed排名：1或2 / 5

**预计总时间**: 1-2小时

**如果所有验证通过，恭喜你成功复现了实验结果！** ✅

---

**文档创建**: 2025-12-14
**版本**: 1.0
**预计更新**: 投稿后根据审稿人反馈更新
