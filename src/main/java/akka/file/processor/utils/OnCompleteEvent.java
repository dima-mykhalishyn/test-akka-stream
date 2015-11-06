package akka.file.processor.utils;

import akka.dispatch.OnComplete;
import scala.runtime.BoxedUnit;

/**
 * {@link OnComplete} event handler
 *
 * @author dmytro.mykhalishyn@rakuten.de
 * @see OnComplete
 */
public class OnCompleteEvent extends OnComplete<BoxedUnit> {

	private final Runnable runnable;

	/**
	 * Default constructor that take function, that should be executed
	 *
	 * @param runnable function that should be execute
	 */
	public OnCompleteEvent(final Runnable runnable) {
		this.runnable = runnable;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see OnComplete#onComplete(Throwable, Object)
	 */
	@Override
	public void onComplete(final Throwable throwable, final BoxedUnit boxedUnit) throws Throwable {
		if (this.runnable != null)
			this.runnable.run();
	}
}
