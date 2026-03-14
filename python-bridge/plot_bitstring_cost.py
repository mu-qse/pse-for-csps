import matplotlib.pyplot as plt
import numpy as np
import os
import pandas as pd
import sys

if __name__ == "__main__":
    csvFile = sys.argv[1]
    svgFile = sys.argv[2]
    title = sys.argv[3]

    df = pd.read_csv(csvFile, sep = ",")
    df["index"] = df.index
    df_min = df[df["cost"]==df["cost"].min()]
    df_max = df[df["cost"]==df["cost"].max()]

    fig, ax = plt.subplots(dpi = 150)
    ax.plot(df["index"], df["cost"], color = "grey", linewidth = 0.2)
    #ax.stairs(df["cost"], color = "grey", linewidth = 0.2)
    for row in df_min.iloc:
        ax.axvline(row["index"], color = "black", linewidth = 1, linestyle = "--", label = "Iteration: {:d}, Cost: {:.3g}".format(int(row["index"]), row["cost"]))

    ax.legend()

    plt.autoscale(tight = True)
    ax.set_xlabel("Iteration")
    ax.tick_params("x", rotation = 90)

    #if len(df) % 16 == 0:
    #    ax.set_xticks(np.linspace(0, len(df) - 1, 8 + 1))
    #elif len(df) % 10 == 0:
    #    ax.set_xticks(np.linspace(0, len(df) - 1, 10 + 1))

    ax.set_ylabel("Cost")
    #ax.set_yscale("log")
    #ax.autoscale(enable = True, axis = "y")
    ax.set_ybound(df_min.iloc[0]["cost"], df_max.iloc[0]["cost"])

    plt.title(title)
    plt.tight_layout()

    if "SHOW_PLOT" in os.environ:
        plt.show()
    else:
        plt.savefig(svgFile, bbox_inches = "tight")

    print("Plot written to: {}".format(svgFile))
