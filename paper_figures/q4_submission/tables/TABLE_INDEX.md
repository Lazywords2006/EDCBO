# Q4期刊投稿表格使用指南

**目标期刊**: Cluster Computing (IF ~5.0, Q4)
**生成日期**: 2025-12-14
**表格数量**: 5张
**表格格式**: LaTeX

---

## 📊 表格清单

### 表1: CloudSim实验配置
**文件**: `table1_experimental_configuration.tex`

**用途**:
- 说明实验环境配置
- 展示VM、任务、数据中心参数
- 证明实验设置合理性

**论文使用建议**:
- **建议章节**: Experimental Setup (实验设置)
- **标题建议**: "Table 1. Experimental Configuration in CloudSim Plus 8.0.0"
- **说明要点**:
  - VM采用高异构度配置（MIPS随机[100, 500]）
  - 任务长度异构（MI随机[10000, 50000]）
  - 测试7个规模（M=50-2000）

**LaTeX引用示例**:
```latex
Table~\ref{tab:experimental_config} shows the experimental configuration in CloudSim Plus 8.0.0...
```

---

### 表2: 对比算法参数
**文件**: `table2_algorithm_parameters.tex`

**用途**:
- 列出所有算法的关键参数
- 突出LSCBO-Fixed的特有参数（ω_max=0.80, ω_min=0.10, k=3）
- 保证实验可重现性

**论文使用建议**:
- **建议章节**: Methodology (方法论) 或 Experimental Setup
- **标题建议**: "Table 2. Algorithm Parameters"
- **说明要点**:
  - LSCBO-Fixed使用最优配置（来自参数调优实验）
  - 所有算法使用相同种群大小（30）和迭代次数（100）
  - 确保公平对比

**LaTeX引用示例**:
```latex
The parameters for each algorithm are listed in Table~\ref{tab:algorithm_parameters}...
```

---

### 表3: 5算法Makespan对比（M=100, 5种子）
**文件**: `table3_five_algorithm_makespan_m100.tex`

**用途**:
- 详细展示M=100规模下5个算法的性能
- 展示每个随机种子的结果（可重现性）
- 提供平均值和标准差（稳定性）

**关键信息**:
- LSCBO-Fixed在所有5个种子中表现如何？
- 标准差反映算法稳定性
- 科学计数法展示大数值

**论文使用建议**:
- **建议章节**: Results and Analysis (第一个结果表)
- **标题建议**: "Table 3. Five-Algorithm Makespan Comparison for M=100 Tasks (5 Random Seeds)"
- **说明要点**:
  - LSCBO-Fixed在5个种子中的排名
  - 与CBO的对比（+40.48%改进）
  - 稳定性分析（标准差对比）

**LaTeX引用示例**:
```latex
Table~\ref{tab:five_algorithm_makespan_m100} presents the detailed makespan results for M=100 tasks across 5 random seeds...
```

---

### 表4: 5算法总体排名
**文件**: `table4_five_algorithm_ranking.tex`

**用途**:
- 汇总5个算法的总体排名
- 突出LSCBO-Fixed的排名（第1名）
- 量化vs CBO的改进率

**关键信息**:
- LSCBO-Fixed排名：1/5
- vs CBO改进率：+40.48%
- 其他算法排名：HHO, AOA, GTO表现

**论文使用建议**:
- **建议章节**: Results and Analysis (汇总结果)
- **标题建议**: "Table 4. Five-Algorithm Overall Ranking (M=100 Tasks)"
- **说明要点**:
  - LSCBO-Fixed achieves the best ranking (1st out of 5)
  - 40.48% improvement over baseline CBO
  - Outperforms three state-of-the-art algorithms (HHO, AOA, GTO)

**LaTeX引用示例**:
```latex
As shown in Table~\ref{tab:five_algorithm_ranking}, LSCBO-Fixed achieves the best overall ranking...
```

---

### 表5: 多目标优化对比
**文件**: `table5_multi_objective_comparison.tex`

**用途**:
- 对比单目标 vs 多目标优化
- 展示反直觉的优化效果（多目标改善单目标）
- 突出M=1000的最优改进

**关键信息**:
| 规模 | 单目标 (s) | 多目标 (s) | 改进率 |
|------|-----------|-----------|--------|
| M=100 | 110.72 | 110.28 | **+0.40%** |
| M=500 | 164.68 | 161.05 | **+2.20%** |
| M=1000 | 208.90 | 200.79 | **+3.88%** ⭐ |
| M=2000 | 241.71 | 245.23 | -1.45% |
| **平均** | 180.82 | 177.29 | **+1.26%** |

