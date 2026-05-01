library(shiny)

data <- read.csv("results-6.csv", header = FALSE)

colnames(data) <- c("ignore", "subset_size",
                    "nn_dist", "nn_time",
                    "twoopt_dist", "twoopt_time",
                    "i1", "i2")

data <- subset(data, nn_time > 0 & twoopt_time > 0)
data45 <- read.csv("results45.csv", header = TRUE)

data45 <- subset(data45,
                 nn_time > 0 &
                   twoopt_time > 0 &
                   heldkarp_time > 0)
ui <- fluidPage(
  
  h1("effectiveness of nn 2-opt for optimizing the travelling salesman problem"),
  
  br(),
  
  h3("Introduction"),
  p("This project was made to compare various algorithms, specifically nearest neighbor, 2 opt, and heldkarp, that try to optimize the travelling salesman problem, with special attention given to the combination of nearest neighbor for base route finding, and 2-opt for route optimization, with comparisons to held karp to determine efficiency/valid use cases of one over other, with regards to runtime/cost at differing sizes/shapes"),
  
  hr(),
  
  
  h3("What is the Travelling Salesman Problem?"),
  p("The travelling salesman problem is a mapping problem that deals with trying to find the shortest possible route on a map of points that crosses each non-starting point exactly once, and then returns to the starting point."),
  
  hr(),
  
  h3("Nearest Neighbor"),
  fluidRow(
    column(6,
           p("Simple algorithm that constructs a route by jumping from a starting point to the nearest point until it has passed through all points and returned to start."),
         
    ),
    column(6,
           
           
    )
  ),
  
  hr(),
  
  h3("2-opt"),
  fluidRow(
    column(6,
           p("Route optimization algorithm that works on an existing route (such as one made by nearest neighbor). It selects two non adjacent vertices and removes the path between them, replacing that path with another path and keeping the new route if it optimizes distance.."),
      
  
           selectInput("scale", "Scale",
                       choices = c("Linear", "Log-Log"),
                       selected = "Linear")
    ),
    column(6,
           
         
           plotOutput("scalingPlot", height = "400px")
    )
  ),
  
  hr(),
  
  h3("Held-Karp"),
  fluidRow(
    column(6,
           p("Guarnteed to find optimal path, but has a cost that makes it unfeasible for tsp problems with more than 24 or so as an actual realistic tsp optimizer, but is still useful beyond that as a way to guarntee an optimal path up until is 29 or more, where due to its exponentially scaling cost (O(n*2^n))  "),
           
           p("Starts by finding the distance of all paths between 2 points: the starting point and each possible next point (A→B, A→C, A→D, etc)."),
           
           p("It then builds up to subsets of 3 points, using previously computed results to evaluate paths like A→B→C, A→D→C, and so on."),
           
           p("This process continues, incrementally building solutions for larger subsets until all cities are included, and finally returns to the starting point.")
    ),
    
    column(6,
           plotOutput("heldPlot", height = "400px"),
        
    )
  ),
 h3(""),
  fluidRow(
    column(6,
            p("")
)
)

)
server <- function(input, output) {
  output$nn2optLogPlot <- renderPlot({
    
    
    
    # manually define data (or load from CSV if you prefer)
    
    df <- read.csv(text = "

highway,subset,nn_distance,nn_time,twoopt_distance,twoopt_time

siena2.5,4,6.081,0.006231,6.081,0.010359

union2,4,3.638,0.005851,3.638,0.006823

rpi2,5,5.820,0.006232,5.820,0.008236

amsterdam2,5,9.346,0.006482,8.128,0.012043

whitman5,9,17.378,0.007304,17.378,0.016591

CUW-region,11,9.249,0.009718,9.249,0.013635

ESP-CE-region,14,16.109,0.012594,15.814,0.038001

BRA-AM-region,16,316.022,0.013996,316.022,0.051546

BLM-region,17,13.204,0.016731,13.204,0.065713

rpi5-area,18,37.145,0.016942,27.681,0.077725

amsterdam5-area,23,59.344,0.046557,55.315,0.116698

BRA-AP-region,27,927.171,0.031147,920.810,0.140142

ABW-region,29,45.257,0.035687,41.681,0.237163

CHN-LN-region,46,940.575,0.018515,939.741,0.042028

BRA-AL-region,50,376.791,0.091711,292.881,0.854826

CHN-HN-region,50,1570.018,0.094607,1490.582,0.049011

MEX-MOR-region,50,221.414,0.084228,200.705,0.724917

MEX-NAY-region,50,513.902,0.099315,382.249,0.084147

TKM-region,50,2678.877,0.090519,2623.904,0.343962

CHN-LN-region,96,1495.306,0.088355,1405.329,0.196497

BRA-AL-region,100,879.292,0.508109,787.422,2.258828

CHN-HN-region,100,2503.166,0.330216,2393.261,0.192239

MEX-MOR-region,100,378.648,0.342840,343.307,0.271637

MEX-NAY-region,100,501.024,0.981273,493.025,0.162744

TKM-region,100,2921.534,0.347198,2861.669,1.852829

BRA-AL-region,105,938.061,0.401670,846.191,2.445286

MEX-MOR-region,144,593.988,2.730288,459.545,11.625548

TKM-region,183,3629.225,1.059459,3537.571,7.498953

CHN-HN-region,200,4244.047,0.945426,3359.157,0.981252

MEX-NAY-region,200,1195.333,0.303267,904.103,1.799660

MEX-NAY-region,237,1358.473,6.353554,1099.739,12.424189

CHN-LN-region,285,3308.783,3.863875,2991.928,21.678780

", header = TRUE)
    
    # extract
    
    x_nn <- df$nn_time
    
    x_2opt <- df$twoopt_time
    
    y <- df$subset
    
    
    
    ord <- order(y)
    
    
    
    xmin <- min(c(x_nn, x_2opt), na.rm = TRUE)
    
    xmax <- max(c(x_nn, x_2opt), na.rm = TRUE)
    
    ymin <- min(y)
    
    ymax <- max(y)
    
    
    
    par(mar = c(5,5,4,10), xpd = TRUE)
    
    options(scipen = 999)
    
    
    
    plot(x_nn, y,
         
         log = "xy",
         
         xlim = c(xmin, xmax),
         
         ylim = c(ymin, ymax),
         
         pch = 16,
         
         col = "black",
         
         xlab = "Time (ms)",
         
         ylab = "Subset Size",
         
         main = "NN vs 2-opt Runtime Scaling (Log-Log)")
    
    
    
    points(x_2opt, y,
           
           col = "gold", pch = 16)
    
    
    
    lines(x_nn[ord], y[ord],
          
          col = "black", lwd = 2)
    
    
    
    lines(x_2opt[ord], y[ord],
          
          col = "gold", lwd = 2)
    
    
    
    legend("topright",
           
           inset = c(-0.25, 0),
           
           legend = c("Nearest Neighbor", "2-opt"),
           
           col = c("black", "gold"),
           
           lty = 1,
           
           pch = 16)
    
  })

  output$heldPlot <- renderPlot({
    
    x_nn <- data45$nn_time
    x_2opt <- data45$twoopt_time
    x_hk <- data45$heldkarp_time
    y <- data45$subset
    
    ord <- order(y)
    
    xmin <- min(c(x_nn, x_2opt, x_hk), na.rm = TRUE)
    xmax <- max(c(x_nn, x_2opt, x_hk), na.rm = TRUE)
    par(mar = c(5, 5, 4, 10), xpd = TRUE)
    options(scipen = 999)
    plot(x_nn, y,
         log = "x",
         xlim = c(xmin, xmax),
         pch = 16,
         col = "black",
         xlab = "Log time (ms)",
         ylab = "Subset Size",
         main = "Runtime on differing subsets")
    points(x_2opt, y,
           col = "gold", pch = 16)
    
    points(x_hk, y,
           col = "purple", pch = 16)
    
    lines(x_nn[ord], y[ord],
          col = "black", lwd = 2)
    
    lines(x_2opt[ord], y[ord],
          col = "gold", lwd = 2)
    
    lines(x_hk[ord], y[ord],
          col = "purple", lwd = 2)
    
    legend("topright",
           inset = c(-0.25, 0),
           legend = c("Nearest Neighbor", "2-opt", "Held-Karp"),
           col = c("black", "gold", "purple"),
           lty = 1,
           pch = 16)
  })
  
 
  output$scalingPlot <- renderPlot({
    
    x_nn <- data$nn_time
    x_2opt <- data$twoopt_time
    y <- data$subset_size
    
    ord <- order(y)
    
    xmin <- min(c(x_nn, x_2opt), na.rm = TRUE)
    xmax <- max(c(x_nn, x_2opt), na.rm = TRUE)
    ymin <- min(y)
    ymax <- max(y)
    
    if (input$scale == "Log-Log") {
      plot(x_nn, y,
           log = "xy",
           xlim = c(xmin, xmax),
           ylim = c(ymin, ymax),
           pch = 16,
           col = "blue",
           xlab = " Log Time (ms)",
           ylab = "Subset Size",
           main = "Runtime at subset sizes of New York Metal Map")
    } else {
      plot(x_nn, y,
           xlim = c(xmin, xmax),
           ylim = c(ymin, ymax),
           pch = 16,
           col = "blue",
           xlab = "Time (ms)",
           ylab = "Subset Size",
           main = "Runtime at subset sizes of New York Metal Map")
    }
    
    points(x_2opt, y,
           col = "orange", pch = 16)
    
    lines(x_nn[ord], y[ord],
          col = "blue", lwd = 2)
    
    lines(x_2opt[ord], y[ord],
          col = "orange", lwd = 2)
    
    legend("topright",
           legend = c("Nearest Neighbor", "2-opt"),
           col = c("blue", "orange"),
           lty = 1,
           pch = 16)
  })
  
}

shinyApp(ui = ui, server = server)