package io.pivotal.literx;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.subscriber.ScriptedSubscriber;

import java.time.Duration;

import static java.util.Arrays.asList;

/**
 * Learn how to create Flux instances.
 *
 * @author Sebastien Deleuze
 * @see <a href="http://projectreactor.io/core/docs/api/reactor/core/publisher/Flux.html">Flux Javadoc</a>
 * @see <a href="https://github.com/reactor/reactor-addons/blob/master/reactor-test/src/main/java/reactor/test/subscriber/ScriptedSubscriber.java>ScriptedSubscriber</a>
 */
public class Part01CreateFlux {

//========================================================================================

	@Test
	public void empty() {
		Flux<String> flux = emptyFlux();

		ScriptedSubscriber
				.create()
				.expectNextCount(0)
				.expectComplete()
				.verify(flux);
	}

	private Flux<String> emptyFlux() {
		return Flux.empty();
	}

//========================================================================================

	@Test
	public void fromValues() {
		Flux<String> flux = fooBarFluxFromValues();
		ScriptedSubscriber
				.create()
				.expectNext("foo", "bar")
				.expectComplete()
				.verify(flux);
	}

	private Flux<String> fooBarFluxFromValues() {
		return Flux.just("foo", "bar");
	}

//========================================================================================

	@Test
	public void fromList() {
		Flux<String> flux = fooBarFluxFromList();
		ScriptedSubscriber
				.create()
				.expectNext("foo", "bar")
				.expectComplete()
				.verify(flux);
	}

	private Flux<String> fooBarFluxFromList() {
		return Flux.fromIterable(asList("foo", "bar"));
	}

//========================================================================================

	@Test
	public void error() {
		Flux<String> flux = errorFlux();
		ScriptedSubscriber
				.create()
				.expectError(IllegalStateException.class)
				.verify(flux);
	}

	private Flux<String> errorFlux() {
		return Flux.error(new IllegalStateException());
	}

//========================================================================================

	@Test
	public void countEachSecond() {
		Flux<Long> flux = counter();
		ScriptedSubscriber
				.create()
				.expectNext(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L)
				.expectComplete()
				.verify(flux);
	}

	private Flux<Long> counter() {
		return Flux.interval(Duration.ofMillis(100)).take(10);
	}

}
