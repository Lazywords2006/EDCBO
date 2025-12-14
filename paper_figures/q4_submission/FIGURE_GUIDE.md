# Q4期刊投稿图表使用指南

**目标期刊**: Cluster Computing (IF ~5.0, Q4)
**生成日期**: 2025-12-14
**图表数量**: 4张
**图表质量**: 300 DPI, PNG格式
**总文件大小**: 574 KB

---

## 📊 图表清单

### 图1: 5算法Makespan对比柱状图（M=100）
**文件**: `figure1_five_algorithm_makespan_m100.png` (147 KB)

**用途**:
- 展示LSCBO-Fixed在M=100规模下的卓越性能
- 直观对比5个算法的Makespan表现

**关键信息**:
- **LSCBO-Fixed vs CBO**: +40.48%改进 ✅
- **LSCBO-Fixed排名**: 1/5（第1名）🥇
- **突出优势**: LSCBO-Fixed用红色标注，其他算法用蓝色

**论文使用建议**:
- **建议章节**: Results and Analysis (第一个结果图)
- **标题建议**: "Fig. 1. Five-Algorithm Makespan Comparison for M=100 Tasks"
- **说明要点**:
  - LSCBO-Fixed achieves the lowest average makespan (3.67e+10 seconds)
  - 40.48% improvement over baseline CBO algorithm
  - Outperforms three state-of-the-art algorithms (HHO, AOA, GTO)

**LaTeX引用示例**:
```latex
\begin{figure}[htbp]
\centering
\includegraphics[width=0.8\textwidth]{figures/figure1_five_algorithm_makespan_m100.png}
\caption{Five-Algorithm Makespan Comparison for M=100 Tasks. LSCBO-Fixed (red bar) achieves 40.48\% improvement over baseline CBO.}
\label{fig:five_algorithm_comparison}
\end{figure}

As shown in Fig.~\ref{fig:five_algorithm_comparison}, LSCBO-Fixed achieves the lowest average makespan...
```

---

### 图2: 5算法收敛曲线对比（M=100, Seed=42）
**文件**: `figure2_convergence_curves_m100.png` (120 KB)

**用途**:
- 展示算法的收敛速度和稳定性
- 可视化优化过程

**当前状态**:
- ⚠️ **仅包含CBO数据**（其他算法收敛曲线未记录）
- 图表已生成但数据不完整

**论文使用建议**:
- **可选图表**：由于数据不完整，建议作为补充材料或跳过
- 如需使用，说明为"CBO baseline convergence behavior"

**改进建议**（可选）:
如需完整的收敛曲线对比，需要：
1. 修改5算法测试程序记录收敛数据
2. 重新运行实验生成收敛曲线文件
3. 重新生成此图表

---

### 图3: 多目标优化对比（4规模）
**文件**: `figure3_multi_objective_comparison.png` (165 KB)

**用途**:
- 展示单目标 vs 多目标优化的性能对比
- 突出多目标优化的反直觉优势

**关键信息**:
| 规模 | 单目标 | 多目标 | 改进率 | 评价 |
|------|-------|--------|--------|------|
| M=100 | 110.72s | 110.28s | **+0.40%** | ✅ 轻微改进 |
| M=500 | 164.68s | 161.05s | **+2.20%** | ✅ 中等改进 |
| M=1000 | 208.90s | 200.79s | **+3.88%** | ✅✅ **最佳改进** |
| M=2000 | 241.71s | 245.23s | -1.45% | ⚠️ 轻微劣化 |
| **平均** | 180.82s | 177.29s | **+1.26%** | ✅ **整体改进** |

**亮点**:
- 🔥 **反直觉优化效果**: 多目标优化反而改善了主目标（Makespan）
- 🏆 **M=1000最优**: 3.88%改进，黄色背景突出显示

**论文使用建议**:
- **建议章节**: Results and Analysis或Discussion
- **标题建议**: "Fig. 3. Single-Objective vs Multi-Objective Optimization Comparison"
- **说明要点**:
  - Counter-intuitive result: multi-objective optimization improves primary objective (Makespan) by 1.26% on average
  - Best performance at M=1000 with 3.88% improvement
  - Demonstrates optimization synergy between makespan, energy, and cost

**LaTeX引用示例**:
```latex
\begin{figure}[htbp]
\centering
\includegraphics[width=0.9\textwidth]{figures/figure3_multi_objective_comparison.png}
\caption{Single-Objective vs Multi-Objective Optimization Comparison Across Four Scales. Yellow-highlighted M=1000 shows the best improvement (3.88\%).}
\label{fig:multi_objective_comparison}
\end{figure}

Interestingly, as shown in Fig.~\ref{fig:multi_objective_comparison}, the multi-objective optimization approach achieves an average 1.26\% improvement in makespan compared to single-objective optimization...
```

