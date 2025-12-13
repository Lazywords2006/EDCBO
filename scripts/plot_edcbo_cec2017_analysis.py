#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
LSCBO CEC2017完整实验结果可视化分析
生成发表级图表（300 DPI）

生成图表：
1. 算法获胜次数柱状图
2. LSCBO-Fixed在30个函数上的排名热力图
3. LSCBO-Fixed vs 最优算法对比（获胜的2个函数）
4. 函数类型性能雷达图
5. CloudSim + CEC2017双数据集对比

作者：LSCBO Research Team
日期：2025-12-13
"""

import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import seaborn as sns
from matplotlib import rcParams

# 设置中文字体和绘图风格
rcParams['font.sans-serif'] = ['SimHei', 'Arial Unicode MS', 'DejaVu Sans']
rcParams['axes.unicode_minus'] = False
rcParams['figure.dpi'] = 300
rcParams['savefig.dpi'] = 300
plt.style.use('seaborn-v0_8-darkgrid')

# 创建输出目录
import os
output_dir = '../paper_figures/edcbo_cec2017'
os.makedirs(output_dir, exist_ok=True)

# ==================== 数据准备 ====================

# 算法获胜次数
algorithm_wins = {
    'WOA': 8,
    'CBO': 8,
    'ICBO-Enhanced': 6,
    'GWO': 3,
    'PSO': 2,
    'LSCBO-Fixed': 2,
    'ICBO': 1,
    'Random': 0
}

# LSCBO-Fixed在30个函数上的性能排名（手工整理）
function_rankings = {
    'Sphere': 7,  # 接近完美但不如CBO/ICBO-E
    'Sum Squares': 7,
    'Zakharov': 6,
    'Powell': 6,
    'Quartic': 6,
    'Rosenbrock': 7,
    'Rastrigin': 4,
    'Griewank': 5,
    'Ackley': 8,  # 局部最优失败
    'Schwefel': 2,  # 优秀
    'Six-Hump Camel': 7,
    'Levy': 8,  # 失败
    'Dixon-Price': 1,  # 夺冠
    'Michalewicz': 2,  # 亚军
    'Styblinski-Tang': 2,  # 优秀
    'Alpine': 6,
    'Salomon': 8,  # 失败
    'Xin-She Yang': 3,
    'HappyCat': 1,  # 夺冠
    'Periodic': 5,
    'Expanded Schaffer F6': 6,
    'Weierstrass': 7,
    'Pathological': 5,
    'Exponential': 7,
    'Step': 8,  # 失败
    'Bent Cigar': 7,
    'Discus': 6,
    'High Conditioned Elliptic': 8,  # 失败
    'Hybrid Function 1': 8,  # 失败
    'Hybrid Function 2': 6
}

# LSCBO-Fixed夺冠的2个函数详细数据
winning_functions = {
    'Dixon-Price': {
        'LSCBO-Fixed': 0.5112,
        'GWO': 0.6667,
        'WOA': 0.6668,
        'CBO': 0.9928,
        'PSO': 40.97
    },
    'HappyCat': {
        'LSCBO-Fixed': 0.3704,
        'PSO': 0.4814,
        'WOA': 0.5168,
        'GWO': 0.6942,
        'ICBO-Enhanced': 1.2119
    }
}

# 函数类型分类
function_types = {
    '单峰': ['Sphere', 'Sum Squares', 'Zakharov', 'Powell', 'Quartic'],
    '简单多峰': ['Rastrigin', 'Griewank', 'Ackley'],
    '复杂多峰': ['Schwefel', 'Levy', 'Salomon', 'Michalewicz', 'Styblinski-Tang'],
    '混合函数': ['Hybrid Function 1', 'Hybrid Function 2'],
    '特殊函数': ['Dixon-Price', 'HappyCat', 'Periodic', 'Weierstrass', 'Step']
}

# ==================== 图1：算法获胜次数柱状图 ====================

def plot_algorithm_wins():
    fig, ax = plt.subplots(figsize=(10, 6))

    algorithms = list(algorithm_wins.keys())
    wins = list(algorithm_wins.values())
    colors = ['#1f77b4', '#ff7f0e', '#2ca02c', '#d62728', '#9467bd', '#e74c3c', '#8c564b', '#7f7f7f']

    bars = ax.bar(algorithms, wins, color=colors, alpha=0.8, edgecolor='black', linewidth=1.2)

    # 高亮LSCBO-Fixed
    bars[5].set_color('#e74c3c')
    bars[5].set_alpha(1.0)
    bars[5].set_edgecolor('red')
    bars[5].set_linewidth(2.5)

    # 添加数值标签
    for i, (bar, win) in enumerate(zip(bars, wins)):
        height = bar.get_height()
        ax.text(bar.get_x() + bar.get_width()/2., height,
                f'{win}',
                ha='center', va='bottom', fontsize=12, fontweight='bold')

    ax.set_xlabel('Algorithm', fontsize=14, fontweight='bold')
    ax.set_ylabel('Number of Wins (out of 30 functions)', fontsize=14, fontweight='bold')
    ax.set_title('CEC2017 Benchmark: Algorithm Performance Ranking\n(8 Algorithms × 30 Functions × 30 Runs)',
                 fontsize=16, fontweight='bold', pad=20)
    ax.set_ylim(0, 10)
    ax.grid(axis='y', alpha=0.3, linestyle='--')
    ax.set_axisbelow(True)

    # 添加说明
    ax.text(0.98, 0.97, 'LSCBO-Fixed: 2/30 wins (6.7%)\nRanked 4th (tied with PSO)',
            transform=ax.transAxes, fontsize=11,
            verticalalignment='top', horizontalalignment='right',
            bbox=dict(boxstyle='round', facecolor='wheat', alpha=0.5))

    plt.xticks(rotation=15, ha='right')
    plt.tight_layout()
    plt.savefig(f'{output_dir}/fig1_algorithm_wins.png', dpi=300, bbox_inches='tight')
    print(f'[OK] 图1已保存: {output_dir}/fig1_algorithm_wins.png')
    plt.close()

# ==================== 图2：LSCBO-Fixed排名热力图 ====================

def plot_ranking_heatmap():
    # 按函数类型组织数据
    sorted_functions = []
    for category, funcs in function_types.items():
        sorted_functions.extend(funcs)

    # 创建排名矩阵
    rankings = [function_rankings[f] for f in sorted_functions if f in function_rankings]
    sorted_functions = [f for f in sorted_functions if f in function_rankings]

    fig, ax = plt.subplots(figsize=(3, 12))

    # 创建热力图数据
    data = np.array(rankings).reshape(-1, 1)

    # 使用反转的颜色映射（1=绿色最优，8=红色最差）
    cmap = sns.color_palette("RdYlGn_r", as_cmap=True)
    im = ax.imshow(data, cmap=cmap, aspect='auto', vmin=1, vmax=8)

    # 设置刻度
    ax.set_yticks(np.arange(len(sorted_functions)))
    ax.set_yticklabels(sorted_functions, fontsize=9)
    ax.set_xticks([0])
    ax.set_xticklabels(['LSCBO-Fixed\nRanking'], fontsize=11, fontweight='bold')

    # 添加排名数值
    for i, rank in enumerate(rankings):
        color = 'white' if rank >= 5 else 'black'
        text = ax.text(0, i, f'{rank}', ha="center", va="center",
                      color=color, fontsize=10, fontweight='bold')

    # 添加颜色条
    cbar = plt.colorbar(im, ax=ax, orientation='horizontal', pad=0.05, aspect=30)
    cbar.set_label('Ranking (1=Best, 8=Worst)', fontsize=10, fontweight='bold')
    cbar.set_ticks([1, 2, 3, 4, 5, 6, 7, 8])

    ax.set_title('LSCBO-Fixed Performance Ranking\nAcross 30 CEC2017 Functions',
                 fontsize=13, fontweight='bold', pad=15)

    # 添加分类分隔线
    y_pos = 0
    for category, funcs in function_types.items():
        valid_funcs = [f for f in funcs if f in function_rankings]
        if y_pos > 0:
            ax.axhline(y_pos - 0.5, color='black', linewidth=2)
        y_pos += len(valid_funcs)

    plt.tight_layout()
    plt.savefig(f'{output_dir}/fig2_ranking_heatmap.png', dpi=300, bbox_inches='tight')
    print(f'[OK] 图2已保存: {output_dir}/fig2_ranking_heatmap.png')
    plt.close()

# ==================== 图3：LSCBO-Fixed获胜函数对比 ====================

def plot_winning_functions():
    fig, axes = plt.subplots(1, 2, figsize=(14, 6))

    for idx, (func_name, data) in enumerate(winning_functions.items()):
        ax = axes[idx]
        algorithms = list(data.keys())
        values = list(data.values())

        # 设置颜色（LSCBO-Fixed用红色高亮）
        colors = ['#e74c3c' if alg == 'LSCBO-Fixed' else '#3498db' for alg in algorithms]

        bars = ax.bar(algorithms, values, color=colors, alpha=0.8, edgecolor='black', linewidth=1.2)

        # LSCBO-Fixed加粗边框
        bars[0].set_edgecolor('red')
        bars[0].set_linewidth(2.5)

        # 添加数值标签
        for bar, val in zip(bars, values):
            height = bar.get_height()
            ax.text(bar.get_x() + bar.get_width()/2., height,
                    f'{val:.4f}' if val < 10 else f'{val:.2f}',
                    ha='center', va='bottom', fontsize=10, fontweight='bold')

        ax.set_ylabel('Fitness Value (Lower is Better)', fontsize=12, fontweight='bold')
        ax.set_title(f'{func_name} Function\n(LSCBO-Fixed WINS 🏆)',
                     fontsize=13, fontweight='bold', color='red')
        ax.grid(axis='y', alpha=0.3, linestyle='--')
        ax.set_axisbelow(True)
        plt.setp(ax.xaxis.get_majorticklabels(), rotation=20, ha='right')

    plt.suptitle('LSCBO-Fixed Winning Functions: Detailed Comparison',
                 fontsize=16, fontweight='bold', y=1.02)
    plt.tight_layout()
    plt.savefig(f'{output_dir}/fig3_winning_functions.png', dpi=300, bbox_inches='tight')
    print(f'[OK] 图3已保存: {output_dir}/fig3_winning_functions.png')
    plt.close()

# ==================== 图4：函数类型性能雷达图 ====================

def plot_function_type_radar():
    # 计算每种类型的平均排名
    type_avg_ranking = {}
    for category, funcs in function_types.items():
        valid_rankings = [function_rankings[f] for f in funcs if f in function_rankings]
        type_avg_ranking[category] = np.mean(valid_rankings) if valid_rankings else 8

    categories = list(type_avg_ranking.keys())
    values = list(type_avg_ranking.values())

    # 雷达图需要闭合
    values += values[:1]
    categories += categories[:1]

    fig, ax = plt.subplots(figsize=(8, 8), subplot_kw=dict(projection='polar'))

    angles = np.linspace(0, 2 * np.pi, len(function_types), endpoint=False).tolist()
    angles += angles[:1]

    ax.plot(angles, values, 'o-', linewidth=2, color='#e74c3c', label='LSCBO-Fixed')
    ax.fill(angles, values, alpha=0.25, color='#e74c3c')

    ax.set_xticks(angles[:-1])
    ax.set_xticklabels(list(function_types.keys()), fontsize=11, fontweight='bold')
    ax.set_ylim(0, 8)
    ax.set_yticks([1, 2, 3, 4, 5, 6, 7, 8])
    ax.set_yticklabels(['1\n(Best)', '2', '3', '4', '5', '6', '7', '8\n(Worst)'], fontsize=9)
    ax.grid(True, linestyle='--', alpha=0.5)

    # 反转y轴方向（1在外，8在内，因为1是最优）
    ax.set_theta_offset(np.pi / 2)
    ax.set_theta_direction(-1)

    ax.set_title('LSCBO-Fixed Performance by Function Type\n(Lower is Better)',
                 fontsize=14, fontweight='bold', pad=30)

    # 添加图例说明
    legend_text = '\n'.join([f'{cat}: {val:.2f}' for cat, val in type_avg_ranking.items()])
    ax.text(1.35, 0.5, f'Average Rankings:\n{legend_text}',
            transform=ax.transAxes, fontsize=10,
            verticalalignment='center',
            bbox=dict(boxstyle='round', facecolor='wheat', alpha=0.5))

    plt.tight_layout()
    plt.savefig(f'{output_dir}/fig4_function_type_radar.png', dpi=300, bbox_inches='tight')
    print(f'[OK] 图4已保存: {output_dir}/fig4_function_type_radar.png')
    plt.close()

# ==================== 图5：CloudSim + CEC2017双数据集对比 ====================

def plot_dual_dataset_comparison():
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 6))

    # 子图1：CloudSim任务调度结果
    algorithms_cs = ['CBO\n(Baseline)', 'LSCBO\n(Old)', 'LSCBO-Fixed\n(New)']
    makespans = [925.64, 961.13, 718.14]
    colors_cs = ['#3498db', '#e67e22', '#e74c3c']

    bars1 = ax1.bar(algorithms_cs, makespans, color=colors_cs, alpha=0.8,
                    edgecolor='black', linewidth=1.2)
    bars1[2].set_edgecolor('red')
    bars1[2].set_linewidth(2.5)

    # 添加数值和改进率标签
    for i, (bar, val) in enumerate(zip(bars1, makespans)):
        height = bar.get_height()
        ax1.text(bar.get_x() + bar.get_width()/2., height,
                f'{val:.2f}s',
                ha='center', va='bottom', fontsize=11, fontweight='bold')
        if i == 2:
            improvement = ((925.64 - val) / 925.64) * 100
            ax1.text(bar.get_x() + bar.get_width()/2., height/2,
                    f'+{improvement:.2f}%\nImprovement',
                    ha='center', va='center', fontsize=10, fontweight='bold',
                    color='white',
                    bbox=dict(boxstyle='round', facecolor='red', alpha=0.8))

    ax1.set_ylabel('Makespan (seconds)', fontsize=12, fontweight='bold')
    ax1.set_title('Dataset 1: CloudSim Task Scheduling\n(M=100 tasks, N=20 VMs, Seed=42)',
                  fontsize=12, fontweight='bold')
    ax1.set_ylim(0, 1100)
    ax1.grid(axis='y', alpha=0.3, linestyle='--')
    ax1.set_axisbelow(True)

    # 子图2：CEC2017基准测试结果
    metrics = ['Wins\n(out of 30)', 'Top 3\nFinishes', 'Avg Rank\n(1-8)']
    edcbo_values = [2, 6, 5.5]  # 获胜2次，Top3有6次，平均排名约5.5

    # 使用不同颜色表示不同指标
    colors_cec = ['#e74c3c', '#f39c12', '#3498db']
    bars2 = ax2.bar(metrics, edcbo_values, color=colors_cec, alpha=0.8,
                    edgecolor='black', linewidth=1.2)

    # 添加数值标签
    labels = ['2/30\n(6.7%)', '6/30\n(20%)', '~5.5']
    for bar, val, label in zip(bars2, edcbo_values, labels):
        height = bar.get_height()
        ax2.text(bar.get_x() + bar.get_width()/2., height,
                label,
                ha='center', va='bottom', fontsize=11, fontweight='bold')

    ax2.set_ylabel('Score', fontsize=12, fontweight='bold')
    ax2.set_title('Dataset 2: CEC2017 Benchmark Functions\n(30 Functions × 30 Runs, D=30)',
                  fontsize=12, fontweight='bold')
    ax2.set_ylim(0, 8)
    ax2.grid(axis='y', alpha=0.3, linestyle='--')
    ax2.set_axisbelow(True)

    plt.suptitle('LSCBO-Fixed: Dual Dataset Validation Results',
                 fontsize=16, fontweight='bold', y=0.98)
    plt.tight_layout()
    plt.savefig(f'{output_dir}/fig5_dual_dataset_comparison.png', dpi=300, bbox_inches='tight')
    print(f'[OK] 图5已保存: {output_dir}/fig5_dual_dataset_comparison.png')
    plt.close()

# ==================== 图6：LSCBO-Fixed失败案例分析 ====================

def plot_failure_analysis():
    # 失败的5个函数（排名8）
    failures = {
        'Ackley': {
            'LSCBO-Fixed': 18.22,
            'CBO': 2.34e-15,
            'Issue': 'Local Optimum Trap'
        },
        'Salomon': {
            'LSCBO-Fixed': 17.82,
            'ICBO-Enhanced': 3.95e-109,
            'Issue': 'Insufficient Exploration'
        },
        'Levy': {
            'LSCBO-Fixed': 43.06,
            'WOA': 0.22,
            'Issue': 'Exploration-Exploitation Imbalance'
        },
        'Step': {
            'LSCBO-Fixed': 1799,
            'CBO': 0.0,
            'Issue': 'Discrete Optimization Failure'
        },
        'High Conditioned Elliptic': {
            'LSCBO-Fixed': 11210,
            'CBO': 0.0,
            'Issue': 'Numerical Instability'
        }
    }

    fig, ax = plt.subplots(figsize=(12, 7))

    x = np.arange(len(failures))
    width = 0.35

    edcbo_values = [data['LSCBO-Fixed'] for data in failures.values()]
    best_algo_names = [list(data.keys())[1] for data in failures.values()]
    best_values = [list(data.values())[1] for data in failures.values()]

    bars1 = ax.bar(x - width/2, edcbo_values, width, label='LSCBO-Fixed (Failed)',
                   color='#e74c3c', alpha=0.8, edgecolor='black', linewidth=1.2)
    bars2 = ax.bar(x + width/2, best_values, width, label='Best Algorithm',
                   color='#2ecc71', alpha=0.8, edgecolor='black', linewidth=1.2)

    ax.set_ylabel('Fitness Value (Log Scale)', fontsize=12, fontweight='bold')
    ax.set_title('LSCBO-Fixed Failure Cases: Root Cause Analysis\n(5 Functions with Ranking = 8)',
                 fontsize=14, fontweight='bold', pad=20)
    ax.set_xticks(x)
    ax.set_xticklabels(failures.keys(), rotation=20, ha='right', fontsize=10)
    ax.legend(fontsize=11, loc='upper left')
    ax.set_yscale('log')
    ax.grid(axis='y', alpha=0.3, linestyle='--')
    ax.set_axisbelow(True)

    # 添加根本原因标签
    for i, (func, data) in enumerate(failures.items()):
        ax.text(i, max(edcbo_values[i], best_values[i]) * 1.5,
                data['Issue'],
                ha='center', va='bottom', fontsize=8, rotation=0,
                bbox=dict(boxstyle='round', facecolor='yellow', alpha=0.5))

    # 添加最优算法名称
    for i, name in enumerate(best_algo_names):
        ax.text(i + width/2, best_values[i],
                f'{name}',
                ha='center', va='bottom', fontsize=8, fontweight='bold')

    plt.tight_layout()
    plt.savefig(f'{output_dir}/fig6_failure_analysis.png', dpi=300, bbox_inches='tight')
    print(f'[OK] 图6已保存: {output_dir}/fig6_failure_analysis.png')
    plt.close()

# ==================== 主函数 ====================

def main():
    print('\n' + '='*60)
    print('开始生成LSCBO CEC2017完整实验可视化分析图表')
    print('='*60 + '\n')

    print('生成图表中...')
    plot_algorithm_wins()
    plot_ranking_heatmap()
    plot_winning_functions()
    plot_function_type_radar()
    plot_dual_dataset_comparison()
    plot_failure_analysis()

    print('\n' + '='*60)
    print('[OK] 所有图表生成完成！')
    print(f'输出目录: {output_dir}')
    print('='*60)
    print('\n生成的图表清单：')
    print('  1. fig1_algorithm_wins.png - 算法获胜次数柱状图')
    print('  2. fig2_ranking_heatmap.png - LSCBO-Fixed排名热力图')
    print('  3. fig3_winning_functions.png - 获胜函数详细对比')
    print('  4. fig4_function_type_radar.png - 函数类型性能雷达图')
    print('  5. fig5_dual_dataset_comparison.png - 双数据集对比')
    print('  6. fig6_failure_analysis.png - 失败案例根因分析')
    print('='*60 + '\n')

if __name__ == '__main__':
    main()