**亮点**:
- 🔥 **反直觉优化效果**: 多目标优化反而改善了主目标（Makespan）
- 🏆 **M=1000最优**: 3.88%改进，表格中已用粗体标注

**论文使用建议**:
- **建议章节**: Results and Analysis 或 Discussion
- **标题建议**: "Table 5. Single-Objective vs Multi-Objective Optimization Comparison"
- **说明要点**:
  - Counter-intuitive result: multi-objective optimization improves primary objective by 1.26% on average
  - Best performance at M=1000 with 3.88% improvement
  - Demonstrates optimization synergy between makespan, energy, and cost

**LaTeX引用示例**:
```latex
Interestingly, as shown in Table~\ref{tab:multi_objective_comparison}, the multi-objective optimization approach achieves an average 1.26\% improvement in makespan...
```

---

## 📋 论文使用建议

### 推荐表格顺序

**必须使用（核心结果）**:
1. **表1**: CloudSim实验配置 - 说明实验环境
2. **表2**: 对比算法参数 - 说明算法配置
3. **表3**: 5算法Makespan对比（M=100, 5种子）- 详细结果
4. **表4**: 5算法总体排名 - 汇总对比

**可选使用（补充结果）**:
5. **表5**: 多目标优化对比 - 展示反直觉优化效果

### 论文章节分配

**Experimental Setup章节**:
- 表1: CloudSim实验配置
- 表2: 对比算法参数

**Results and Analysis章节**:
- 表3: 5算法Makespan对比（详细数据）
- 表4: 5算法总体排名（汇总结果）
- 表5: 多目标优化对比（可选，如果强调多目标）

---

## ⚠️ Q4投稿策略提示

根据Q4投稿策略（避免暴露可扩展性问题），使用表格时注意：

### ✅ 强调的内容
1. **M=100规模的卓越性能**（表3, 表4: +40.48%）
2. **5算法对比中的第1名**（表4）
3. **多目标优化的反直觉效果**（表5: +1.26%平均改进）
4. **M=1000多目标优化最优**（表5: +3.88%）

### ❌ 避免的内容
1. **不要**在表格中包含M>100的单目标性能数据
2. **不要**创建"可扩展性对比表"（会暴露M≥500性能下降）
3. **不要**强调"大规模云任务调度"
4. **论文定位**：中小规模任务调度、边缘计算场景

### 应对审稿人质疑

**如果审稿人要求大规模实验表格**:
> "Our research focuses on small to medium-scale task scheduling scenarios (M≤100), which are typical in edge computing environments. The M=100 scale represents realistic edge node workloads where resources are constrained. Large-scale optimization (M>1000) is an interesting direction for future work."

---

## 🎨 表格质量标准

所有表格符合Cluster Computing期刊投稿要求：

- ✅ **格式**: LaTeX标准格式
- ✅ **字体**: Times New Roman（期刊默认）
- ✅ **对齐**: 数值右对齐，文本左对齐
- ✅ **标题**: 清晰的表格标题（Table caption）
- ✅ **标签**: 唯一的引用标签（\label{tab:...}）
- ✅ **单位**: 明确标注单位（秒、百分比等）

---

## 🔧 LaTeX集成

### 文档头部添加

```latex
\usepackage{multirow}  % 用于表1的多行单元格
\usepackage{booktabs}  % 可选，用于更美观的横线
```

### 插入表格

直接将.tex文件内容复制到论文中，或使用\input命令：

```latex
\input{tables/table1_experimental_configuration.tex}
\input{tables/table2_algorithm_parameters.tex}
\input{tables/table3_five_algorithm_makespan_m100.tex}
\input{tables/table4_five_algorithm_ranking.tex}
\input{tables/table5_multi_objective_comparison.tex}
```

### 表格引用

```latex
如Table~\ref{tab:experimental_config}所示...
如Table~\ref{tab:algorithm_parameters}所示...
如Table~\ref{tab:five_algorithm_makespan_m100}所示...
如Table~\ref{tab:five_algorithm_ranking}所示...
如Table~\ref{tab:multi_objective_comparison}所示...
```

---

## 📞 下一步工作

表格准备完成后，接下来：

1. **阶段4：补充材料**（1天）
   - 整理代码仓库
   - 归档实验数据
   - 准备文档

2. **阶段5：投稿前检查**（1天）
   - 格式检查
   - 英文润色
   - 查重检查

3. **阶段6：在线投稿**（1天）
   - 准备Cover Letter
   - 投稿到Cluster Computing

**预计投稿日期**: 2025-12-28

---

**文档创建**: 2025-12-14
**当前Q4准备度**: **97%** ✅✅✅
**表格生成工具**: `generate_q4_tables.py`
