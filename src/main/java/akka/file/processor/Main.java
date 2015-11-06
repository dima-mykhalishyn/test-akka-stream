package akka.file.processor;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * Main executor
 *
 * @author dmytro.mykhalishyn@rakuten.de
 */
public class Main {

	public static void main(final String[] args) throws Exception {
		Validate.isTrue(args.length == 2, "Expected 2 arguments: Path to input file, Pathe to output file");
		final File from = new File(args[0]);
		final File to = new File(args[1]);
		Validate.isTrue(from.isFile(), "Expected first parameter as path to input file that exist");
		Validate.isTrue(to.isFile() || to.createNewFile(), "Expected second parameter as path to output file that exist");
		final PrintWriter writer = new PrintWriter(new FileOutputStream(to), true);
		final ActorSystem system = ActorSystem.create("test");
		final ActorMaterializer materializer = ActorMaterializer.create(system);
		final FileProcessor fileProcessor = new FileProcessor(materializer, system.dispatcher());
		fileProcessor.processFile(from, writer, system::shutdown);
		system.awaitTermination();
		writer.close();
		System.out.println("*****Done****");
		System.out.println("Please check the file: " + args[1]);
	}

}
