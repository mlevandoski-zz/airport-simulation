import java.lang.Math;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

public class Airport {
  protected static int numComm;
  protected static int numInt;
  protected static int missedFlights;
  protected static int profit;
  protected static int ticketRevenue;
  protected static int agentPay;
  protected static int totalagents;
  protected static int numCoachCheckinAgents;
  protected static int numCoachSecAgents;
  protected static int numfcCheckinAgents;
  protected static int numfcSecAgents;
  protected static int numIntlFlights;
  protected static int commFlightFreq;
  protected static double gateAvgWait;
  private static double checkInAvgWait;
  private static double finalClock;
  private static double totalTimeSpentServicing;
  protected static Random r;

  /* Commuters: Poisson process, avg 40 people an hour
   * International: Normal dist, mean 75 min, var 50 min sq
   * method also handles most profit-related tasks
   */
  public static Queue<Passenger> arrival(int simTime) {
    Queue<Passenger> arrivalQueue = new PriorityQueue<Passenger>();
    int totalTime = 60*simTime; // number of minutes in the simulation
    numComm = numInt = ticketRevenue = agentPay = 0;
    // Calculate number of regional planes for the interval to get total cost of operation
    for (int i = commFlightFreq; i <= totalTime; i=(i+commFlightFreq)) profit -= 1000;

    // Regional arrivals every X minutes, follow Poisson distribution
    double interArrivalTime, t, prevTime, differentiate;
    interArrivalTime = t = 0;
    prevTime = -1;
    differentiate = 0.01;
    while (t < totalTime) {
      interArrivalTime = poisson(((double)40/60));
      if (prevTime >= interArrivalTime) interArrivalTime += differentiate;
      t += interArrivalTime;
      prevTime = interArrivalTime;
      if (t < totalTime) {
        arrivalQueue.add(new Passenger(t, 0, 0, -1));
        numComm++;
        ticketRevenue += 200;
      }
    }

    // International flights arrive every six hours
    // Passengers supposed to arrive 90 min before flight.
    // Follow normal distribution with mean 75, variance 50 min sq
    t = 0;
    numIntlFlights = 0;
    int minutes = 0;
    for (int i = 6; i <= simTime; i = (i+6)) {
      numIntlFlights++;
      profit -= 10000; //cost to operate each international flight
      minutes = (i*60);
      //coach, max capacity 150
      // chance of filling a seat 85%
      for (int j = 0; j < 150; j++) {
        if (r.nextDouble() < .85) {
          t = minutes - (r.nextGaussian()*Math.sqrt(50)+75);
          arrivalQueue.add(new Passenger(t, 1, 0, minutes));
          numInt++;
          ticketRevenue += 500;
        }
      }
      //first class, max capacity 50
      // chance of filling a seat: 80%
      for (int k = 0; k < 50; k++) {
        if (r.nextDouble()< .80) {
          t = minutes - (r.nextGaussian()*Math.sqrt(50)+75);
          arrivalQueue.add(new Passenger(t, 1, 1, minutes));
          numInt++;
          ticketRevenue += 1000;
        }
      }
    }

    agentPay = 25 * totalagents * simTime;
    profit -= agentPay;
    profit += ticketRevenue;

    return arrivalQueue;
  }

  public static void theGate(Queue<Passenger> gateQueue, int simTime) { // the gate
    Passenger p;
    gateAvgWait = 0;
    missedFlights = 0;
    while (gateQueue.peek() != null) {
      p = gateQueue.remove();
      if (p.dest == 0) { //Commuter, another queue for them
        for (int i = commFlightFreq; i <= simTime*60; i = (i + commFlightFreq)) {
          if ((p.gateArrivalTime > 30) && (p.gateArrivalTime < i)) {
            gateAvgWait += (i - p.gateArrivalTime);
            break;
          }
        }

      } else { // international
        if (p.flightTime < p.gateArrivalTime) { //missed flight
          missedFlights++;
          if ((p.flightTime - p.checkInArrivalTime) > 90) { // refund
            if (p.seat == 1) profit -= 1000; //ticket cost for 1st class
            else profit -= 500; // ticket cost for intl coach
          } // else they don't get a refund
        } // else they made their plane
      }
    }
    gateAvgWait = gateAvgWait / numComm;
  }