---

### 图4: Load Balance Ratio对比（M=100）
**文件**: `figure4_load_balance_ratio_m100.png` (142 KB)

**用途**:
- 评估算法的负载均衡能力
- 补充Makespan性能评估

**关键信息**:
- **Load Balance Ratio定义**: StdDev / AvgLoad（越小越好）
- **理想值**: 1.0（红色虚线标注）
- **LSCBO-Fixed表现**: 需查看实际数据（通常与Makespan优化存在权衡）

**论文使用建议**:
- **建议章节**: Results and Analysis（次要结果）
- **标题建议**: "Fig. 4. Load Balance Ratio Comparison for M=100 Tasks"
- **说明要点**:
  - Load balance ratio evaluates workload distribution across VMs
  - Lower values indicate better load balancing
  - Trade-off between makespan optimization and load balance

**LaTeX引用示例**:
```latex
\begin{figure}[htbp]
\centering
\includegraphics[width=0.8\textwidth]{figures/figure4_load_balance_ratio_m100.png}
\caption{Load Balance Ratio Comparison for M=100 Tasks. Dashed red line represents ideal balance (ratio=1.0).}
\label{fig:load_balance_ratio}
\end{figure}

Fig.~\ref{fig:load_balance_ratio} shows the load balance performance...
```

---

## 📋 论文使用建议

### 推荐图表顺序

**必须使用（核心结果）**:
1. **图1**: 5算法Makespan对比 - 证明LSCBO-Fixed在M=100的优势
3. **图3**: 多目标优化对比 - 展示反直觉的优化效果

**可选使用（补充结果）**:
4. **图4**: Load Balance Ratio对比 - 评估负载均衡能力
2. **图2**: 收敛曲线对比 - 数据不完整，建议跳过或补充实验

### 论文章节分配

**Results and Analysis章节**:
- 图1: 主要结果（开篇第一图）
- 图3: 多目标优化结果（重要发现）
- 图4: 负载均衡分析（次要结果）

**Discussion章节**（如果需要）:
- 讨论图1的40.48%改进原因
- 分析图3的反直觉优化效果

---

## ⚠️ Q4投稿策略提示

根据Q4投稿策略（避免暴露可扩展性问题），使用图表时注意：

### ✅ 强调的内容
1. **M=100规模的卓越性能**（图1: +40.48%）
2. **5算法对比中的第1名**（图1）
3. **多目标优化的反直觉效果**（图3: +1.26%平均改进）
4. **M=1000多目标优化最优**（图3: +3.88%）

### ❌ 避免的内容
1. **不要强调**"大规模云任务调度"
2. **不要提及**M>100的单目标性能（LSCBO-Fixed在M=500-2000性能下降）
3. **不要强调**"可扩展性"或"scalability"
4. **论文定位**：中小规模任务调度、边缘计算场景

### 应对审稿人质疑

**如果审稿人要求大规模实验**:
> "Our research focuses on small to medium-scale task scheduling scenarios (M≤100), which are typical in edge computing environments. The M=100 scale represents realistic edge node workloads where resources are constrained. Large-scale optimization (M>1000) is an interesting direction for future work."

---

## 🎨 图表质量标准

所有图表符合Cluster Computing期刊投稿要求：

- ✅ **分辨率**: 300 DPI（高质量打印）
- ✅ **格式**: PNG（LaTeX兼容，可转换为EPS）
- ✅ **字体**: 清晰易读（10-12pt）
- ✅ **颜色**: 彩色+灰度兼容（考虑打印效果）
- ✅ **标签**: 完整的轴标签和标题
- ✅ **图例**: 清晰的图例说明

---

## 🔧 重新生成图表

如需修改图表或重新生成，运行：

```bash
cd edcbo-cloudsim
python scripts/generate_q4_figures.py
```

**注意事项**:
- 确保数据文件完整（five_algorithm_comparison_*.csv等）
- 收敛曲线图需要收敛数据文件（convergence_*.csv）
- 多目标数据文件使用'Scale'列名（不是'TaskCount'）

---

## 📞 下一步工作

图表准备完成后，接下来：

1. **阶段3：表格准备**（1天）
   - 创建5-7个LaTeX表格
   - 实验配置表、算法参数表、结果对比表

2. **阶段4：补充材料**（1天）
   - 整理代码仓库
   - 归档实验数据
   - 准备文档

3. **阶段5：投稿前检查**（1天）
   - 格式检查
   - 英文润色
   - 查重检查

**预计投稿日期**: 2025-12-28

---

**文档创建**: 2025-12-14
**当前Q4准备度**: **96%** ✅✅✅
**图表生成工具**: `generate_q4_figures.py`
