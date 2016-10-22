package io.pivotal.literx;

import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.subscriber.ScriptedSubscriber;

/**
 * Learn how to create Mono instances.
 *
 * @author Sebastien Deleuze
 * @see <a href="http://projectreactor.io/core/docs/api/reactor/core/publisher/Mono.html">Mono Javadoc</a>
 * @see <a href="https://github.com/reactor/reactor-addons/blob/master/reactor-test/src/main/java/reactor/test/subscriber/ScriptedSubscriber.java>ScriptedSubscriber</a>
 */
public class Part02CreateMono {

//========================================================================================

	@Test
	public void empty() {
		Mono<String> mono = emptyMono();
		ScriptedSubscriber
				.create()
				.expectNextCount(0)
				.expectComplete()
				.verify(mono);
	}

	private Mono<String> emptyMono() {
		return Mono.empty();
	}

//========================================================================================

	@Test
	public void fromValue() {
		Mono<String> mono = fooMono();
		ScriptedSubscriber
				.create()
				.expectNext("foo")
				.expectComplete()
				.verify(mono);
	}

	private Mono<String> fooMono() {
		return Mono.just("foo");
	}

//========================================================================================

	@Test
	public void error() {
		Mono<String> mono = errorMono();
		ScriptedSubscriber
				.create()
				.expectError(IllegalStateException.class)
				.verify(mono);
	}

	private Mono<String> errorMono() {
		return Mono.error(new IllegalStateException());
	}

}
