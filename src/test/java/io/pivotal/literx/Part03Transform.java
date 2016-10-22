package io.pivotal.literx;

import io.pivotal.literx.domain.User;
import io.pivotal.literx.repository.ReactiveRepository;
import io.pivotal.literx.repository.ReactiveUserRepository;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.subscriber.ScriptedSubscriber;

/**
 * Learn how to transform values.
 *
 * @author Sebastien Deleuze
 * @see <a href="http://projectreactor.io/core/docs/api/reactor/core/publisher/Flux.html">Flux Javadoc</a>
 * @see <a href="http://projectreactor.io/core/docs/api/reactor/core/publisher/Mono.html">Mono Javadoc</a>
 * @see <a href="https://github.com/reactor/reactor-addons/blob/master/reactor-test/src/main/java/reactor/test/subscriber/ScriptedSubscriber.java>ScriptedSubscriber</a>
 */
public class Part03Transform {

	private ReactiveRepository<User> repository = new ReactiveUserRepository();

//========================================================================================

	@Test
	public void transformMono() {
		Mono<User> mono = repository.findFirst();
		ScriptedSubscriber
				.create()
				.expectNext(new User("SWHITE", "SKYLER", "WHITE"))
				.expectComplete()
				.verify(capitalizeOne(mono));
	}

	private Mono<User> capitalizeOne(Mono<User> mono) {
		return mono.map(this::toUpperCase);
	}


//========================================================================================

	@Test
	public void transformFlux() {
		Flux<User> flux = repository.findAll();
		ScriptedSubscriber
				.create()
				.expectNext(
					new User("SWHITE", "SKYLER", "WHITE"),
					new User("JPINKMAN", "JESSE", "PINKMAN"),
					new User("WWHITE", "WALTER", "WHITE"),
					new User("SGOODMAN", "SAUL", "GOODMAN"))
				.expectComplete()
				.verify(capitalizeMany(flux));
	}

	private Flux<User> capitalizeMany(Flux<User> flux) {
		return flux.map(this::toUpperCase);
	}

//========================================================================================

	@Test
	public void  asyncTransformFlux() {
		Flux<User> flux = repository.findAll();
		ScriptedSubscriber
				.create()
				.expectNext(
					new User("SWHITE", "SKYLER", "WHITE"),
					new User("JPINKMAN", "JESSE", "PINKMAN"),
					new User("WWHITE", "WALTER", "WHITE"),
					new User("SGOODMAN", "SAUL", "GOODMAN"))
				.expectComplete()
				.verify(asyncCapitalizeMany(flux));
	}

	private Flux<User> asyncCapitalizeMany(Flux<User> flux) {
		return flux.flatMap(this::asyncCapitalizeUser);
	}

	private Mono<User> asyncCapitalizeUser(User u) {
		return Mono.just(this.toUpperCase(u));
	}

	private User toUpperCase(User user) {
		final String userName = user.getUsername().toUpperCase();
		final String firstName = user.getFirstname().toUpperCase();
		final String lastName = user.getLastname().toUpperCase();
		return new User(userName, firstName, lastName);
	}

}
