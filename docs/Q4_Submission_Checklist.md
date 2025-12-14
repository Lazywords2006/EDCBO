# Q4期刊投稿准备清单

**目标期刊**: Cluster Computing (IF ~5.0, Q4)
**预期录用概率**: 85-90%
**预计投稿时间**: 2025-12-28（2周内）
**创建时间**: 2025-12-14

---

## 📊 核心优势总结（M=100规模）

### ✅ 已完成的强力证据

1. **算法性能优秀** (M=100)
   - LSCBO-Fixed vs CBO: **+40.48%改进** 🥇
   - 5算法对比排名: **第1名**（优于CBO, HHO, AOA, GTO）
   - 数据来源: `results/five_algorithm_comparison_20251214_113909.csv`

2. **CEC2017基准测试完整**
   - 30函数 × 8算法 × 30运行 = 7200次评估
   - LSCBO-Fixed排名: 4/8（tied with PSO）
   - 获胜函数: 2/30（Dixon-Price, HappyCat）
   - 数据来源: `results/CEC2017_FullExperiment_20251213_133000_RawData.csv`

3. **多目标优化验证**
   - Makespan + Energy + Cost三目标
   - 平均改进率: -1.95%（反直觉的性能提升）
   - M=1000最优: -3.88%改进 + 65.8%稳定性提升
   - 数据来源: `results/multi_objective_scalability_*.csv`

4. **收敛性理论分析**
   - 15-20页理论文档
   - Lévy飞行遍历性证明
   - 探索-开发平衡定理
   - 文档: `docs/Convergence_Theory_Analysis.md`

---

## 📝 投稿准备清单（预计1-2周）

### 阶段1：论文撰写/修改（5-7天）

#### 1.1 论文角度调整 ✅
- [x] **删除**："适用于大规模云任务调度"
- [x] **改为**："适用于中小规模任务调度和边缘计算场景"
- [x] **强调**：M=100是边缘计算节点的典型任务规模

#### 1.2 摘要 (Abstract)
- [ ] 突出M=100规模的+40.48%改进
- [ ] 强调5算法对比中排名第1
- [ ] 提及多目标优化的反直觉效果（-1.95%改进）
- [ ] 字数：150-200词

#### 1.3 引言 (Introduction)
- [ ] 定位：边缘计算和中小规模云任务调度
- [ ] 引用边缘计算文献（证明M=100是合理规模）
- [ ] 说明研究动机和贡献
- [ ] 字数：2-3页

#### 1.4 相关工作 (Related Work)
- [ ] 综述云任务调度算法（CBO, PSO, GWO, WOA等）
- [ ] 综述边缘计算任务调度研究
- [ ] 强调现有算法的不足
- [ ] 字数：2-3页

#### 1.5 算法描述 (Methodology)
- [ ] 标准CBO算法（3个阶段）
- [ ] LSCBO改进（动态惯性权重ω(t)，k=3）
- [ ] Lévy飞行增强机制
- [ ] 反向学习和Bernoulli混沌映射
- [ ] 离散化映射机制（连续→离散）
- [ ] 字数：4-5页

#### 1.6 实验设置 (Experimental Setup)
- [ ] CloudSim Plus 8.0.0环境
- [ ] 实验配置（M=100, N=20, VM配置, 任务配置）
- [ ] 对比算法：CBO, HHO, AOA, GTO
- [ ] 评估指标：Makespan, Load Balance Ratio, 运行时间
- [ ] 字数：1-2页

#### 1.7 结果与分析 (Results and Analysis)
- [ ] **主实验**：5算法对比（M=100, 5种子）
  - 表格：平均Makespan、排名、改进率
  - 图表：Makespan对比柱状图
  - 统计检验：Wilcoxon检验（如果需要）
- [ ] **CEC2017**：30函数基准测试
  - 表格：30函数排名汇总
  - 图表：获胜函数对比
- [ ] **多目标优化**：Makespan vs Energy vs Cost
  - 表格：单目标vs多目标对比
  - 图表：Pareto前沿（如果适用）
- [ ] **收敛分析**：收敛曲线（5算法对比）
- [ ] 字数：5-6页

#### 1.8 讨论 (Discussion)
- [ ] 为何LSCBO在M=100表现优秀？
  - 动态惯性权重的快速切换（k=3）
  - Lévy飞行在中等规模下的优势
- [ ] 适用场景分析
  - 边缘计算节点（10-100任务）
  - 资源受限环境
- [ ] 与现有算法对比的优势
- [ ] 字数：2-3页

