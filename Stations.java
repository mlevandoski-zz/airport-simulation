// A separate class for the stations for organizations's sake
// Handles Check-in station and Security station
import java.util.Collections;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Stations {

  private int station;
  private double clock;
  protected double checkInAvgWait;
  protected double totalTimeSpentServicing;
  protected double finalClock;

  private Queue<Passenger> q;
  private Queue<Passenger> firstClassQueue;
  private Queue<Passenger> coachQueue;
  private Queue<Passenger> waiting;
  private Queue<Passenger> returnQ;

  private List<Boolean> coachAgents;
  private List<Boolean> fcAgents;
  private List<Boolean> agents;

  private Random r;

  public Stations(Queue<Passenger> arrivalQueue, int station, int numCoachAgents, int numFcAgents) {
    clock = 0;
    this.station = station;
    checkInAvgWait = 0;
    totalTimeSpentServicing = 0;
    finalClock = 0;

    coachAgents = new ArrayList<Boolean>(Collections.nCopies(numCoachAgents, true));
    fcAgents = new ArrayList<Boolean>(Collections.nCopies(numFcAgents, true));

    q = arrivalQueue;
    firstClassQueue = new PriorityQueue<Passenger>();
    coachQueue = new PriorityQueue<Passenger>();
    waiting = new PriorityQueue<Passenger>();
    returnQ = new PriorityQueue<Passenger>();

    r = new Random();
  }

  public Queue<Passenger> runStation() {
    Passenger p;
    // Iterates through all Passengers that need to go through the station
    while (!q.isEmpty()) {
      p = q.remove();
      clock = p.arrivalTime;
      if (p.enter) enterStation(p);
      else exitStation(p);
    }

    finalClock = clock;
    checkInAvgWait = checkInAvgWait/returnQ.size();
    return returnQ;
  }

  public void enterStation(Passenger p) { // check-in counters
    // two queues, one for coach, one for first class

    if (p.seat == 1) waiting = firstClassQueue;
    else waiting = coachQueue;

    boolean result = allocateAgents(p);
    if (!result) waiting.add(p); // Passenger hasn't been served yet


  }

  public void exitStation(Passenger p) {
    Passenger pWaiting = null;
    p.enter = true;
    p.exit = false;
    double waitTime;
    if (station == 1) {
      waitTime = clock - p.checkInArrivalTime - p.serviceTime;
      p.checkInWaitTime = waitTime;
      p.securityArrivalTime = clock;
      checkInAvgWait += waitTime;

    } else if (station == 2) {
      waitTime = clock - p.securityArrivalTime - p.serviceTime;
      p.securityWaitTime = waitTime;
      p.gateArrivalTime = clock;
    }

    p.arrivalTime = clock;
    returnQ.add(p); // this pasenger is ready for the next station

    // Next passenger
    waiting = null;
    agents = null;
    if (p.seat == 0) {
      waiting = coachQueue;
      agents = coachAgents;
    }
    if (p.seat == 1) {
      waiting = firstClassQueue;
      agents = fcAgents;
    }
    agents.set(p.agent, true); //set agent to available

    if (!waiting.isEmpty()) {
      pWaiting = waiting.remove();
      allocateAgents(pWaiting);
    }
  }

  public boolean allocateAgents(Passenger p) {
    List<Boolean> agents = null;
    if (p.seat == 1) agents = fcAgents;
    else if (p.seat == 0) agents = coachAgents;

    boolean available;
    for (int i = 0; i < agents.size(); i++) {
      available = agents.get(i);
      if (available == true) {
        agents.set(i, false);
        p.agent = i;
        if (station == 1) calc_service_time(p);
        else p.serviceTime = exponential(3);

        p.enter = false;
        p.exit = true;
        p.arrivalTime = clock + p.serviceTime;

        q.add(p);
        return true;
      }
    }

    return false;
  }

  public void calc_service_time(Passenger p) {
    p.serviceTime = 0;
    p.serviceTime += exponential(2); // print boarding pass
    for (int i = 0; i < p.bags; i++) p.serviceTime += exponential(1); //checking bags
    p.serviceTime += exponential(3); // other delays
    totalTimeSpentServicing += p.serviceTime;
  }

  public double exponential(int lambda) {
    return Math.log(1-r.nextDouble())/(-lambda);
  }
}
