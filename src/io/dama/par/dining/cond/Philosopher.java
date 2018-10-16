package io.dama.par.dining.cond;

import java.lang.Thread;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.atomic.AtomicBoolean;
import java.lang.InterruptedException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;

public class Philosopher extends Thread implements IPhilosopher {
    //Philosopher partners left and right and the tabel
    private Philosopher left;
    private Philosopher right;
    private Lock table;
    // We use this condition to get a signal from the neighbours when they finish eating
    private Condition neighbourNotEating;
    // Stop run method flag. Querried in the run method in a loop
    private AtomicBoolean stopped;
    // Variable to signal that this thread is eating
    // Can be querrried by the neighbours
    private AtomicBoolean eat;
    // Self-referenc to call interrupt on in a stop request
    private Thread selfThread;
    
    public Philosopher(){
	this.stopped = new AtomicBoolean(false);
	this.eat = new AtomicBoolean(false);
    }
    @Override
    public void run(){
	this.selfThread = Thread.currentThread();
	try{
	while(!(stopped.get())){
	    this.think();
	    this.eat();
	}
	}catch(InterruptedException e){
	    System.out.println(Thread.currentThread().getId()+" is leaving the table.");
	}
    }
    private void eat()throws InterruptedException{
	table.lock();
	try{
	    while(left.eat.get() || right.eat.get())
		neighbourNotEating.await((long)PhilosopherExperiment.MAX_TAKING_TIME_MS,TimeUnit.MILLISECONDS);
	    this.eat.set(true);
	}finally{
	    table.unlock();
	}
	System.out.println(Thread.currentThread().getId()+" is eating!\n\"Hmmm Yumy!\"\n");
	Thread.sleep(PhilosopherExperiment.MAX_EATING_DURATION_MS);
    }

    private void think()throws InterruptedException{
	this.table.lock();
	try{
	    this.eat.set(false);
	    left.neighbourNotEating.signal();
	    right.neighbourNotEating.signal();
	}finally{
	    table.unlock();
	}
	System.out.println(Thread.currentThread().getId()+" is thinking!\n"+this.thoughts()+"\n");
	Thread.sleep(PhilosopherExperiment.MAX_THINKING_DURATION_MS);
    }
    
    // Because we can't change the IPhilosopher interface witch lacks of some variables e.g. eat-variable
    // We have to cast here to Philosopher (this is ugly but changing a interface is worse because of compatability reasons)
    // If a obect not of static-type Philosopher is given as parameter this method calls an system-exit and halts the programm
    @Override
    public void setLeft(final IPhilosopher left) {
        //set the partner sitting to the left
	if(this.getClass() == left.getClass()){
	    this.left = (Philosopher)left;
	}else{
	    System.err.println("Error: Type missmatch.");
	    System.err.println("Class: Philosopher.");
	    System.err.println("Method: setRight().");
	    System.err.println("Variable-typ: Philosopher");
	    System.err.println("System-Exit!");
	    System.exit(1);
	}
    }
    // Because we can't change the IPhilosopher interface witch lacks of some variables e.g. eat-variable
    // We have to cast here to Philosopher (this is ugly but changing a interface is worse because of compatability reasons)
    // If a obect not of static-type Philosopher is given as parameter this method calls an system-exit and halts the programm
    @Override
    public void setRight(final IPhilosopher right) {
        //set the partner siting to the right
	if(this.getClass() == right.getClass()){
	    this.right = (Philosopher)right;
	}else{
	    System.err.println("Error: Type missmatch.");
	    System.err.println("Class: Philosopher.");
	    System.err.println("Method: setRight().");
	    System.err.println("Variable-typ: Philosopher");
	    System.err.println("System-Exit!");
	    System.exit(1);
	}
    }
    // Setting the table which is later used as a lock to ensure exlusive access to some
    // variables for state querries and assigments.
    // We further have to set a condition here because we cant do this in the constructor
    // as at the time the constructor is called no table is set (on which the condition depends)
    // This limitation results from the use of this class out of the manin class PhilosopherExperiment
    // and cannot be changed.
    @Override
    public void setTable(final Lock table) {
	this.table = table;
	this.neighbourNotEating = this.table.newCondition();
    }
    @Override
    public void stopPhilosopher() {
        // request termination
	this.stopped.set(true);
	if (this.selfThread != null) {
            this.selfThread.interrupt();
	}
	
    }
    private String thoughts(){
	int randomNum = ThreadLocalRandom.current().nextInt(0, 10);
	String thought;
	switch(randomNum){
	case 0: thought = "Die Kunst des Lebens besteht mehr im Ringen als im Tanzen.\n \"Marc Aurel\"";
	    break;
	case 1: thought = "Wer von Tag nicht zwei Drittel für sich selbst hat, ist ein Sklave.\n \"Friedrich Nietzsche\"";
	    break;
	case 2: thought = "Aus dem Spiegel-Spiel des Gerings des Ringes ereignet sich das Dingen des Dinges.\n \"Martin Heidegger\"";
	    break;
	case 3: thought = "Es weiß immer ein Esel einen andern zu schätzen.\n \"Meister Eckhard\"";
	    break;
	case 4: thought = "Bekommst du eine gute Frau, wirst du glücklich werden; bekommst du eine schlechte, wirst du Philosoph werden.\n \"Sokrates\"";
	    break;
	case 5: thought = "Sieh in den Spiegel: wenn du schön aussiehst, musst du auch Schönes tun\nwenn hässlich, musst du den Mangel an Natur durch Rechtschaffenheit ausgleichen.\n \"Bias von Priene\"";
	    break;
	case 6: thought = "Das Auto ist eine um den einzelnen Fahrer herum gebaute platonische Höhle.\n \"Peter Sloterdjik\"";
	    break;
	case 7: thought = "Alle Formen des Niedergangs sind dazu da, um mir Halt zu verleihen. \n \"Emile Cioran\"";
	    break;
	case 8: thought = "Der Widerspruch ist das Erheben der Vernunft über die Beschränkungen des Verstandes.\n \"Hegel\"";
	    break;
	case 9: thought = "Man soll schweigen oder Dinge sagen, die noch besser sind als das Schweigen.\n \"Pythagoras von Samos\"";
	    break;
	default: thought = "Himmel und Erde werden vergehen, aber meine Worte werden nicht vergehen.\n\"Jesus von Nazareth\"";
	    break;
	}
	return thought;
    }
    
}
