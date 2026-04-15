# Progress Updates

## Progress as of 4/10/2026
We added the structure of our project, having a file for the algorithms, a main driver, and a file to load the highway data. We used the previous lab as a structure for load the .tmg files. 
We also added a few files a various sizes to run tests on for the different algorithms. Finally, we got the Nearest Neighbor algorithm working on subsets from the data by picking subsets of 
points from the data containing certain labels as a start. The plan remains the same and we plan on adding the other algorithms in the coming weeks and comparing results.

## Progress as of 4/17/2026
We completed an implementation of the 2-opt improvement, which will tak an existing route and swap edges if a more
efficient path is available. We also added a method to HighwayGraph to create a set of all vertices for smaller graphs 
where it can still run efficiently. Finally, we added a method that filters out vertices that are disconnected from the graph that still have Double.MAX_VALUE in the distance matrix. 
