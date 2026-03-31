# Comparing TSP Approximation Algorithms on Real Highway Network Data

**Overview:** The Traveling Salesman Problem (TSP) asks for the shortest route visiting a set of locations exactly once before returning to the starting point. 
Despite the simple formulation, TSP is one of the most challenging classes of problems, so it is one of the most studied problems in algorithm design. 
This project will implement and compare multiple TSP algorithms, ranging from exact to approximation approaches, applied to real road network data from the METAL graph dataset.

**Algorithms to Investigate:** 
* Held-Karp - serves as a correctness baseline for small inputs 
* Nearest Neighbor - fast but can be suboptimal and is greedy
* 2-opt Improvement - Iteratively improves existing routes by swapping edge pairs
* Christofides Algorithm? - theoretically guarantees a solution within 1.5x optimal length

**Analysis Plan:** For each algorithm, we will measure total route distance, runtime, and how performance scales as problem size grows. We will compare approximation
ratios to the baseline from Held-Karp and potentially visualize results. 

**Schedule:** We plan on having an algorithm implemented and tested for each of the milestone updates as we go. 
In the end we will put the data together for comparisons and work on a clean format to display results. 

**Success Criteria:** In order to consider the project a success, the most important milestones are getting the Nearest Neighbor and 2-opt algorithms working with the 
METAL data. Christofides algorithm might be a stretch, but if time permits we will try it, and if not, we could do a theoretical comparison in the analysis. 
The project is still strong with the first three algorithms used in an empirical study.

**Feasibility:** The project will be developed in Java within the timeframe, using the publicly accessible METAL data. No additional software or hardware is necessary. 
