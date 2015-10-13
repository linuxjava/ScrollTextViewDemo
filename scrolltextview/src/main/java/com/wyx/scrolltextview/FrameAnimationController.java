package com.wyx.scrolltextview;

import android.os.Handler;
import android.os.Message;

import java.util.LinkedList;

public class FrameAnimationController {
	private static final int MSG_ANIMATE = 1000;

	public static final int ANIMATION_FRAME_DURATION = 1000 / 60;

	private static final Handler mHandler = new AnimationHandler();

	private static final LinkedList<Message> messages = new LinkedList<Message>();

	private FrameAnimationController() {
		throw new UnsupportedOperationException();
	}

	public static void requestAnimationFrame(Runnable runnable) {
		Message message = new Message();
		message.what = MSG_ANIMATE;
		message.obj = runnable;
		messages.push(message);
		mHandler.sendMessageDelayed(message, ANIMATION_FRAME_DURATION);

	}

	public static void requestFrameDelay(Runnable runnable, long delay) {
		Message message = new Message();
		message.what = MSG_ANIMATE;
		message.obj = runnable;
		mHandler.sendMessageDelayed(message, delay);
	}

	public static void removeAnimation() {
		if (messages.size() == 0) {
			mHandler.removeCallbacksAndMessages(null);
		} else {
			messages.pollFirst();
		}
	}

	private static class AnimationHandler extends Handler {
		@Override
		public void handleMessage(Message m) {
			switch (m.what) {
			case MSG_ANIMATE:
				if (m.obj != null) {
					((Runnable) m.obj).run();
				}
				break;
			}
		}
	}
}