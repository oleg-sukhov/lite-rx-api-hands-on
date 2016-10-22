package io.pivotal.literx;

import io.pivotal.literx.domain.User;
import io.pivotal.literx.repository.ReactiveRepository;
import io.pivotal.literx.repository.ReactiveUserRepository;
import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.subscriber.ScriptedSubscriber;
import reactor.util.function.Tuple3;

import java.security.cert.PKIXRevocationChecker;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

/**
 * Learn how to use various other operators.
 *
 * @author Sebastien Deleuze
 * @see <a href="http://projectreactor.io/core/docs/api/reactor/core/publisher/Flux.html">Flux Javadoc</a>
 * @see <a href="http://projectreactor.io/core/docs/api/reactor/core/publisher/Mono.html">Mono Javadoc</a>
 * @see <a href="https://github.com/reactor/reactor-addons/blob/master/reactor-test/src/main/java/reactor/test/subscriber/ScriptedSubscriber.java>ScriptedSubscriber</a>
 */
public class Part06OtherOperations {

	private static final User MARIE = new User("mschrader", "Marie", "Schrader");
	private static final User MIKE = new User("mehrmantraut", "Mike", "Ehrmantraut");

//========================================================================================

	@Test
	public void zipFirstNameAndLastName() {
		Flux<String> usernameFlux = Flux.just(User.SKYLER.getUsername(), User.JESSE.getUsername(), User.WALTER.getUsername(), User.SAUL.getUsername());
		Flux<String> firstnameFlux = Flux.just(User.SKYLER.getFirstname(), User.JESSE.getFirstname(), User.WALTER.getFirstname(), User.SAUL.getFirstname());
		Flux<String> lastnameFlux = Flux.just(User.SKYLER.getLastname(), User.JESSE.getLastname(), User.WALTER.getLastname(), User.SAUL.getLastname());
		Flux<User> userFlux = userFluxFromStringFlux(usernameFlux, firstnameFlux, lastnameFlux);
		ScriptedSubscriber.create()
				.expectNext(User.SKYLER, User.JESSE, User.WALTER, User.SAUL)
				.expectComplete()
				.verify(userFlux);
	}

	private Flux<User> userFluxFromStringFlux(Flux<String> usernameFlux, Flux<String> firstnameFlux, Flux<String> lastnameFlux) {
		return Flux
				.zip(usernameFlux, firstnameFlux, lastnameFlux)
				.map(this::toUser);
	}

	private User toUser(Tuple3<String, String, String> tuple) {
		return new User(tuple.getT1(), tuple.getT2(), tuple.getT3());
	}

//========================================================================================

	@Test
	public void fastestMono() {
		ReactiveRepository<User> repository1 = new ReactiveUserRepository(MARIE);
		ReactiveRepository<User> repository2 = new ReactiveUserRepository(250, MIKE);
		Mono<User> mono = useFastestMono(repository1.findFirst(), repository2.findFirst());
		ScriptedSubscriber.create()
				.expectNext(MARIE)
				.expectComplete()
				.verify(mono);

		repository1 = new ReactiveUserRepository(250, MARIE);
		repository2 = new ReactiveUserRepository(MIKE);
		mono = useFastestMono(repository1.findFirst(), repository2.findFirst());
		ScriptedSubscriber.create()
				.expectNext(MIKE)
				.expectComplete()
				.verify(mono);
	}

	private Mono<User> useFastestMono(Mono<User> mono1, Mono<User> mono2) {
		return Mono.first(mono1, mono2);
	}

//========================================================================================

	@Test
	public void fastestFlux() {
		ReactiveRepository<User> repository1 = new ReactiveUserRepository(MARIE, MIKE);
		ReactiveRepository<User> repository2 = new ReactiveUserRepository(250);
		Flux<User> flux = useFastestFlux(repository1.findAll(), repository2.findAll());
		ScriptedSubscriber.create()
				.expectNext(MARIE, MIKE)
				.expectComplete()
				.verify(flux);

		repository1 = new ReactiveUserRepository(250, MARIE, MIKE);
		repository2 = new ReactiveUserRepository();
		flux = useFastestFlux(repository1.findAll(), repository2.findAll());
		ScriptedSubscriber.create()
				.expectNext(User.SKYLER, User.JESSE, User.WALTER, User.SAUL)
				.expectComplete()
				.verify(flux);
	}

	private Flux<User> useFastestFlux(Flux<User> flux1, Flux<User> flux2) {
		return Flux.firstEmitting(flux1, flux2);
	}

//========================================================================================

	@Test
	public void complete() {
		ReactiveRepository<User> repository = new ReactiveUserRepository();
		Mono<Void> completion = fluxCompletion(repository.findAll());
		ScriptedSubscriber.create()
				.expectComplete()
				.verify(completion);
	}

	private Mono<Void> fluxCompletion(Flux<User> flux) {
		return flux.then();
	}

//========================================================================================

	@Test
	public void monoWithValueInsteadOfError() {
		Mono<User> mono = betterCallSaulForBogusMono(Mono.error(new IllegalStateException()));
		ScriptedSubscriber.create()
				.expectNext(User.SAUL)
				.expectComplete()
				.verify(mono);

		mono = betterCallSaulForBogusMono(Mono.just(User.SKYLER));
		ScriptedSubscriber.create()
				.expectNext(User.SKYLER)
				.expectComplete()
				.verify(mono);
	}

	private Mono<User> betterCallSaulForBogusMono(Mono<User> mono) {
		return mono.otherwise(e -> Mono.just(User.SAUL));
	}

//========================================================================================

	@Test
	public void fluxWithValueInsteadOfError() {
		Flux<User> flux = betterCallSaulAndJesseForBogusFlux(Flux.error(new IllegalStateException()));
		ScriptedSubscriber.create()
				.expectNext(User.SAUL, User.JESSE)
				.expectComplete()
				.verify(flux);

		flux = betterCallSaulAndJesseForBogusFlux(Flux.just(User.SKYLER, User.WALTER));
		ScriptedSubscriber.create()
				.expectNext(User.SKYLER, User.WALTER)
				.expectComplete()
				.verify(flux);
	}

	private Flux<User> betterCallSaulAndJesseForBogusFlux(Flux<User> flux) {
		return flux.onErrorResumeWith(throwable -> Flux.just(User.SAUL, User.JESSE));
	}

	//========================================================================================

	@Test
	public void nullHandling() {
		Mono<User> mono = nullAwareUserToMono(User.SKYLER);
		ScriptedSubscriber.create()
				.expectNext(User.SKYLER)
				.expectComplete()
				.verify(mono);
		mono = nullAwareUserToMono(null);
		ScriptedSubscriber.create()
				.expectComplete()
				.verify(mono);
	}

	private Mono<User> nullAwareUserToMono(User user) {
		//return ofNullable(user).map(Mono::just).orElse(Mono.empty());
		return Mono.justOrEmpty(user);
	}

}