  public static void runSimulation(int simTime) {
    Queue arrivalQueue;
    profit=0;
    r = new Random();
    arrivalQueue = arrival(simTime);
    Queue<Passenger> securityQueue, gateQueue;
    Stations station1 = new Stations(arrivalQueue, 1, numCoachCheckinAgents, numfcCheckinAgents);
    securityQueue = station1.runStation();
    checkInAvgWait = station1.checkInAvgWait;
    finalClock = station1.finalClock;
    totalTimeSpentServicing = station1.totalTimeSpentServicing;
    Stations station2 = new Stations(securityQueue, 2, numCoachSecAgents, numfcSecAgents);
    gateQueue = station2.runStation();
    theGate(gateQueue, simTime);

  }


  public static void main(String args[]) {
    Scanner sc = new Scanner(System.in);
    int length = 0;
    System.out.printf("Welcome to the Airport Simulator.\n");
    System.out.printf("How many hours would you like to be simulated?\n");
    length = sc.nextInt();
    System.out.printf("How many check-in agents for first-class passengers? (max total agents = 6)\n");
    numfcCheckinAgents = sc.nextInt();
    System.out.printf("How many check-in agents for other passengers? (max total agents = 6)\n");
    numCoachCheckinAgents = sc.nextInt();
    System.out.printf("Every how many minutes do commuter planes run? Ex: 60\n");
    commFlightFreq = sc.nextInt();

    numfcSecAgents = 1;
    numCoachSecAgents = 2;

    totalagents = numfcSecAgents + numfcCheckinAgents + numCoachSecAgents + numCoachCheckinAgents;

    double totCheckInAvgWait = 0;
    int totProfit = 0;
    int totMissedFlights = 0;
    int totNumIntlFlights = 0;
    double totGateAvgWait = 0;
    double totPayPercentage = 0;
    double totIdleTime = 0;
    int runs = 200;

    for (int i=0; i<runs; i++) {
      runSimulation(length);
      totCheckInAvgWait += checkInAvgWait;
      totProfit += profit/runs;
      totMissedFlights += missedFlights;
      totNumIntlFlights += numIntlFlights;
      totGateAvgWait += gateAvgWait;
      totPayPercentage += (double)agentPay/ticketRevenue;
      totIdleTime += (finalClock - (double)totalTimeSpentServicing/totalagents);

    }
    //results
    System.out.printf("\nResults (averaged over %d runs) for %d first class counters and %d coach counters:\n", runs, numfcCheckinAgents, numCoachCheckinAgents);
    System.out.printf("\tProfit over %d hours: $%d\n", length, totProfit);
    System.out.printf("\tAverage check-in wait time: %.2f minutes\n", totCheckInAvgWait/runs);
    if (numInt != 0) System.out.printf("\tAverage number of international passengers that missed their flights (per flight): %f\n", ((double)totMissedFlights/totNumIntlFlights)/ runs);
    System.out.printf("\tAverage post-security-screening wait time for commuters: %.2f minutes\n", totGateAvgWait/runs);
    System.out.printf("\tPercentage of ticket revenue going to agent pay: %.2f\n", 100*totPayPercentage/runs);
    System.out.printf("\tAverage agent idle time per agent: %.2f hours\n", (double)(totIdleTime/(60*totalagents))/runs);

    System.out.printf("Thanks for using our Airport Simulator. Have a nice day!\n\n");
  }

  //returns next Poisson arrival time
  public static double poisson(double mean){
    double L = Math.exp(-mean);
    int k = 0;
    double p = 1.0;
    do {
      p = p * r.nextDouble();
      k++;
    } while (p > L);
    return k-1;
  }

  public static double exponential(double lambda) {
    return Math.log(1-r.nextDouble())/(-lambda);
  }

}