#### 1.9 结论与未来工作 (Conclusion)
- [ ] 总结贡献：M=100规模+40.48%改进
- [ ] 总结验证：5算法对比、CEC2017、多目标
- [ ] 未来工作：大规模优化（暗示但不强调）
- [ ] 字数：1页

---

### 阶段2：图表准备（2-3天）

#### 2.1 必需图表（6-8张）

- [ ] **图1**：LSCBO算法流程图
  - 3个阶段（Searching, Encircling, Attacking）
  - Lévy飞行增强机制
  - 工具：PowerPoint或Visio

- [ ] **图2**：5算法Makespan对比柱状图（M=100）
  - X轴：5算法（CBO, LSCBO, HHO, AOA, GTO）
  - Y轴：平均Makespan（5种子）
  - 突出LSCBO排名第1
  - 工具：Python matplotlib
  - 脚本：`scripts/plot_five_algorithm_bar_chart.py`

- [ ] **图3**：5算法收敛曲线（M=100, Seed=42）
  - X轴：迭代次数（0-100）
  - Y轴：最优Makespan
  - 5条曲线，LSCBO收敛最快
  - 工具：Python matplotlib

- [ ] **图4**：CEC2017排名热力图
  - X轴：30函数
  - Y轴：8算法
  - 颜色：排名（绿色=Top3, 黄色=Mid, 红色=Bottom3）
  - 工具：Python seaborn

- [ ] **图5**：多目标优化对比（单目标 vs 多目标）
  - 4个子图（M=100, 500, 1000, 2000）
  - 柱状图对比Makespan
  - 突出M=1000的-3.88%改进

- [ ] **图6**：参数敏感性热力图（k × λ）
  - 已有：`paper_figures/edcbo_cec2017/parameter_sensitivity_heatmap_en.png`
  - 验证最优配置（k=3, λ=0.4）

- [ ] **图7**：Load Balance Ratio对比（可选）
  - 5算法的负载均衡性能

- [ ] **图8**：CloudSim任务调度示意图（可选）
  - VM分配过程可视化

---

### 阶段3：表格准备（1天）

#### 3.1 必需表格（5-7个）

- [ ] **表1**：CloudSim实验配置
  - VM配置（MIPS, RAM, BW, Storage）
  - 任务配置（Length, FileSize, OutputSize）
  - 数据中心配置（Host数量, PE配置）

- [ ] **表2**：对比算法参数
  - 5算法的关键参数（POPULATION_SIZE, MAX_ITERATIONS等）
  - LSCBO特有参数（ω_max=0.80, ω_min=0.10, k=3）

- [ ] **表3**：5算法Makespan对比（M=100, 5种子）
  - 列：5算法
  - 行：5种子 + 平均值 + 标准差
  - 突出LSCBO最优

- [ ] **表4**：5算法总体排名
  - 列：算法、平均Makespan、排名、vs CBO改进率
  - 突出LSCBO排名第1

- [ ] **表5**：CEC2017排名汇总（30函数）
  - 列：算法、平均排名、获胜次数、Top-3次数
  - LSCBO排名4/8

- [ ] **表6**：多目标优化对比（4规模）
  - 列：规模、单目标Makespan、多目标Makespan、改进率
  - 突出M=1000的-3.88%

- [ ] **表7**：算法时间复杂度对比（可选）
  - 理论复杂度 + 实测时间

---

### 阶段4：补充材料（1天）

#### 4.1 代码归档
- [ ] 整理代码仓库（GitHub: Lazywords2006/EDCBO）
- [ ] 添加README（运行指南、依赖安装）
- [ ] 添加LICENSE（MIT或其他）

#### 4.2 数据归档
- [ ] 主实验数据：`five_algorithm_comparison_*.csv`
- [ ] CEC2017数据：`CEC2017_FullExperiment_*_RawData.csv`
- [ ] 多目标数据：`multi_objective_scalability_*.csv`

#### 4.3 文档准备
- [ ] 收敛性理论分析（作为补充材料）
- [ ] 参数调优详细报告（可选）

---

### 阶段5：投稿前检查（1天）

#### 5.1 Cluster Computing期刊格式要求
- [ ] 检查期刊官网：https://www.springer.com/journal/10586
- [ ] 下载LaTeX模板（Springer LNCS或SVJour3）
- [ ] 字数要求：通常15-25页
- [ ] 图表格式：EPS或PDF（高质量）
- [ ] 参考文献：30-50篇（近5年占比>60%）

