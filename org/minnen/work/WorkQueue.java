package org.minnen.work;

import java.util.*;

/** queue of work items */
public class WorkQueue implements Runnable
{
  protected LinkedList<WorkItem> Q;
  protected Thread[]             threads;
  protected volatile boolean     bRun = true;

  public WorkQueue(int nThreads)
  {
    Q = new LinkedList<WorkItem>();

    threads = new Thread[nThreads];
    for (int i = 0; i < nThreads; i++) {
      threads[i] = new Thread(this);
      threads[i].setDaemon(true);
      threads[i].setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
      // threads[i].setPriority(Thread.MIN_PRIORITY);
      threads[i].start();
    }
  }

  public void addWork(WorkItem work)
  {
    System.out.printf("add work: %s\n", work);
    synchronized (Q) {
      Q.add(work);
      Q.notify();
    }
  }

  public void stop()
  {
    bRun = false;
    synchronized (Q) {
      Q.notifyAll();
    }
  }

  @Override
  public void run()
  {
    System.out.printf("Work thread started (%s)!\n", Thread.currentThread().getId());

    while (bRun) {
      WorkItem work = null;

      synchronized (Q) {
        if (!Q.isEmpty()) work = Q.remove();
      }

      if (work != null) {
        System.out.printf("Thread %d found some work: %s\n", Thread.currentThread().getId(), work);
        work.run();
      } else {
        synchronized (Q) {
          while (bRun && Q.isEmpty()) {
            try {
              Q.wait(200);
            } catch (InterruptedException e) {}
          }
        }
      }
    }
  }

}
