import numpy as np
import copy


class NodeClass:
    w = 1.3
    status = ''

    def __init__(self, content, predecessor, goal):
        self.content = content
        self.f = 0  # admissible
        self.fp = 0  # non-admissible
        self.g = 0  # cost function
        self.h = 0  # heuristic function
        self.predecessor = predecessor
        self.size = self.content.shape[0]
        self.goal = goal

    def is_goal(self):
        return np.array_equal(self.content, self.goal)

    def calculate_h(self):
        """
        :return: manhattan distance to goal
        """
        h_value = 0
        for number in range(self.size**2):
            goal_position = np.argwhere(self.goal == number + 1)
            current_position = np.argwhere(self.content == number + 1)
            h_value += np.sum(np.abs(goal_position - current_position))
        self.h = h_value

    def calculate_g(self):
        # g value
        parent = self.predecessor
        self.g = parent.g + 1

    def calculate_f(self):
        # f value
        self.fp = self.g + self.w * self.h
        self.f = self.g + self.h

    def calculate_costs(self):
        self.calculate_h()
        self.calculate_g()
        self.calculate_f()

    def __str__(self):
        output = ''
        output += 'fp(%0.2f) = g(%0.2f) + %0.1f*h(%0.2f)\n' % (self.fp, self.g, self.w, self.h)
        output += 'f (%0.2f) = g(%0.2f) + h(%0.2f)\n' % (self.f, self.g, self.h)
        for i in range(self.size):
            for j in range(self.size):
                if self.content[i, j] == self.size ** 2:
                    output += '%2s' % 'b'
                else:
                    output += '%2d' % self.content[i, j]
                output += ' '
            output += '\n'
        return output

    def __eq__(self, other):
        if np.array_equal(self.content, other.content):
            return True
        else:
            return False

    def generate_successor(self):
        successors_list = []

        content = self.content
        i, j = np.where(content == self.size ** 2)
        i = i[0]
        j = j[0]

        # Move blank up
        if i != 0:
            temp = copy.deepcopy(content)
            temp_value = temp[i - 1][j]
            temp[i - 1][j] = self.size ** 2
            temp[i][j] = temp_value
            successors_list.append(NodeClass(temp, self, self.goal))

        # Move blank down
        if i != (content.shape[0] - 1):
            temp = copy.deepcopy(content)
            temp_value = temp[i + 1][j]
            temp[i + 1][j] = self.size ** 2
            temp[i][j] = temp_value
            successors_list.append(NodeClass(temp, self, self.goal))

        # Move blank left
        if j != 0:
            temp = copy.deepcopy(content)
            temp_value = temp[i][j - 1]
            temp[i][j - 1] = self.size ** 2
            temp[i][j] = temp_value
            successors_list.append(NodeClass(temp, self, self.goal))

        # Move blank right
        if j != (content.shape[1] - 1):
            temp = copy.deepcopy(content)
            temp_value = temp[i][j + 1]
            temp[i][j + 1] = self.size ** 2
            temp[i][j] = temp_value
            successors_list.append(NodeClass(temp, self, self.goal))

        return successors_list

    def build_path(self):
        path = [self]
        current = self
        while current.predecessor.status == '':
            path.append(current.predecessor)
            current = current.predecessor
        path.append(current.predecessor)
        return path