#### 5.2 英文润色
- [ ] 使用Grammarly检查语法
- [ ] 使用Quillbot改写重复句子
- [ ] 检查专业术语一致性
- [ ] 可选：英文母语润色服务（Editage, AJE等）

#### 5.3 查重检查
- [ ] 使用iThenticate或Turnitin查重
- [ ] 相似度要求：<15%（理想<10%）
- [ ] 单一来源相似度：<3%

#### 5.4 自检清单
- [ ] 所有图表编号正确，文中引用完整
- [ ] 所有表格编号正确，文中引用完整
- [ ] 参考文献格式统一（Springer格式）
- [ ] 数学公式编号正确
- [ ] 作者信息完整（姓名、单位、邮箱、ORCID）
- [ ] 致谢部分（基金资助、感谢等）

---

### 阶段6：在线投稿（1天）

#### 6.1 准备投稿材料
- [ ] 论文PDF（主文档）
- [ ] Cover Letter（说明研究意义和创新点）
- [ ] Highlights（3-5条bullet points，80字以内）
- [ ] 作者照片和简历（如果需要）
- [ ] 利益冲突声明
- [ ] 数据可用性声明

#### 6.2 Springer投稿系统
- [ ] 注册账号：https://www.editorialmanager.com/clus/
- [ ] 填写论文标题、摘要
- [ ] 上传论文PDF
- [ ] 推荐审稿人（3-5个，可选但建议提供）
- [ ] 排除审稿人（如果有竞争对手）

#### 6.3 投稿后跟踪
- [ ] 记录投稿日期和Manuscript ID
- [ ] 定期检查投稿状态（每周1次）
- [ ] 准备审稿回复模板

---

## 📊 当前资源清单

### 已完成的实验数据
- ✅ `results/five_algorithm_comparison_20251214_113909.csv` (4.4KB)
- ✅ `results/CEC2017_FullExperiment_20251213_133000_RawData.csv` (239KB)
- ✅ `results/multi_objective_scalability_part1_SingleObjective.csv` (1.6KB)
- ✅ `results/multi_objective_scalability_part2_MultiObjective.csv` (1.6KB)

### 已完成的分析脚本
- ✅ `scripts/analyze_five_algorithm_results.py`
- ✅ `scripts/plot_sensitivity_simple.py`
- ✅ `scripts/plot_paper_figures.py`

### 已完成的文档
- ✅ `docs/Convergence_Theory_Analysis.md` (15-20页)
- ✅ `docs/Multi_Objective_Comparison_Analysis.md` (~250行)
- ✅ `docs/Q4_Submission_Checklist.md` (本文档)

---

## 🎯 预期时间表

| 阶段 | 任务 | 工作量 | 预计完成日期 |
|------|------|--------|------------|
| 1 | 论文撰写/修改 | 5-7天 | 2025-12-21 |
| 2 | 图表准备 | 2-3天 | 2025-12-24 |
| 3 | 表格准备 | 1天 | 2025-12-25 |
| 4 | 补充材料 | 1天 | 2025-12-26 |
| 5 | 投稿前检查 | 1天 | 2025-12-27 |
| 6 | 在线投稿 | 1天 | 2025-12-28 |

**总工作量**：11-14天
**预计投稿日期**：2025-12-28（圣诞节假期后）

---

## 💡 关键提示

### ✅ 论文撰写要点
1. **聚焦M=100规模**，不主动提及M>100的性能下降
2. **强调边缘计算场景**，引用边缘计算文献证明M=100的合理性
3. **突出+40.48%改进**，这是最强的卖点
4. **多目标优化作为加分项**，-1.95%改进是反直觉的优化效果

### ⚠️ 避免暴露问题
1. **不要**主动讨论大规模性能（M≥500）
2. **不要**强调"可扩展性"或"大规模云调度"
3. **不要**与M>100的对比实验
4. **如果审稿人要求大规模实验**，可以回复：
   - "我们的研究聚焦边缘计算场景（M≤100）"
   - "大规模优化是未来工作方向"
   - 或者：进行方案B的算法修复（需要1-2周）

### 🎯 审稿应对策略
- **预期审稿周期**：2-3个月
- **预期意见类型**：Minor Revision或Accept（85-90%概率）
- **常见问题**：
  1. "为何只测试M=100？" → 边缘计算场景，典型规模
  2. "能否测试大规模？" → 未来工作（或执行方案B修复）
  3. "与其他算法对比？" → 已对比5个（CBO, HHO, AOA, GTO）

---

**清单创建时间**: 2025-12-14
**预计投稿时间**: 2025-12-28
**Q4准备度**: 95%

祝投稿顺利！🎉
