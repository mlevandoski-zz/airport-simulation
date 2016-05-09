import java.util.Random;
import java.util.Random;

public class Passenger implements Comparable<Passenger> {
  protected double arrivalTime;
  protected int bags;
  protected int seat;
  protected int dest;
  protected int agent;
  protected double serviceTime;
  protected double checkInArrivalTime;
  protected double checkInWaitTime;
  protected double securityWaitTime;
  protected double securityArrivalTime;
  protected double gateArrivalTime;
  protected double flightTime;
  protected boolean enter;
  protected boolean exit;

  public Passenger(double initialArrival, int dest, int firstClass, int flightTime) {
    this.dest = dest; // 0 = commuter, 1 = intl
    bags = numBags(dest);
    seat = firstClass; // 0 = coach, 1 = 1st class

    arrivalTime = initialArrival;
    serviceTime = 0;
    checkInArrivalTime = initialArrival;
    checkInWaitTime = 0;
    securityArrivalTime = 0;
    securityWaitTime = 0;
    gateArrivalTime = 0;
    this.flightTime = flightTime;

    agent = 0;
    enter = true;
    exit = false;

  }

  private int numBags(int dest){
    Random r = new Random();
    int bags = 0;
    double bias;
    if (dest == 0) { //commuter
      bias = .80;
    } else { //dest == 1, international
      bias = .60;
    }

    while (r.nextDouble() > bias) {
      bags++;
    }

    return bags;
  }

  @Override
  public int compareTo(Passenger other) {
    if (this.arrivalTime > other.arrivalTime) return 1;
    if (this.arrivalTime < other.arrivalTime) return -1;
    return 0;
  }
}
