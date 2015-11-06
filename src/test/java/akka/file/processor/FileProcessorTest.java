package akka.file.processor;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Tests for {@link FileProcessor}
 *
 * @author dmytro.mykhalishyn@rakuten.de
 */
public class FileProcessorTest {

	private static final int LINES = 100000;

	private File in;

	private File out;

	@Before
	public void before() throws IOException {
		this.in = File.createTempFile("test", "in");
		this.out = File.createTempFile("test", "out");
		this.generateFile();
	}

	@Test
	public void testProcessFile() throws IOException {
		final ActorSystem system = ActorSystem.create("test");
		final ActorMaterializer materializer = ActorMaterializer.create(system);
		final FileProcessor fileProcessor = new FileProcessor(materializer, system.dispatcher());
		final PrintWriter output = new PrintWriter(new FileOutputStream(this.out), true);
		fileProcessor.processFile(this.in, output, system::shutdown);
		system.awaitTermination();
		output.close();
		try (final BufferedReader reader = Files.newBufferedReader(this.out.toPath())) {
			long sum = reader.lines().map(x -> Long.valueOf(x.split(";")[1])).reduce(0L, Long::sum);
			Assert.assertEquals(LINES, sum);
		}
	}

	@Test
	public void testValidation() throws IOException {
		final ActorSystem system = ActorSystem.create("test");
		final ActorMaterializer materializer = ActorMaterializer.create(system);
		final FileProcessor fileProcessor = new FileProcessor(materializer, system.dispatcher());
		final PrintWriter output = new PrintWriter(new FileOutputStream(this.out), true);
		try {
			fileProcessor.processFile(null, output, () -> {/*do nothing*/});
			Assert.assertTrue("Exception 1 should be thrown", false);
		} catch (Exception e) {
			Assert.assertTrue(true);
		} finally {
			output.close();
		}
		try {
			fileProcessor.processFile(this.out, null, null);
			Assert.assertTrue("Exception 2 should be thrown", false);
		} catch (Exception e) {
			Assert.assertTrue(true);
		}
	}

	@After
	@SuppressWarnings("all")
	public void after() throws IOException {
		this.in.delete();
		this.out.delete();
	}

	private void generateFile() throws FileNotFoundException {

		final Random random = new Random();
		final PrintWriter output = new PrintWriter(new FileOutputStream(this.in), true);
		IntStream.range(0, LINES).forEach(x -> {
			output.println(String.format(FileProcessor.RESULT_FORMAT, random.nextInt(11), 1));
		});
		output.close();
	}
}