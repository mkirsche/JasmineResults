'''
Makes stacked bar plots looking at merged structural variants.

The SVs are partitioned in a number of ways, such as by type, length, etc.
For each partition, a separate stacked bar plot is made as follows:
  The x-axis indicates a subset of the samples (based on the support vector of the variants)
  For each support vector, a bar is made which is colored by the values along the plot's specific partition.
  For example, a bar could be made for "Sample 1 only" which is then colored by the variants which are insertions, deletions, etc.
  
The input for this must be in a very specific format (currently parsed from a VCF by src/PairResults.java for a pair of sample):

<number of different support vectors>
for each support vector:
  <name of support vector - e.g., First Sample Only>
  <number of variants with that support vector>
  <number of partitions>
    for each partition:
      <name of partition>
      <number of values of that partition> followed by <value count> for each value, all space-separated 
'''
import matplotlib
matplotlib.use('Agg')
import matplotlib.patches as mpatches
import matplotlib.pyplot as plt
import seaborn as sns
import sys
import numpy as np

fn = sys.argv[1]

# Let the user specify an output file prefix as a command line argument, but default to none
ofn = ''
if(len(sys.argv) > 2):
  ofn = sys.argv[2] + '_'

data = {} # Map structure will be (Partition category e.g. SVTYPE, Support vector, Partition value e.g. INS, count)
plotOrder = {} # Similar map but without support vectors: {Partition category, index, partition value}
seenValues = {} # Used to speed up the plotOrder data structure accesses: {Partition category, partition value, 1 if present}
suppVecs = [] # List of all support vectors

# Read the input and store it in a buch of different ways described above
with open(fn) as f:
  n = int(f.readline())
  print(n)
  for i in range(0, n):
    suppVec = f.readline().rstrip()
    suppVecs.append(suppVec)
    size = int(f.readline())
    numCategories = int(f.readline())
    for j in range(0, numCategories):
      list = []
      name = f.readline().rstrip()
      if not name in data:
        data[name] = {}
        plotOrder[name] = {}
        seenValues[name] = {}
      tokens = f.readline().split(' ')
      numValues = int(tokens[0])
      innerMap = {}
      for k in range(0, numValues):
        x = tokens[2*k+1]
        if not x in seenValues[name]:
          plotOrder[name][len(plotOrder[name])] = x
          seenValues[name][x] = len(seenValues[name])
        y = int(tokens[2*k+2])
        innerMap[x] = y;
      data[name][suppVec] = innerMap
print(plotOrder)
print(seenValues)

# Create the stacked bar plots
for partitionCategory, value in data.items():
  sns.set() 
  bars = []
  colorNames = [plotOrder[partitionCategory][i] for i in range(0, len(plotOrder[partitionCategory]))]
  barNames = []
  for j in range(0, len(suppVecs)):
    suppVec = suppVecs[j]
    freqMap = value[suppVec]
    stack = [0 for i in range(0, len(plotOrder[partitionCategory]))]
    for partitionValue, count in freqMap.items():
      stack[seenValues[partitionCategory][partitionValue]] = count
    bars.append(stack)
    barNames.append(suppVec)
    
  plotTotals = [0 for i in range(0, len(bars))]
  for i in range(0, len(colorNames)):
    print([bars[j][i] for j in range(0, len(bars))])
    dataToPlot = [bars[j][i] for j in range(0, len(bars))]
    plt.bar(np.arange(len(bars)), dataToPlot, bottom=plotTotals, width = 0.25, edgecolor="none", color = sns.color_palette()[i])
    for j in range(0, len(bars)):
      plotTotals[j] += dataToPlot[j]
  patches = [mpatches.Patch(color=sns.color_palette()[i], label=plotOrder[partitionCategory][i]) for i in range(0, len(plotOrder[partitionCategory]))]
  plt.legend(handles=patches)
  plt.xticks(np.arange(len(bars)), suppVecs)
  plt.title('Sample Specific SVs by ' + partitionCategory)
  
  # Output plot to file with name based on partition category
  fn = ofn + partitionCategory.replace(' ', '').lower() + '.png'
  plt.savefig(fn)
      
print(data)
