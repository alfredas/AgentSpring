library(rjson)
library(RCurl)
options(warn=-1)
### FUNCTIONS ###
# start simulation
startSimulation <- function() {
  return(fromJSON(file="http://localhost:8080/agentspring-face/engine/start"))
}
# stop 
stopSimulation <- function() {
  return(fromJSON(file="http://localhost:8080/agentspring-face/engine/stop"))
}
# pause
pauseSimulation <- function() {
  return(fromJSON(file="http://localhost:8080/agentspring-face/engine/pause"))
}
# resume
resumeSimulation <- function() {
  return(fromJSON(file="http://localhost:8080/agentspring-face/engine/resume"))
}
# get status
statusSimulation <- function(){
  return(fromJSON(file="http://localhost:8080/agentspring-face/engine/status"))
}
# check if paused
isPausedSimulation <- function() {
  status <- fromJSON(file="http://localhost:8080/agentspring-face/engine/status")
  return(status$state == "PAUSED")
}
# pause and only return when paused (simulation is only paused when the current tick is over - can take a while in some cases)
waitPauseSimulation <- function() {
  pauseSimulation()
  while (!isPausedSimulation()) {
    Sys.sleep(5)
  }
  return(TRUE)
}
# get tick
tickSimulation <- function() {
  status <- statusSimulation()
  return(status$tick)
}
# query simulation - returns JSON
querySimulation <- function(start, query) {
  urlStr <- paste("http://localhost:8080/agentspring-face/db/query?start=",start,"&query=",curlEscape(query),sep="")
  return(fromJSON(file=urlStr))
}
# simulation runner - runs the simulation for 'tick' number of ticks and executes the 'x(tick)' function after the tick is finished
runSimulation <- function(x, ticks) {
  startSimulation()
  tick <- 0 
  while (tick < ticks) {
    waitPauseSimulation()
    tick <- tickSimulation()
    x(tick)
    resumeSimulation()
    tick <- tick + 1
  }
  st <- stopSimulation()
}

### END FUNCTIONS ###


