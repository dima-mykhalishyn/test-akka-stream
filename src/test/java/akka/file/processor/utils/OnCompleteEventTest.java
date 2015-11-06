package akka.file.processor.utils;

import org.junit.Assert;
import org.junit.Test;
import scala.runtime.BoxedUnit;

/**
 * Tests for {@link OnCompleteEvent}
 *
 * @author dmytro.mykhalishyn@rakuten.de
 */
public class OnCompleteEventTest {

	@Test
	public void smokeTest() throws Throwable {
		final Exception exception = new Exception("test");
		new OnCompleteEvent(null).onComplete(exception, BoxedUnit.UNIT);
		new OnCompleteEvent(() -> {
			System.out.println("test");
		}).onComplete(exception, BoxedUnit.UNIT);
		Assert.assertTrue(true);
	}

}