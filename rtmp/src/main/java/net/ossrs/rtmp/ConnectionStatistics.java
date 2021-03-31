package net.ossrs.rtmp;

import android.util.Log;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.Timer;
import java.util.TimerTask;

public class ConnectionStatistics {
	private static final String TAG = "ConnectionStatistics";

	protected Long mQueuedBytes = 0L;
	protected Long mSentBytes = 0L;
	protected CircularFifoQueue<Long> mQueuedBytesStats;
	protected CircularFifoQueue<Long> mSentBytesStats;
	protected Timer mTimer;
	protected boolean mRunning = false;

	ConnectionStatistics(int size, int period) {
		mQueuedBytesStats = new CircularFifoQueue<>(size);
		mSentBytesStats = new CircularFifoQueue<>(size);
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (mRunning)
					updateStats();
			}
		}, period, period);
	}

	protected void stop() {
		mRunning = false;
		mTimer.cancel();
		mTimer = null;
		synchronized (mQueuedBytesStats) {
			mQueuedBytesStats.clear();
			mQueuedBytes = 0L;
		}
		synchronized (mSentBytesStats) {
			mSentBytesStats.clear();
			mSentBytes = 0L;
		}
	}

	protected void start() {
		mRunning = true;
	}

	public void addQueued(long queued) {
		synchronized (mQueuedBytesStats) { mQueuedBytes += queued; }
	}

	public void addSent(long sent) {
		synchronized (mSentBytesStats) { mSentBytes += sent; }
	}

	public Long[] getQueuedBytesStats() {
		synchronized (mQueuedBytesStats) {
			return mQueuedBytesStats.toArray(new Long[mQueuedBytesStats.size()]);
		}
	}

	public Long[] getSentBytesStats() {
		synchronized (mSentBytesStats) {
			return mSentBytesStats.toArray(new Long[mSentBytesStats.size()]);
		}
	}

	protected void updateStats() {
		synchronized (mQueuedBytesStats) {
			mQueuedBytesStats.add(mQueuedBytes);
			mQueuedBytes = 0L;
		}
		synchronized (mSentBytesStats) {
			mSentBytesStats.add(mSentBytes);
			mSentBytes = 0L;
		}

		StringBuilder builder = new StringBuilder();
		Long[] queued = getQueuedBytesStats();
		Long[] sent = getSentBytesStats();
		for(int i = 0; i < queued.length; i++) {
			builder.append(queued[i]);
			builder.append("/");
			builder.append(sent[i]);
			builder.append(" ");
		}
		Log.i(TAG, "Connection stats: "+builder.toString());
	}
}
