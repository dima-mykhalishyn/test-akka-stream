package akka.file.processor;

import akka.file.processor.utils.OnCompleteEvent;
import akka.stream.ActorMaterializer;
import akka.stream.io.Framing;
import akka.stream.io.SynchronousFileSource;
import akka.util.ByteString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import scala.concurrent.ExecutionContextExecutor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * File processor
 *
 * @author dmytro.mykhalishyn@rakuten.de
 */
public class FileProcessor {

	public static final String RESULT_FORMAT = "%s;%d";

	private final ActorMaterializer materializer;

	private final ExecutionContextExecutor dispatcher;

	/**
	 * Base constructor
	 *
	 * @param materializer the {@code ActorMaterializer} for running threads. Cannot be {@code null}
	 * @param dispatcher   the {@code ExecutionContextExecutor}. Cannot be @{code null}
	 */
	public FileProcessor(final ActorMaterializer materializer,
								final ExecutionContextExecutor dispatcher) {
		Validate.notNull(materializer, "ActorMaterializer cannot be null");
		Validate.notNull(dispatcher, "ExecutionContextExecutor cannot be null");
		this.materializer = materializer;
		this.dispatcher = dispatcher;
	}

	/**
	 * File processing. Read data from file and put the statistics to other file
	 *
	 * @param from       file to read. Cannot be {@code null}
	 * @param writer     writer. Cannot be {@code null}
	 * @param onComplete the event that should be executed on completing the logic. Can be {@code null}
	 * @throws FileNotFoundException
	 */
	public void processFile(final File from, final PrintWriter writer, final Runnable onComplete) throws FileNotFoundException {
		Validate.notNull(from, "Input File cannot be null");
		Validate.notNull(writer, "Writer cannot be null");
		SynchronousFileSource.create(from)
				.via(Framing.delimiter(ByteString.fromString(System.lineSeparator()), 1024, true))
				.map(byteString -> byteString.utf8String().split(";")) // lines to array
				.groupBy(lineParts -> StringUtils.trimToEmpty(lineParts[0])) // group by id
				.runForeach(idValuePaar -> {
					final AtomicLong counter = new AtomicLong(0);
					final String key = idValuePaar.first();
					idValuePaar.second().runForeach(values -> {
						final Long value =
								(values.length > 0 && NumberUtils.isDigits(values[1]))
										? Long.valueOf(values[1]) : 0;
						counter.updateAndGet(n -> n + value); // update amount
					}, this.materializer).onComplete(new OnCompleteEvent(() -> {
						writer.println(String.format(RESULT_FORMAT, key, counter.intValue()));
					}), this.dispatcher);
				}, this.materializer)
				.onComplete(new OnCompleteEvent(onComplete), this.dispatcher);
	}

}
