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

# check if stopped
isStoppedSimulation <- function() {
  status <- fromJSON(file="http://localhost:8080/agentspring-face/engine/status")
  return(status$state == "STOPPED")
}

# pause and only return when paused (simulation is only paused when the current tick is over - can take a while in some cases)
waitPauseSimulation <- function() {
  pauseSimulation()
  while (!isPausedSimulation()) {
    Sys.sleep(5)
  }
  return(TRUE)
}

# stop and only return when stopped (simulation is only stopped when the current tick is over - can take a while in some cases)
waitStopSimulation <- function() {
  stopSimulation()
  while (!isStoppedSimulation()) {
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

#load scenario
loadScenario <- function(scenario) {
  curlPerform(url="http://localhost:8080/agentspring-face/engine/load", postfields=paste("scenario",scenario,sep="="), post = 1L)
}

#change the value of a parameter (object is your bean id)
changeParameter <- function(object, field, value) {
  data <- paste(paste("id",object,sep="="),paste("field",field,sep="="),paste("value",value,sep="="),sep="&")
  print(data)
  curlPerform(url="http://localhost:8080/agentspring-face/parameters/saveone", postfields=data, post = 1L)
}


# simulation runner - runs the simulation for 'tick' number of ticks and executes the 'x(tick)' function after the tick is finished
runSimulation <- function(x, ticks, run) {
  startSimulation()
  tick <- 0 
  while (tick < ticks) {
    waitPauseSimulation()
    tick <- tickSimulation()
    result <- try(x(tick, ticks, run));
    if(class(result) == "try-error") {
      resumeSimulation()
      waitStopSimulation()
      print(paste("error at tick",tick))
      return(result)
    }
    print(paste("finished tick",tick))
    resumeSimulation()
    tick <- tick + 1
  }
  st <- waitStopSimulation()
  return("success")
}

### END FUNCTIONS ###


