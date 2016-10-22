/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.literx;

import io.pivotal.literx.domain.User;
import io.pivotal.literx.repository.ReactiveRepository;
import io.pivotal.literx.repository.ReactiveUserRepository;
import org.junit.Test;
import reactor.adapter.RxJava1Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.subscriber.ScriptedSubscriber;
import rx.Observable;
import rx.Single;

import java.util.concurrent.CompletableFuture;

/**
 * Learn how to convert from/to Java 8+ CompletableFuture and RxJava Observable/Single.
 *
 * Mono and Flux already implements Reactive Streams interfaces so they are natively
 * Reactive Streams compliant + there are Mono.from(Publisher) and Flux.from(Publisher)
 * factory methods.
 *
 * @author Sebastien Deleuze
 * @see <a href="https://github.com/reactor/reactor-addons/blob/master/reactor-test/src/main/java/reactor/test/subscriber/ScriptedSubscriber.java>ScriptedSubscriber</a>
 */
public class Part08Conversion {

	private ReactiveRepository<User> repository = new ReactiveUserRepository();

//========================================================================================

	@Test
	public void observableConversion() {
		Flux<User> flux = repository.findAll();
		Observable<User> observable = fromFluxToObservable(flux);
		ScriptedSubscriber.create()
				.expectNext(User.SKYLER, User.JESSE, User.WALTER, User.SAUL)
				.expectComplete()
				.verify(fromObservableToFlux(observable));
	}

	private Observable<User> fromFluxToObservable(Flux<User> flux) {
		return RxJava1Adapter.publisherToObservable(flux);
	}

	private Flux<User> fromObservableToFlux(Observable<User> observable) {
		return RxJava1Adapter.observableToFlux(observable);
	}

//========================================================================================

	@Test
	public void singleConversion() {
		Mono<User> mono = repository.findFirst();
		Single<User> single = fromMonoToSingle(mono);
		ScriptedSubscriber.create()
				.expectNext(User.SKYLER)
				.expectComplete()
				.verify(fromSingleToMono(single));
	}

	private Single<User> fromMonoToSingle(Mono<User> mono) {
		return RxJava1Adapter.publisherToSingle(mono);
	}

	private Mono<User> fromSingleToMono(Single<User> single) {
		return RxJava1Adapter.singleToMono(single);
	}

//========================================================================================

	@Test
	public void completableFutureConversion() {
		Mono<User> mono = repository.findFirst();
		CompletableFuture<User> future = fromMonoToCompletableFuture(mono);
		ScriptedSubscriber.create()
				.expectNext(User.SKYLER)
				.expectComplete()
				.verify(fromCompletableFutureToMono(future));
	}

	private CompletableFuture<User> fromMonoToCompletableFuture(Mono<User> mono) {
		return mono.toFuture();
	}

	private Mono<User> fromCompletableFutureToMono(CompletableFuture<User> future) {
		return Mono.fromFuture(future);
	}

}
