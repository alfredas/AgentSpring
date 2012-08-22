library(ggplot2)
source("simulation.R")

reports <- "reports/"

getBidCurve <- function(data) {
    amounts <- c(0)
    prices <- c(0)
    amount <- 0
    for (dt in data$result) {
      for (bid in dt) {
        prices <- c(prices,bid$properties$price)
        amount <- amount + bid$properties$amount
        amounts <- c(amounts,amount)
      }
    }
    prices <- c(prices,0)
    amounts <- c(amounts,amount)
    return(as.data.frame(cbind(amounts,prices)))
}

getClearingPoint <-function(data) {
  for (dt in data$result) {
    for (point in dt) {
      return(c(point$properties$price, point$properties$volume))
    }
  }
}

drawSupplyDemandForMarketSegment <- function(market, segment, tick) {
  chartName <- paste(reports,"drawSupplyDemandForMarketSegment",segment,"_",tick,".pdf",sep="")
  tryCatch({
    demandBids <- querySimulation(market, paste("v.in('segmentmarket').filter{it.segmentID==",segment,"}.back(2).in('market').filter{it.time==tick && it.supplyBid==false}.collect{it}.sort{it.price}.reverse()",sep=""))
    supplyBids <- querySimulation(market, paste("v.in('segmentmarket').filter{it.segmentID==",segment,"}.back(2).in('market').filter{it.time==tick && it.supplyBid==true}.collect{it}.sort{it.price}",sep=""))
    cpData<-querySimulation(market, paste("v.in('segmentmarket').filter{it.segmentID==",segment,"}.back(2).out('clearingpoint').filter{it.time==tick}",sep=""))
    cp <- getClearingPoint(cpData)
    supply <- getBidCurve(supplyBids)
    demand <- getBidCurve(demandBids)
    cpDf <- data.frame(amounts=supply$amounts,prices=supply$prices,clearedprice=cp[1],clearedvolume=cp[2])
    pdf(chartName)
    p <- ggplot()+geom_step(data=supply, aes(x=amounts, y=prices),direction = "vh", colour="blue") + geom_step(data=demand, aes(x=amounts, y=prices),direction = "vh",colour="red")
    p <- p + xlab("Amount") + ylab("Price")
    p <- p + geom_line(data=cpDf, aes(x = amounts, y = clearedprice), colour = "black",linetype = 2) + geom_line(data=cpDf, aes(x = clearedvolume, y = prices), colour = "black",linetype = 2)
    p <- p + opts(title=paste(market,"segment:",segment,"p:",round(cp[1],digits=2),"v:",round(cp[2],digits=0)))
    print(p)
    dev.off()
  }, error = function(ex) drawErrorGraph(chartName, ex))
}

drawSupplyDemandForMarketBySubstance <- function(market, substance, tick) {
  chartName <- paste(reports,"drawSupplyDemandForMarketBySubstance",gsub(" ", "_", substance),"_",tick,".pdf",sep="")
  tryCatch({
    demandBids <- querySimulation(market, paste("v.in('substancemarket').filter{it.name=='",substance,"'}.back(2).in('market').filter{it.time==tick && it.supplyBid==false}.collect{it}.sort{it.price}.reverse()",sep=""))
    supplyBids <- querySimulation(market, paste("v.in('substancemarket').filter{it.name=='",substance,"'}.back(2).in('market').filter{it.time==tick && it.supplyBid==true}.collect{it}.sort{it.price}",sep=""))
    cpData<-querySimulation(market, paste("v.in('substancemarket').filter{it.name=='",substance,"'}.back(2).out('clearingpoint').filter{it.time==tick}",sep=""))
    cp <- getClearingPoint(cpData)
    supply <- getBidCurve(supplyBids)
    demand <- getBidCurve(demandBids)
    cpDf <- data.frame(amounts=supply$amounts,prices=supply$prices,clearedprice=cp[1],clearedvolume=cp[2])
    pdf(chartName)
    p <- ggplot()+geom_step(data=supply, aes(x=amounts, y=prices),direction = "vh", colour="blue") + geom_step(data=demand, aes(x=amounts, y=prices),direction = "vh",colour="red")
    p <- p + xlab("Amount") + ylab("Price")
    p <- p + geom_line(data=cpDf, aes(x = amounts, y = clearedprice), colour = "black",linetype = 2) + geom_line(data=cpDf, aes(x = clearedvolume, y = prices), colour = "black",linetype = 2)
    p <- p + opts(title=paste(market,"substance:",substance,"p:",round(cp[1],digits=2),"v:",round(cp[2],digits=0))) + scale_x_log10() + scale_y_log10()
    print(p)
    dev.off()
  }, error = function(ex) drawErrorGraph(chartName, ex))
}

drawErrorGraph <- function(name,ex) {
  pdf(name)
  plot(0:1, 0:1, type= "n", xlab="", ylab="")
  text(0.5,0.5, paste("Error:",ex), cex=1)
  dev.off()
  fileConn <- file("error.log")
  writeLines(paste("Error in:",name,ex), fileConn)
  close(fileConn)
}

