import os
import pandas as pd

from src.cholesky_correlation import add_random_correlation
from src.data_shapes import normal_data


if __name__ == '__main__':
    data = normal_data(20, 1000)
    data = add_random_correlation(data)
    header = ["{}".format(i) for i in range(data.shape[-1])]
    df = pd.DataFrame(data, index=None, columns=header)
    print(df.corr())
    df.to_csv(path_or_buf=os.path.join("save", "data.csv"), sep=";")