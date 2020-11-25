import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np


class Plotter:

    def __init__(self, num_targets: int, num_colors: int, num_rows: int, num_cols: int,
                 sharex: str, sharey: str):
        self.figure, self.axes = plt.subplots(nrows=num_rows, ncols=num_cols, sharex=sharex, sharey=sharey)
        self.num_rows = num_rows
        self.num_cols = num_cols
        self.num_targets = num_targets
        self.palette = sns.cubehelix_palette(n_colors=num_colors, start=.5, rot=-.75)

    def add_to_subplot(self, x, y, ax_index: int, color_index: int, marker="o"):
        if self.num_rows > 1 and self.num_cols == 1:
            ax = self.axes[ax_index]
        elif self.num_cols > 1 and self.num_rows == 1:
            ax = self.axes[ax_index]
        elif self.num_rows == 1 and self.num_cols == 1:
            ax = self.axes
        else:
            row = int(ax_index / self.num_cols)
            col = ax_index % row
            ax = self.axes[row, col]
        ax.plot(x, y, marker=marker, c=self.palette[color_index], alpha=0.9)

    def add_legend(self, labels):
        if self.num_rows == 1 and self.num_cols == 1:
            ax = self.axes
        elif len(self.axes.shape) == 1:
            ax = self.axes[-1]
        else:
            ax = self.axes[-1][-1]
        lines = [plt.Line2D([], [], color=self.palette[i]) for i in range(len(labels))]
        ax.legend(handles=lines, labels=labels)

    def set_title(self, for_axis: int, title: str):
        if self.num_rows > 1 and self.num_cols == 1:
            ax = self.axes[for_axis]
        elif self.num_cols > 1 and self.num_rows == 1:
            ax = self.axes[for_axis]
        elif self.num_rows == 1 and self.num_cols == 1:
            ax = self.axes
        else:
            row = int(for_axis / self.num_cols)
            col = for_axis % row
            ax = self.axes[row, col]
        ax.set_title(title)

    def set_ax_labels(self, labels: (str, str)):
        for ax in np.array([self.axes]).flatten():
            ax.set_ylabel(labels[1])
            ax.set_xlabel(labels[0])

    def save(self, fp):
        plt.tight_layout()
        plt.savefig(fp)
        # plt.clf()

