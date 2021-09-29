'''
Created on Oct 10, 2018
@author: ss4350
'''

from typing import Union
import numpy as np
import copy
import time
import matplotlib.pyplot as plt

from src.utils.stopwatch import measure_duration
from src.pathfinding.node_class import NodeClass


def shuffle_board(board, n):
    temp = copy.deepcopy(board)
    for i in range(n):
        # random direction
        direction = np.random.choice(range(4), 1)
        a, b = np.where(temp == board.shape[0] ** 2)
        a = a[0]
        b = b[0]
        # Move blank up
        if direction == 0 and a != 0:
            temp_value = temp[a - 1][b]
            temp[a - 1][b] = temp.shape[0] ** 2
            temp[a][b] = temp_value

        # Move blank down
        if direction == 1 and a != board.shape[0] - 1:
            temp_value = temp[a + 1][b]
            temp[a + 1][b] = board.shape[0] ** 2
            temp[a][b] = temp_value

        # Move blank left
        if direction == 2 and b != 0:
            temp_value = temp[a][b - 1]
            temp[a][b - 1] = board.shape[0] ** 2
            temp[a][b] = temp_value

        # Move blank right
        if direction == 3 and b != board.shape[0] - 1:
            temp_value = temp[a][b + 1]
            temp[a][b + 1] = board.shape[0] ** 2
            temp[a][b] = temp_value

    return temp


def get_min(temp):
    min_index = np.argmin([item.fp for item in temp])
    min_index2 = min_index
    for i in range(len(temp)):
        if temp[i].fp == temp[min_index].fp:
            if temp[i].h < temp[min_index2].h:
                min_index2 = i
    return temp[min_index2]


@measure_duration
def iterate_successor(s: NodeClass, ub: Union[None, NodeClass]) -> NodeClass:
    # Check for better duplicates in open_list
    if s in open_list:
        k = open_list.index(s)
        if s.g < open_list[k].g:
            open_list.remove(s)
            open_list.append(s)

    # Check for better duplicates in closed_list
    elif s in closed_list:
        k = closed_list.index(s)
        if s.g < closed_list[k].g:
            closed_list.remove(s)
            open_list.append(s)
    else:
        open_list.append(s)
    track_performance(node)
    return ub


def track_performance(node: NodeClass):
    if len(open_list) > 0:
        min_cost = min([item.f for item in open_list])
        lower_bound_vals.append(min_cost)
        performance.append(min_cost / upper_bound.f if upper_bound is not None else float("nan"))
        upper_bound_vals.append(upper_bound.f if upper_bound is not None else float("nan"))


if __name__ == '__main__':

    # 8-Puzzle
    # goal = np.array([[1, 2, 3], [8, 9, 4], [7, 6, 5]])
    goal = np.array([[1, 2, 3], [4, 5, 6], [7, 8, 9]])
    # 15-Puzzle
    # goal = np.array([[1, 2, 3, 4], [5, 6, 7, 8], [9, 10, 11, 12], [13, 14, 15, 16]])

    # Shuffle
    startNode = shuffle_board(goal, 8000)
    # startNode = np.array([[5, 6, 7],
    #                       [4, 9, 8],
    #                       [3, 2, 1]])
    # startNode = np.array([[8, 6, 7],
    #                       [2, 5, 4],
    #                       [3, 9, 1]])

    startNode = NodeClass(startNode, 0, goal)
    startNode.status = 'Start'
    startNode.calculate_h()
    startNode.calculate_f()
    print('Start Node')
    print(startNode)

    open_list = [startNode]
    closed_list = []
    start_time = time.time()

    upper_bound: NodeClass = None
    iteration_times = []
    upper_bound_vals = []
    lower_bound_vals = []
    performance = []


    # for i in range(2):
    while len(open_list) != 0:
        # Find node with least f
        node = get_min(open_list)
        open_list.remove(node)
        # Add to closed list
        closed_list.append(node)
        if upper_bound is None or node.f < upper_bound.f:
            upper_bound = expand(node, upper_bound)

    path_to_start = upper_bound.build_path()
    print('Number of moves: %d' % upper_bound.g)
    print('path is:****************************************************')

    for item in reversed(path_to_start):
        print(item)
    print('Total time is: %5f' % (time.time() - start_time))

    fig, axes = plt.subplots(4, 1)
    axes[0].plot(range(len(iteration_times)), iteration_times)
    axes[1].plot(range(len(upper_bound_vals)), upper_bound_vals)
    axes[1].plot(range(len(lower_bound_vals)), lower_bound_vals)
    axes[2].plot(range(len(performance)), performance)
    axes[3].hist(iteration_times, bins=100)
    plt.show()
