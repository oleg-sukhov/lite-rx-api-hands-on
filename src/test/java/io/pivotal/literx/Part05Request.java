package io.pivotal.literx;

import io.pivotal.literx.domain.User;
import io.pivotal.literx.repository.ReactiveRepository;
import io.pivotal.literx.repository.ReactiveUserRepository;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.subscriber.ScriptedSubscriber;

/**
 * Learn how to control the demand.
 *
 * @author Sebastien Deleuze
 */
public class Part05Request {

	private ReactiveRepository<User> repository = new ReactiveUserRepository();

//========================================================================================

	@Test
	public void requestAll() {
		Flux<User> flux = repository.findAll();
		ScriptedSubscriber<Object> subscriber = requestAllExpectFour();
		subscriber.verify(flux);
	}

	private ScriptedSubscriber<Object> requestAllExpectFour() {
		return ScriptedSubscriber
				.create()
				.expectNextCount(4)
				.expectComplete();
	}

//========================================================================================

	@Test
	public void requestOneByOne() {
		Flux<User> flux = repository.findAll();
		ScriptedSubscriber<Object> subscriber = requestOneExpectSkylerThenRequestOneExpectJesse();
		subscriber.verify(flux);
	}

	private ScriptedSubscriber<Object> requestOneExpectSkylerThenRequestOneExpectJesse() {
		return ScriptedSubscriber
				.create(1)
				.expectNext(User.SKYLER)
				.thenRequest(1)
				.expectNext(User.JESSE)
				.thenCancel();
	}

//========================================================================================

	@Test
	public void experimentWithLog() {
		Flux<User> flux = fluxWithLog();
		ScriptedSubscriber.create(0)
				.thenRequest(1)
				.expectNextWith(u -> true)
				.thenRequest(1)
				.expectNextWith(u -> true)
				.thenRequest(2)
				.expectNextWith(u -> true)
				.expectNextWith(u -> true)
				.expectComplete()
				.verify(flux);
	}

	private Flux<User> fluxWithLog() {
		return repository
				.findAll()
				.log();
	}


//========================================================================================

	@Test
	public void experimentWithDoOn() {
		Flux<User> flux = fluxWithDoOnPrintln();
		ScriptedSubscriber.create()
				.expectNextCount(4)
				.expectComplete()
				.verify(flux);
	}

	private Flux<User> fluxWithDoOnPrintln() {
		return repository.findAll()
				.doOnSubscribe(subscription -> System.out.println("Starting!!!"))
				.doOnNext(user -> System.out.println(user.getFirstname() + "_" + user.getLastname()))
				.doOnComplete(() -> System.out.println("The end!"));
	}

}
